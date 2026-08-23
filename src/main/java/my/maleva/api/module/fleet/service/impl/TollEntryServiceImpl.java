package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.TollEntryDetailDto;
import my.maleva.api.module.fleet.dto.TollEntryDetailRowDto;
import my.maleva.api.module.fleet.dto.TollEntryListItemDto;
import my.maleva.api.module.fleet.dto.TollEntryListResponse;
import my.maleva.api.module.fleet.dto.request.TollEntryDetailRequest;
import my.maleva.api.module.fleet.dto.request.TollEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.TollEntrySearchRequest;
import my.maleva.api.module.fleet.entity.TollEntry;
import my.maleva.api.module.fleet.entity.TollEntryDetails;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.TollEntryDetailsRepository;
import my.maleva.api.module.fleet.repository.TollEntryRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.TollEntryService;
import my.maleva.api.module.fleet.specification.TollEntrySpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Toll entry business logic.
 *
 * Differences from the legacy TollEntryServices, all deliberate:
 * <ul>
 *   <li>the SP call and every WHERE clause are parameterised - the legacy code
 *       concatenated them, and stripped apostrophes out of remarks to cope;</li>
 *   <li>the list returns headers only, with a transaction count, instead of
 *       every detail row of every entry in the range;</li>
 *   <li>the header Amount is recomputed from the lines rather than trusted from
 *       the browser.</li>
 * </ul>
 */
@Service
public class TollEntryServiceImpl implements TollEntryService {

    private static final Logger logger = LoggerFactory.getLogger(TollEntryServiceImpl.class);

    private static final Integer ACTIVE = 1;
    private static final String TOLL_NUMBER_PREFIX = "TE";
    private static final int TOLL_NUMBER_DIGITS = 9;
    private static final String SEQUENCE_NAME = "TollEntry";

    private final TollEntryRepository tollEntryRepository;
    private final TollEntryDetailsRepository tollEntryDetailsRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public TollEntryServiceImpl(TollEntryRepository tollEntryRepository,
                                TollEntryDetailsRepository tollEntryDetailsRepository,
                                TruckMasterRepository truckMasterRepository,
                                JdbcTemplate jdbcTemplate,
                                ObjectMapper objectMapper) {
        this.tollEntryRepository = tollEntryRepository;
        this.tollEntryDetailsRepository = tollEntryDetailsRepository;
        this.truckMasterRepository = truckMasterRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------- list

    @Override
    @Transactional(readOnly = true)
    public TollEntryListResponse search(TollEntrySearchRequest request) {
        requireCompany(request.getCompanyRefId());
        boolean byNumber = request.getSearch() != null && !request.getSearch().isBlank();
        if (!byNumber && (request.getFromDate() == null || request.getToDate() == null)) {
            throw new InvalidRequestException("fromDate and toDate are required unless a toll number is given");
        }
        if (!byNumber && request.getFromDate().isAfter(request.getToDate())) {
            throw new InvalidRequestException("fromDate must not be after toDate");
        }

        List<TollEntry> entries = tollEntryRepository.findAll(
                TollEntrySpecification.from(request), TollEntryRepository.DEFAULT_SORT);

        Map<Integer, String> truckNames = truckNames(request.getCompanyRefId());

        List<TollEntryListItemDto> items = entries.stream()
                .map(entry -> TollEntryListItemDto.builder()
                        .id(entry.getId())
                        .cNumber(entry.getCNumber())
                        .cNumberDisplay(entry.getCNumberDisplay())
                        .saleDate(entry.getSaleDate() == null ? null : entry.getSaleDate().toLocalDate())
                        .truckRefId(entry.getTruckRefid())
                        .truckName(truckNames.get(entry.getTruckRefid()))
                        .amount(toDouble(entry.getAmount()))
                        .detailCount((int) tollEntryDetailsRepository
                                .countByTollEntryMasterRefId(entry.getId()))
                        .remarks(entry.getRemarks())
                        .filePath(entry.getFilePath())
                        .build())
                .toList();

        double total = items.stream()
                .mapToDouble(item -> item.getAmount() == null ? 0d : item.getAmount())
                .sum();

        return TollEntryListResponse.builder()
                .items(items)
                .entriesTotal(round2(total))
                .build();
    }

    // -------------------------------------------------------------- numbering

    @Override
    @Transactional(readOnly = true)
    public String nextTollNumber(Integer companyRefId) {
        requireCompany(companyRefId);
        Integer next = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = ? AND SequenceName = ?",
                Integer.class, companyRefId, SEQUENCE_NAME);

        int value = next == null ? 1 : next;
        return TOLL_NUMBER_PREFIX + String.format("%0" + TOLL_NUMBER_DIGITS + "d", value);
    }

    // ------------------------------------------------------------------ save

    @Override
    @Transactional
    public TollEntryDetailDto save(TollEntrySaveRequest request, String username) {
        requireCompany(request.getCompanyRefId());

        List<TollEntryDetailRequest> details =
                request.getDetails() == null ? List.of() : request.getDetails();
        if (details.isEmpty()) {
            throw new InvalidRequestException("A toll entry needs at least one transaction");
        }

        // The header total is the sum of the lines. The legacy screen computed it
        // in JavaScript and posted it, so it could disagree with its own details.
        double amount = details.stream()
                .mapToDouble(line -> line.getEntryAmount() == null ? 0d : line.getEntryAmount())
                .sum();

        String payload = buildStoredProcedurePayload(request, details, round2(amount));

        // Bound parameters. The legacy call pasted the JSON into the statement
        // after stripping apostrophes, so a location name containing one could
        // rewrite the command.
        Map<String, Object> result = jdbcTemplate.queryForMap(
                "EXEC [SP_TollEntry] ?, ?", payload, request.getCompanyRefId());

        Integer resultCode = asInteger(result.get("Result"));
        if (resultCode == null || resultCode != 1) {
            String message = String.valueOf(result.getOrDefault("Msg", "Toll entry was not saved"));
            logger.warn("SP_TollEntry rejected the entry: {}", message);
            throw new InvalidRequestException(message);
        }

        Integer savedId = asInteger(result.get("Id"));
        if (savedId == null || savedId == 0) {
            savedId = request.getId();
        }
        return getForEdit(savedId, null, request.getCompanyRefId());
    }

    /**
     * Builds the JSON array SP_TollEntry expects.
     *
     * The procedure reads the header with OPENJSON and then reads
     * {@code $.SaleDetails} again as nested JSON, so the transactions have to be
     * an array inside the header object rather than a separate argument.
     * Jackson builds it so quotes and backslashes are escaped properly.
     */
    private String buildStoredProcedurePayload(TollEntrySaveRequest request,
                                               List<TollEntryDetailRequest> details,
                                               double amount) {
        ObjectNode master = objectMapper.createObjectNode();
        master.put("Id", request.getId() == null ? 0 : request.getId());
        master.put("CompanyRefId", request.getCompanyRefId());
        master.put("EmployeeRefId", request.getEmployeeRefId() == null ? 0 : request.getEmployeeRefId());
        master.put("TruckRefid", request.getTruckRefId());
        master.put("UserRefId", 0);
        master.put("SaleDate", request.getSaleDate().toString());
        master.put("Amount", amount);
        master.put("Remarks", request.getRemarks() == null ? "" : request.getRemarks());
        master.put("FilePath", request.getFilePath() == null ? "" : request.getFilePath());
        master.put("CNumberDisplay", "");
        master.put("CNumber", 0);

        ArrayNode lines = objectMapper.createArrayNode();
        for (TollEntryDetailRequest line : details) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("EntryAmount", nullSafe(line.getEntryAmount()));
            node.put("EntryBalance", nullSafe(line.getEntryBalance()));
            node.put("EntryDate", line.getEntryDate() == null ? null : line.getEntryDate().toString());
            node.put("EntryTime", line.getEntryTime() == null ? null : line.getEntryTime().toString());
            node.put("TransType", line.getTransType() == null ? "" : line.getTransType());
            node.put("VehicleClass", line.getVehicleClass() == null ? 0 : line.getVehicleClass());
            node.put("EntrySP", blankIfNull(line.getEntrySP()));
            node.put("EntryLocation", blankIfNull(line.getEntryLocation()));
            node.put("ExitSP", blankIfNull(line.getExitSP()));
            node.put("ExitLocation", blankIfNull(line.getExitLocation()));
            node.put("TransNo", blankIfNull(line.getTransNo()));
            node.put("TransactionID", blankIfNull(line.getTransactionId()));
            node.put("VehicleNumber", blankIfNull(line.getVehicleNumber()));
            node.put("MFGNumber", blankIfNull(line.getMfgNumber()));
            lines.add(node);
        }
        master.set("SaleDetails", lines);

        ArrayNode rows = objectMapper.createArrayNode();
        rows.add(master);
        return rows.toString();
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public TollEntryDetailDto getForEdit(Integer id, Integer tollNumber, Integer companyRefId) {
        requireCompany(companyRefId);

        Integer lookupId = id;
        if (tollNumber != null && tollNumber != 0) {
            lookupId = tollEntryRepository.findIdsByCNumber(companyRefId, tollNumber)
                    .stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No toll entry with number " + tollNumber));
        }
        if (lookupId == null || lookupId == 0) {
            throw new InvalidRequestException("Either id or tollNumber must be supplied");
        }

        final Integer resolvedId = lookupId;
        TollEntry entry = tollEntryRepository
                .findByIdAndCompanyRefIdAndActive(resolvedId, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Toll entry " + resolvedId + " was not found"));

        return toDetail(entry, true);
    }

    @Override
    @Transactional(readOnly = true)
    public TollEntryDetailDto getForPrint(Integer id, Integer companyRefId) {
        requireCompany(companyRefId);
        TollEntry entry = tollEntryRepository
                .findByIdAndCompanyRefIdAndActive(id, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Toll entry " + id + " was not found"));
        return toDetail(entry, true);
    }

    private TollEntryDetailDto toDetail(TollEntry entry, boolean withDetails) {
        List<TollEntryDetailRowDto> lines = !withDetails ? List.of()
                : tollEntryDetailsRepository.findByTollEntryMasterRefId(entry.getId()).stream()
                        .map(this::toDetailRow)
                        .toList();

        return TollEntryDetailDto.builder()
                .id(entry.getId())
                .companyRefId(entry.getCompanyRefId())
                .cNumberDisplay(entry.getCNumberDisplay())
                .cNumber(entry.getCNumber())
                .saleDate(entry.getSaleDate() == null ? null : entry.getSaleDate().toLocalDate())
                .truckRefId(entry.getTruckRefid())
                .truckName(truckName(entry.getTruckRefid()))
                .employeeRefId(entry.getEmployeeRefId())
                .amount(toDouble(entry.getAmount()))
                .remarks(entry.getRemarks())
                .filePath(entry.getFilePath())
                .details(lines)
                .build();
    }

    private TollEntryDetailRowDto toDetailRow(TollEntryDetails line) {
        return TollEntryDetailRowDto.builder()
                .id(line.getId())
                .tollEntryMasterRefId(line.getTollEntryMasterRefId())
                .entryAmount(toDouble(line.getEntryAmount()))
                .entryBalance(toDouble(line.getEntryBalance()))
                .entryDate(line.getEntryDate() == null ? null : line.getEntryDate().toLocalDate())
                .entryTime(line.getEntryTime())
                .transType(line.getTransType())
                .vehicleClass(line.getVehicleClass())
                .entrySP(line.getEntrySP())
                .entryLocation(line.getEntryLocation())
                .exitSP(line.getExitSP())
                .exitLocation(line.getExitLocation())
                .transNo(line.getTransNo())
                .transactionId(line.getTransactionID())
                .vehicleNumber(line.getVehicleNumber())
                .mfgNumber(line.getMfgNumber())
                .build();
    }

    // ---------------------------------------------------------------- delete

    @Override
    @Transactional
    public void delete(Integer id, Integer companyRefId, String username) {
        requireCompany(companyRefId);
        int updated = tollEntryRepository.softDelete(id, companyRefId, username);
        if (updated == 0) {
            throw new EntityNotFoundException("Toll entry " + id + " was not found");
        }
    }

    // --------------------------------------------------------------- helpers

    private Map<Integer, String> truckNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (TruckMaster truck : truckMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(truck.getId(), truck.getTruckName());
        }
        return names;
    }

    private String truckName(Integer truckRefId) {
        if (truckRefId == null) {
            return null;
        }
        return truckMasterRepository.findById(truckRefId).map(TruckMaster::getTruckName).orElse(null);
    }

    private void requireCompany(Integer companyRefId) {
        if (companyRefId == null || companyRefId == 0) {
            throw new InvalidRequestException("companyRefId is required");
        }
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * Widens a value from a SQL Server {@code real} column, rounding away the
     * 4-byte float noise that otherwise reaches the screen as 4.670000076293945.
     */
    private Double toDouble(Float value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value.doubleValue())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double nullSafe(Double value) {
        return value == null ? 0d : value;
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }
}

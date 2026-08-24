package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.PassEntryDetailDto;
import my.maleva.api.module.fleet.dto.PassEntryListItemDto;
import my.maleva.api.module.fleet.dto.PassEntryListResponse;
import my.maleva.api.module.fleet.dto.RtiOptionDto;
import my.maleva.api.module.fleet.dto.request.PassEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.PassEntry;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.PassEntryRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.PassEntryService;
import my.maleva.api.module.fleet.specification.PassEntrySpecification;
import my.maleva.api.module.rti.entity.RTIMaster;
import my.maleva.api.module.rti.repository.RTIMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The truck pass algorithm, written once for levi and auto pass entries.
 *
 * The two legacy .NET services were byte-identical apart from the {@code LE} /
 * {@code AP} number prefix, and their tables carry the same columns, so a
 * subclass supplies only its repository, stored procedure name, sequence name
 * and prefix.
 *
 * Differences from the legacy services, all deliberate and shared by both:
 * <ul>
 *   <li>the stored procedure call and every WHERE clause are parameterised. The
 *       legacy code pasted a JSON string into the statement after stripping
 *       apostrophes out of it, so a remark containing one was silently mangled -
 *       and anything it failed to strip could rewrite the command;</li>
 *   <li>the list resolves truck, driver and RTI names with three bulk lookups
 *       instead of joining four tables per row, and returns named fields rather
 *       than {@code select A.*};</li>
 *   <li>"open by document number" reads the real table. Both legacy queries
 *       named a {@code ...Master} table that does not exist, so that path always
 *       threw;</li>
 *   <li>the truck, driver and RTI are validated server-side. The legacy screens
 *       enforced them only in JavaScript.</li>
 * </ul>
 *
 * @param <T> the entity this screen stores
 */
public abstract class AbstractPassEntryService<T extends PassEntry> implements PassEntryService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPassEntryService.class);

    protected static final Integer ACTIVE = 1;
    private static final int NUMBER_DIGITS = 9;

    private final PassEntryRepository<T> repository;
    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final RTIMasterRepository rtiMasterRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    protected AbstractPassEntryService(PassEntryRepository<T> repository,
                                       TruckMasterRepository truckMasterRepository,
                                       DriverMasterRepository driverMasterRepository,
                                       RTIMasterRepository rtiMasterRepository,
                                       JdbcTemplate jdbcTemplate,
                                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.truckMasterRepository = truckMasterRepository;
        this.driverMasterRepository = driverMasterRepository;
        this.rtiMasterRepository = rtiMasterRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // ------------------------------------------------------ subclass contract

    /** Stored procedure that writes this document, e.g. {@code SP_LeviEntry}. */
    protected abstract String storedProcedureName();

    /** {@code SequenceNoMaster.SequenceName} for this document, e.g. {@code LeviEntry}. */
    protected abstract String sequenceName();

    /** Two-letter document prefix, e.g. {@code LE} or {@code AP}. */
    protected abstract String numberPrefix();

    /** Human name used in messages, e.g. "levi entry". */
    protected abstract String documentLabel();

    // ----------------------------------------------------------------- list

    @Override
    @Transactional(readOnly = true)
    public PassEntryListResponse search(PassEntrySearchRequest request) {
        requireCompany(request.getCompanyRefId());

        boolean byNumber = PassEntrySpecification.isSearchingByNumber(request);
        if (!byNumber && (request.getFromDate() == null || request.getToDate() == null)) {
            throw new InvalidRequestException(
                    "fromDate and toDate are required unless a " + documentLabel() + " number is given");
        }
        if (!byNumber && request.getFromDate().isAfter(request.getToDate())) {
            throw new InvalidRequestException("fromDate must not be after toDate");
        }

        List<T> entries = repository.findAll(
                PassEntrySpecification.from(request), PassEntryRepository.DEFAULT_SORT);

        Map<Integer, String> truckNames = truckNames(request.getCompanyRefId());
        Map<Integer, String> driverNames = driverNames(request.getCompanyRefId());
        Map<Integer, String> rtiNumbers = rtiNumbers(request.getCompanyRefId());

        List<PassEntryListItemDto> items = entries.stream()
                .map(entry -> PassEntryListItemDto.builder()
                        .id(entry.getId())
                        .cNumber(entry.getCNumber())
                        .cNumberDisplay(entry.getCNumberDisplay())
                        .saleDate(toLocalDate(entry))
                        .truckRefId(entry.getTruckRefid())
                        .truckName(truckNames.get(entry.getTruckRefid()))
                        .driverRefId(entry.getDriverRefId())
                        .driverName(driverNames.get(entry.getDriverRefId()))
                        .rtiRefId(entry.getRtiRefId())
                        .rtiNumber(rtiNumbers.get(entry.getRtiRefId()))
                        .enterLink(entry.getEnterLink())
                        .exitLink(entry.getExitLink())
                        .amount(toDouble(entry.getAmount()))
                        .remarks(entry.getRemarks())
                        .filePath(entry.getFilePath())
                        .build())
                .toList();

        double total = items.stream()
                .mapToDouble(item -> item.getAmount() == null ? 0d : item.getAmount())
                .sum();

        return PassEntryListResponse.builder()
                .items(items)
                .entriesTotal(round2(total))
                .build();
    }

    // ------------------------------------------------------------- numbering

    @Override
    @Transactional(readOnly = true)
    public String nextNumber(Integer companyRefId) {
        requireCompany(companyRefId);
        Integer next = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = ? AND SequenceName = ?",
                Integer.class, companyRefId, sequenceName());

        int value = next == null ? 1 : next;
        return numberPrefix() + String.format("%0" + NUMBER_DIGITS + "d", value);
    }

    // ------------------------------------------------------------------ save

    @Override
    @Transactional
    public PassEntryDetailDto save(PassEntrySaveRequest request, String username) {
        requireCompany(request.getCompanyRefId());
        requireLookupsExist(request);

        String payload = buildStoredProcedurePayload(request);

        // Bound parameters. The legacy call built "Exec [SP_...] '" + json + "',"
        // + comid after running Replace("'", "") over the payload, which both
        // corrupted legitimate text and left the statement rewritable.
        Map<String, Object> result = jdbcTemplate.queryForMap(
                "EXEC [" + storedProcedureName() + "] ?, ?", payload, request.getCompanyRefId());

        Integer resultCode = asInteger(result.get("Result"));
        if (resultCode == null || resultCode != 1) {
            String message = String.valueOf(
                    result.getOrDefault("msg", "The " + documentLabel() + " was not saved"));
            logger.warn("{} rejected the entry: {}", storedProcedureName(), message);
            throw new InvalidRequestException(message);
        }

        Integer savedId = asInteger(result.get("id"));
        if (savedId == null || savedId == 0) {
            savedId = request.getId();
        }
        return getForEdit(savedId, null, request.getCompanyRefId());
    }

    /**
     * Builds the JSON array the procedure reads with OPENJSON.
     *
     * Jackson writes it, so quotes and backslashes in remarks are escaped rather
     * than deleted. {@code boundindex} is the procedure's {@code SNo}, which it
     * orders its work queue by.
     */
    private String buildStoredProcedurePayload(PassEntrySaveRequest request) {
        ObjectNode master = objectMapper.createObjectNode();
        master.put("boundindex", 1);
        master.put("Id", request.getId() == null ? 0 : request.getId());
        master.put("CompanyRefId", request.getCompanyRefId());
        // The procedure compares these against 0 before validating them, and
        // treats 0 as "not supplied". A null would fail its int conversion.
        master.put("UserRefId", 0);
        master.put("EmployeeRefId", request.getEmployeeRefId() == null ? 0 : request.getEmployeeRefId());
        master.put("TruckRefid", request.getTruckRefId());
        master.put("DriverRefId", request.getDriverRefId());
        master.put("RTIRefId", request.getRtiRefId());
        master.put("SaleDate", request.getSaleDate().toString());
        master.put("Amount", request.getAmount() == null ? 0d : request.getAmount());
        master.put("Remarks", blankIfNull(request.getRemarks()));
        // The screens file documents through the attachment API, which owns the
        // folder and the FilePath column; the procedure gets it unchanged.
        master.put("FilePath", "");
        // Assigned by the procedure from SequenceNoMaster on insert, and left
        // untouched on update - it deliberately does not re-number an edit.
        master.put("CNumberDisplay", "");
        master.put("CNumber", 0);
        master.put("EnterLink", blankIfNull(request.getEnterLink()));
        master.put("ExitLink", blankIfNull(request.getExitLink()));

        ArrayNode rows = objectMapper.createArrayNode();
        rows.add(master);
        return rows.toString();
    }

    /**
     * Checks the three lookups before the procedure does.
     *
     * Both procedures validate them too, but report failure by concatenating an
     * int onto a string ({@code 'Truck Not Found Issue id' + @TruckRefid}),
     * which raises a SQL conversion error instead of returning the message. The
     * caller would see a type-conversion failure rather than "Truck not found".
     */
    private void requireLookupsExist(PassEntrySaveRequest request) {
        Integer companyRefId = request.getCompanyRefId();

        truckMasterRepository.findById(request.getTruckRefId())
                .filter(truck -> companyRefId.equals(truck.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "Truck " + request.getTruckRefId() + " was not found"));

        driverMasterRepository.findById(request.getDriverRefId())
                .filter(driver -> companyRefId.equals(driver.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "Driver " + request.getDriverRefId() + " was not found"));

        rtiMasterRepository.findById(request.getRtiRefId())
                .filter(rti -> companyRefId.equals(rti.getCompanyRefId()))
                .orElseThrow(() -> new InvalidRequestException(
                        "RTI " + request.getRtiRefId() + " was not found"));
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public PassEntryDetailDto getForEdit(Integer id, Integer documentNumber, Integer companyRefId) {
        requireCompany(companyRefId);

        Integer resolvedId = id;
        if (documentNumber != null && documentNumber != 0) {
            resolvedId = repository.findIdsByCNumber(companyRefId, documentNumber).stream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No " + documentLabel() + " numbered " + documentNumber));
        }
        if (resolvedId == null || resolvedId == 0) {
            throw new InvalidRequestException("Either id or the document number is required");
        }

        T entry = repository.findByIdAndCompanyRefIdAndActive(resolvedId, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException(
                        capitalise(documentLabel()) + " " + id + " was not found"));

        return toDetail(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PassEntryDetailDto getForPrint(Integer id, Integer companyRefId) {
        return getForEdit(id, null, companyRefId);
    }

    private PassEntryDetailDto toDetail(T entry) {
        return PassEntryDetailDto.builder()
                .id(entry.getId())
                .companyRefId(entry.getCompanyRefId())
                .cNumber(entry.getCNumber())
                .cNumberDisplay(entry.getCNumberDisplay())
                .saleDate(toLocalDate(entry))
                .truckRefId(entry.getTruckRefid())
                .truckName(truckName(entry.getTruckRefid()))
                .driverRefId(entry.getDriverRefId())
                .driverName(driverName(entry.getDriverRefId()))
                .rtiRefId(entry.getRtiRefId())
                .rtiNumber(rtiNumber(entry.getRtiRefId()))
                .employeeRefId(entry.getEmployeeRefId())
                .enterLink(entry.getEnterLink())
                .exitLink(entry.getExitLink())
                .amount(toDouble(entry.getAmount()))
                .remarks(entry.getRemarks())
                .filePath(entry.getFilePath())
                .createdDate(entry.getCreatedDate())
                .modifiedDate(entry.getModifiedDate())
                .build();
    }

    // ---------------------------------------------------------------- delete

    @Override
    @Transactional
    public void delete(Integer id, Integer companyRefId, String username) {
        requireCompany(companyRefId);
        int updated = repository.softDelete(id, companyRefId, username);
        if (updated == 0) {
            throw new EntityNotFoundException(capitalise(documentLabel()) + " " + id + " was not found");
        }
    }

    // --------------------------------------------------------------- lookups

    @Override
    @Transactional(readOnly = true)
    public List<RtiOptionDto> rtiOptions(Integer companyRefId) {
        requireCompany(companyRefId);
        return rtiMasterRepository.findByCompanyRefIdAndActive(companyRefId, ACTIVE).stream()
                .map(rti -> RtiOptionDto.builder()
                        .id(rti.getId())
                        .rtiNumber(rti.getCNumberDisplay())
                        .build())
                .toList();
    }

    // --------------------------------------------------------------- helpers

    private Map<Integer, String> truckNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (TruckMaster truck : truckMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(truck.getId(), truck.getTruckName());
        }
        return names;
    }

    private Map<Integer, String> driverNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (DriverMaster driver : driverMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(driver.getId(), driver.getDriverName());
        }
        return names;
    }

    private Map<Integer, String> rtiNumbers(Integer companyRefId) {
        Map<Integer, String> numbers = new HashMap<>();
        for (RTIMaster rti : rtiMasterRepository.findByCompanyRefId(companyRefId)) {
            numbers.put(rti.getId(), rti.getCNumberDisplay());
        }
        return numbers;
    }

    private String truckName(Integer truckRefId) {
        if (truckRefId == null) {
            return null;
        }
        return truckMasterRepository.findById(truckRefId).map(TruckMaster::getTruckName).orElse(null);
    }

    private String driverName(Integer driverRefId) {
        if (driverRefId == null) {
            return null;
        }
        return driverMasterRepository.findById(driverRefId).map(DriverMaster::getDriverName).orElse(null);
    }

    private String rtiNumber(Integer rtiRefId) {
        if (rtiRefId == null) {
            return null;
        }
        return rtiMasterRepository.findById(rtiRefId).map(RTIMaster::getCNumberDisplay).orElse(null);
    }

    private LocalDate toLocalDate(T entry) {
        return entry.getSaleDate() == null ? null : entry.getSaleDate().toLocalDate();
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
     * Widens a value from the SQL Server {@code real} column, rounding away the
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

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }

    private String capitalise(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}

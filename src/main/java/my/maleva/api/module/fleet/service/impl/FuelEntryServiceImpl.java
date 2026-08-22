package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.FuelEntryDetailDto;
import my.maleva.api.module.fleet.dto.FuelEntryListItemDto;
import my.maleva.api.module.fleet.dto.FuelEntryListResponse;
import my.maleva.api.module.fleet.dto.request.FuelEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.FuelEntrySearchRequest;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.FuelEntry;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.FuelEntryRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.FuelEntryService;
import my.maleva.api.module.fleet.service.FuelVarianceCalculator;
import my.maleva.api.module.fleet.specification.FuelEntrySpecification;
import my.maleva.api.module.gps.dto.GpsFuelMatchDto;
import my.maleva.api.module.gps.service.FuelGpsMatchService;
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
 * Fuel entry business logic.
 *
 * Differences from the legacy FuelEntryServices, all deliberate:
 * <ul>
 *   <li>every query is parameterised - the legacy WHERE clauses and the
 *       SP_FuelEntry call were built by string concatenation;</li>
 *   <li>the derived amounts are recomputed server side instead of being trusted
 *       from the browser;</li>
 *   <li>the two header totals are returned once, not stamped onto every row;</li>
 *   <li>the GPS filling comes from {@link FuelGpsMatchService}, so one filling
 *       can only be claimed by one entry.</li>
 * </ul>
 */
@Service
public class FuelEntryServiceImpl implements FuelEntryService {

    private static final Logger logger = LoggerFactory.getLogger(FuelEntryServiceImpl.class);

    private static final Integer ACTIVE = 1;
    private static final String FUEL_NUMBER_PREFIX = "FE";
    private static final int FUEL_NUMBER_DIGITS = 9;
    private static final String SEQUENCE_NAME = "FuelEntry";

    private final FuelEntryRepository fuelEntryRepository;
    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final FuelGpsMatchService fuelGpsMatchService;
    private final FuelVarianceCalculator calculator;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public FuelEntryServiceImpl(FuelEntryRepository fuelEntryRepository,
                                TruckMasterRepository truckMasterRepository,
                                DriverMasterRepository driverMasterRepository,
                                FuelGpsMatchService fuelGpsMatchService,
                                FuelVarianceCalculator calculator,
                                JdbcTemplate jdbcTemplate,
                                ObjectMapper objectMapper) {
        this.fuelEntryRepository = fuelEntryRepository;
        this.truckMasterRepository = truckMasterRepository;
        this.driverMasterRepository = driverMasterRepository;
        this.fuelGpsMatchService = fuelGpsMatchService;
        this.calculator = calculator;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------- list

    @Override
    @Transactional(readOnly = true)
    public FuelEntryListResponse search(FuelEntrySearchRequest request) {
        requireCompany(request.getCompanyRefId());
        boolean byNumber = request.getSearch() != null && !request.getSearch().isBlank();
        if (!byNumber && (request.getFromDate() == null || request.getToDate() == null)) {
            throw new InvalidRequestException("fromDate and toDate are required unless a fuel number is given");
        }
        if (!byNumber && request.getFromDate().isAfter(request.getToDate())) {
            throw new InvalidRequestException("fromDate must not be after toDate");
        }

        List<FuelEntry> entries = fuelEntryRepository.findAll(
                FuelEntrySpecification.from(request), FuelEntryRepository.DEFAULT_SORT);

        Map<Integer, String> truckNames = truckNames(request.getCompanyRefId());
        Map<Integer, String> driverNames = driverNames(request.getCompanyRefId());

        List<FuelEntryListItemDto> items = entries.stream()
                .map(entry -> toListItem(entry, truckNames, driverNames))
                .toList();

        double entriesTotal = items.stream()
                .mapToDouble(item -> item.getAAmount() == null ? 0d : item.getAAmount())
                .sum();

        return FuelEntryListResponse.builder()
                .items(items)
                .entriesTotal(round2(entriesTotal))
                .paymentVoucherTotal(byNumber ? null
                        : fuelPaymentVoucherTotal(request.getCompanyRefId(),
                                request.getFromDate(), request.getToDate()))
                .subsidyTotal(byNumber ? null
                        : subsidyTotal(request.getFromDate(), request.getToDate()))
                .build();
    }

    private FuelEntryListItemDto toListItem(FuelEntry entry,
                                            Map<Integer, String> truckNames,
                                            Map<Integer, String> driverNames) {
        FuelVarianceCalculator.FuelVariance variance = calculator.calculate(
                toDouble(entry.getAliter()), toDouble(entry.getAAmount()),
                toDouble(entry.getPliter()), toDouble(entry.getGliter()),
                toDouble(entry.getPRate()));

        return FuelEntryListItemDto.builder()
                .id(entry.getId())
                .cNumber(entry.getCNumber())
                .cNumberDisplay(entry.getCNumberDisplay())
                .saleDate(entry.getSaleDate() == null ? null : entry.getSaleDate().toLocalDate())
                .truckRefId(entry.getTruckRefid())
                .truckName(truckNames.get(entry.getTruckRefid()))
                .driverRefId(entry.getDriverRefId())
                .driverName(driverNames.get(entry.getDriverRefId()))
                .remarks(entry.getRemarks())
                .filePath(entry.getFilePath())
                .pRate(toDouble(entry.getPRate()))
                .aliter(toDouble(entry.getAliter()))
                .aAmount(variance.getAAmount())
                .pliter(toDouble(entry.getPliter()))
                .pAmount(variance.getPAmount())
                .gliter(toDouble(entry.getGliter()))
                .gAmount(variance.getGAmount())
                .diffLiter(variance.getDgLiter())
                .diffAmount(variance.getDgAmount())
                .adverse(variance.isPumpOverGps())
                .fStatus(entry.getFStatus())
                .build();
    }

    // -------------------------------------------------------------- numbering

    @Override
    @Transactional(readOnly = true)
    public String nextFuelNumber(Integer companyRefId) {
        requireCompany(companyRefId);
        Integer next = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(SequenceNo) + 1, 1) FROM SequenceNoMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = ? AND SequenceName = ?",
                Integer.class, companyRefId, SEQUENCE_NAME);

        int value = next == null ? 1 : next;
        return FUEL_NUMBER_PREFIX + String.format("%0" + FUEL_NUMBER_DIGITS + "d", value);
    }

    // ------------------------------------------------------------------ save

    @Override
    @Transactional
    public FuelEntryDetailDto save(FuelEntrySaveRequest request, String username) {
        requireCompany(request.getCompanyRefId());

        // Recomputed here, never taken from the client.
        FuelVarianceCalculator.FuelVariance variance = calculator.calculate(
                request.getAliter(), request.getAAmount(),
                request.getPliter(), request.getGliter(), request.getPRate());

        String payload = buildStoredProcedurePayload(request, variance, username);

        // Bound parameters. The legacy call pasted the JSON straight into the
        // statement text after stripping apostrophes, so a remark containing one
        // could rewrite the command.
        Map<String, Object> result = jdbcTemplate.queryForMap(
                "EXEC [SP_FuelEntry] ?, ?", payload, request.getCompanyRefId());

        Integer resultCode = asInteger(result.get("Result"));
        if (resultCode == null || resultCode != 1) {
            String message = String.valueOf(result.getOrDefault("Msg", "Fuel entry was not saved"));
            logger.warn("SP_FuelEntry rejected the entry: {}", message);
            throw new InvalidRequestException(message);
        }

        Integer savedId = asInteger(result.get("Id"));
        if (savedId == null || savedId == 0) {
            savedId = request.getId();
        }

        // Re-assign the whole truck-day and store the result. Adding an entry
        // changes which filling every sibling entry should hold, so this has to
        // run over the day rather than over the one row that was just saved.
        fuelGpsMatchService.persistAutoMatches(
                request.getCompanyRefId(), request.getTruckRefId(), request.getSaleDate());

        return getForEdit(savedId, null, request.getCompanyRefId());
    }

    /**
     * Builds the JSON array SP_FuelEntry expects.
     *
     * The procedure takes the whole row set as one JSON string - a shape carried
     * over from the jqx screen, which posted an array even for a single entry.
     * Jackson builds it here so that quotes and backslashes are escaped properly
     * instead of being stripped out with string replaces.
     */
    private String buildStoredProcedurePayload(FuelEntrySaveRequest request,
                                               FuelVarianceCalculator.FuelVariance variance,
                                               String username) {
        ObjectNode row = objectMapper.createObjectNode();
        row.put("Id", request.getId() == null ? 0 : request.getId());
        row.put("CompanyRefId", request.getCompanyRefId());
        row.putNull("UserRefId");
        row.put("EmployeeRefId", request.getEmployeeRefId() == null ? 0 : request.getEmployeeRefId());
        row.put("TruckRefid", request.getTruckRefId());
        row.put("DriverRefId", request.getDriverRefId() == null ? 0 : request.getDriverRefId());
        row.put("SaleDate", request.getSaleDate().toString());
        row.put("CNumberDisplay", 0);
        row.put("CNumber", 0);
        row.put("Remarks", request.getRemarks() == null ? "" : request.getRemarks());
        row.put("FilePath", request.getFilePath() == null ? "" : request.getFilePath());
        row.put("FStatus", request.getFStatus() == null ? 0 : request.getFStatus());

        row.put("Aliter", nullSafe(request.getAliter()));
        row.put("AAmount", variance.getAAmount());
        row.put("Pliter", nullSafe(request.getPliter()));
        row.put("PRate", nullSafe(request.getPRate()));
        row.put("PAmount", variance.getPAmount());
        row.put("Gliter", nullSafe(request.getGliter()));
        row.put("GAmount", variance.getGAmount());
        row.put("DPliter", variance.getDpLiter());
        row.put("DPAmount", variance.getDpAmount());
        row.put("DGliter", variance.getDgLiter());
        row.put("DGAmount", variance.getDgAmount());
        row.put("Created_By", username);
        row.put("Modified_By", username);

        ArrayNode rows = objectMapper.createArrayNode();
        rows.add(row);
        return rows.toString();
    }

    // ------------------------------------------------------------------ read

    @Override
    @Transactional(readOnly = true)
    public FuelEntryDetailDto getForEdit(Integer id, Integer fuelNumber, Integer companyRefId) {
        requireCompany(companyRefId);

        Integer lookupId = id;
        if (fuelNumber != null && fuelNumber != 0) {
            lookupId = fuelEntryRepository.findIdsByCNumber(companyRefId, fuelNumber)
                    .stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No fuel entry with number " + fuelNumber));
        }
        if (lookupId == null || lookupId == 0) {
            throw new InvalidRequestException("Either id or fuelNumber must be supplied");
        }

        final Integer resolvedId = lookupId;
        FuelEntry entry = fuelEntryRepository
                .findByIdAndCompanyRefIdAndActive(resolvedId, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Fuel entry " + resolvedId + " was not found"));

        return toDetail(entry, true);
    }

    @Override
    @Transactional(readOnly = true)
    public FuelEntryDetailDto getForPrint(Integer id, Integer companyRefId) {
        requireCompany(companyRefId);
        FuelEntry entry = fuelEntryRepository
                .findByIdAndCompanyRefIdAndActive(id, companyRefId, ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Fuel entry " + id + " was not found"));
        return toDetail(entry, false);
    }

    private FuelEntryDetailDto toDetail(FuelEntry entry, boolean withGpsMatch) {
        FuelVarianceCalculator.FuelVariance variance = calculator.calculate(
                toDouble(entry.getAliter()), toDouble(entry.getAAmount()),
                toDouble(entry.getPliter()), toDouble(entry.getGliter()),
                toDouble(entry.getPRate()));

        GpsFuelMatchDto match = null;
        if (withGpsMatch && entry.getTruckRefid() != null && entry.getSaleDate() != null) {
            LocalDate day = entry.getSaleDate().toLocalDate();
            // Matching runs for the whole truck-day, so an entry only receives a
            // filling that no sibling entry has taken.
            match = fuelGpsMatchService
                    .matchForTruckOnDay(entry.getCompanyRefId(), entry.getTruckRefid(), day)
                    .stream()
                    .filter(candidate -> entry.getId().equals(candidate.getFuelEntryId()))
                    .findFirst()
                    .orElse(null);
        }

        return FuelEntryDetailDto.builder()
                .id(entry.getId())
                .companyRefId(entry.getCompanyRefId())
                .cNumberDisplay(entry.getCNumberDisplay())
                .cNumber(entry.getCNumber())
                .saleDate(entry.getSaleDate() == null ? null : entry.getSaleDate().toLocalDate())
                .truckRefId(entry.getTruckRefid())
                .truckName(truckName(entry.getTruckRefid()))
                .driverRefId(entry.getDriverRefId())
                .driverName(driverName(entry.getDriverRefId()))
                .aliter(toDouble(entry.getAliter()))
                .aAmount(variance.getAAmount())
                .pliter(toDouble(entry.getPliter()))
                .pAmount(variance.getPAmount())
                .gliter(toDouble(entry.getGliter()))
                .gAmount(variance.getGAmount())
                .pRate(toDouble(entry.getPRate()))
                .dpLiter(variance.getDpLiter())
                .dpAmount(variance.getDpAmount())
                .dgLiter(variance.getDgLiter())
                .dgAmount(variance.getDgAmount())
                .pumpOverGps(variance.isPumpOverGps())
                .pumpOverActual(variance.isPumpOverActual())
                .remarks(entry.getRemarks())
                .filePath(entry.getFilePath())
                .fStatus(entry.getFStatus())
                .gpsMatch(match)
                .build();
    }

    // ---------------------------------------------------------------- delete

    @Override
    @Transactional
    public void delete(Integer id, Integer companyRefId, boolean mobile, String username) {
        requireCompany(companyRefId);
        // mobile=true reproduces the legacy guard: a driver-app row may only be
        // removed through the driver-app path.
        int updated = fuelEntryRepository.softDelete(id, companyRefId, mobile ? 1 : null, username);
        if (updated == 0) {
            throw new EntityNotFoundException(
                    "Fuel entry " + id + " was not found, or is not deletable from here");
        }
    }

    // --------------------------------------------------------------- helpers

    private Double fuelPaymentVoucherTotal(Integer companyRefId, LocalDate from, LocalDate to) {
        Double total = jdbcTemplate.queryForObject(
                "SELECT SUM(Amount) FROM PaymentVoucherMaster WITH (NOLOCK) "
                        + "WHERE CompanyRefId = ? AND Description = 'FUEL' "
                        + "AND PaymentVoucherDate >= ? AND PaymentVoucherDate < ?",
                Double.class, companyRefId, from, to.plusDays(1));
        return total == null ? 0d : round2(total);
    }

    private Double subsidyTotal(LocalDate from, LocalDate to) {
        Double total = jdbcTemplate.queryForObject(
                "SELECT SUM(Amount) FROM SubcdiyEntry WITH (NOLOCK) "
                        + "WHERE EntryDate >= ? AND EntryDate < ?",
                Double.class, from, to.plusDays(1));
        return total == null ? 0d : round2(total);
    }

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

    private void requireCompany(Integer companyRefId) {
        if (companyRefId == null || companyRefId == 0) {
            throw new InvalidRequestException("companyRefId is required");
        }
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * Widens a value from a SQL Server {@code real} column.
     *
     * real is 4-byte floating point, so widening to double exposes the noise:
     * a stored rate of 4.67 reads back as 4.670000076293945 and a stored 220.02
     * as 220.02200317382812. Rounding here keeps that out of the API and off the
     * screen; the stored value is untouched.
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

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }
}

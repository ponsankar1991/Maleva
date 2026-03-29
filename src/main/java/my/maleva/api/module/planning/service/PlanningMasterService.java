package my.maleva.api.module.planning.service;

import my.maleva.api.module.planning.dto.PlanningDetailsDto;
import my.maleva.api.module.planning.dto.PlanningMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.planning.mapper.PlanningMasterMapper;
import my.maleva.api.module.planning.mapper.PlanningF5ViewMapper;
import my.maleva.api.module.planning.mapper.PlanningSearchResultMapper;
import my.maleva.api.module.planning.entity.PlanningDetails;
import my.maleva.api.module.planning.entity.PlanningMaster;
import my.maleva.api.module.planning.repository.PlanningDetailsRepository;
import my.maleva.api.module.planning.repository.PlanningMasterRepository;
import my.maleva.api.module.planning.dto.PlanningF5View;
import my.maleva.api.module.planning.dto.request.PlanningF5RequestDto;
import my.maleva.api.module.planning.dto.request.PLANINGSearchRequestDto;
import my.maleva.api.module.planning.dto.PlanningMasterViewModel;
import my.maleva.api.module.planning.dto.PlanningDetailsModel;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanningMasterService {

    private final PlanningMasterRepository planningMasterRepository;
    private final PlanningDetailsRepository planningDetailsRepository;
    private final PlanningMasterMapper planningMasterMapper;
    private final PlanningF5ViewMapper planningF5ViewMapper;
    private final PlanningSearchResultMapper planningSearchResultMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PlanningMasterService(
            PlanningMasterRepository planningMasterRepository,
            PlanningDetailsRepository planningDetailsRepository,
            PlanningMasterMapper planningMasterMapper,
            PlanningF5ViewMapper planningF5ViewMapper,
            PlanningSearchResultMapper planningSearchResultMapper,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.planningMasterRepository = planningMasterRepository;
        this.planningDetailsRepository = planningDetailsRepository;
        this.planningMasterMapper = planningMasterMapper;
        this.planningF5ViewMapper = planningF5ViewMapper;
        this.planningSearchResultMapper = planningSearchResultMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Get all planning records by company (non-deleted)
     */
    public List<PlanningMasterDto> listAll() {
        return planningMasterRepository.findByCompanyRefIdAndActiveNot(0, 2)
                .stream()
                .map(planningMasterMapper::toDto)
                .toList();
    }

    /**
     * Get planning record by ID
     */
    public PlanningMasterDto getById(Integer id) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        PlanningMasterDto dto = planningMasterMapper.toDto(planning);

        // Load related details
        List<PlanningDetails> details = planningDetailsRepository.findByPlanningMasterRefIdSorted(id);
        dto.setPlanningDetails(details.stream()
                .map(d -> PlanningDetailsDto.builder()
                        .id(d.getId())
                        .planningMasterRefId(d.getPlanningMasterRefId())
                        .saleOrderMasterRefId(d.getSaleOrderMasterRefId())
                        .truckRefId(d.getTruckRefId())
                        .remarks(d.getRemarks())
                        .createdDate(d.getCreatedDate())
                        .modifiedDate(d.getModifiedDate())
                        .originD(d.getOriginD())
                        .destinationD(d.getDestinationD())
                        .pickupDateD(d.getPickupDateD())
                        .deliveryDateD(d.getDeliveryDateD())
                        .sortBy(d.getSortBy())
                        .truckNameD(d.getTruckNameD())
                        .driverNameD(d.getDriverNameD())
                        .pickupTimeList(d.getPickupTimeList())
                        .pickupQuantityList(d.getPickupQuantityList())
                        .deliveryQuantityList(d.getDeliveryQuantityList())
                        .deliveryTimeList(d.getDeliveryTimeList())
                        .build())
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Create new planning record with details (Implements SP_PLANINGMaster logic)
     */
    @Transactional
    public PlanningMasterDto create(PlanningMasterDto dto) {
        if (dto.getCompanyRefId() == null) {
            throw new InvalidRequestException("Company reference ID is required");
        }

        LocalDateTime now = LocalDateTime.now();

        // Create master record
        PlanningMaster planning = PlanningMaster.builder()
                .companyRefId(dto.getCompanyRefId())
                .userRefId(dto.getUserRefId())
                .employeeRefId(dto.getEmployeeRefId())
                .lastEmployeeRefId(dto.getLastEmployeeRefId())
                .saleDate(dto.getSaleDate() != null ? dto.getSaleDate() : now)
                .fDate(dto.getFDate() != null ? dto.getFDate() : now)
                .tDate(dto.getTDate() != null ? dto.getTDate() : now)
                .cNumberDisplay(dto.getCNumberDisplay())
                .cNumber(dto.getCNumber() != null ? dto.getCNumber() : 0)
                .remarks(dto.getRemarks())
                .search(dto.getSearch())
                .active(1) // Active by default
                .createdDate(now)
                .createdBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM")
                .modifiedDate(now)
                .modifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM")
                .build();

        PlanningMaster savedMaster = planningMasterRepository.save(planning);

        // Create detail records if provided
        if (dto.getPlanningDetails() != null && !dto.getPlanningDetails().isEmpty()) {
            List<PlanningDetails> details = new ArrayList<>();
            for (PlanningDetailsDto detailDto : dto.getPlanningDetails()) {
                PlanningDetails detail = PlanningDetails.builder()
                        .planningMasterRefId(savedMaster.getId())
                        .saleOrderMasterRefId(detailDto.getSaleOrderMasterRefId())
                        .truckRefId(detailDto.getTruckRefId())
                        .remarks(detailDto.getRemarks())
                        .originD(detailDto.getOriginD())
                        .destinationD(detailDto.getDestinationD())
                        .pickupDateD(detailDto.getPickupDateD())
                        .deliveryDateD(detailDto.getDeliveryDateD())
                        .sortBy(detailDto.getSortBy() != null ? detailDto.getSortBy() : 0)
                        .truckNameD(detailDto.getTruckNameD())
                        .driverNameD(detailDto.getDriverNameD())
                        .pickupTimeList(detailDto.getPickupTimeList())
                        .pickupQuantityList(detailDto.getPickupQuantityList())
                        .deliveryQuantityList(detailDto.getDeliveryQuantityList())
                        .deliveryTimeList(detailDto.getDeliveryTimeList())
                        .createdDate(now)
                        .modifiedDate(now)
                        .build();
                details.add(detail);
            }
            planningDetailsRepository.saveAll(details);
        }

        return getById(savedMaster.getId());
    }

    /**
     * Update planning record with details (Implements SP_PLANINGMaster logic)
     */
    @Transactional
    public PlanningMasterDto update(Integer id, PlanningMasterDto dto) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        LocalDateTime now = LocalDateTime.now();

        // Update master record
        planning.setLastEmployeeRefId(dto.getLastEmployeeRefId() != null ? dto.getLastEmployeeRefId() : planning.getLastEmployeeRefId());
        planning.setUserRefId(dto.getUserRefId() != null ? dto.getUserRefId() : planning.getUserRefId());
        planning.setEmployeeRefId(dto.getEmployeeRefId() != null ? dto.getEmployeeRefId() : planning.getEmployeeRefId());
        planning.setFDate(dto.getFDate() != null ? dto.getFDate() : planning.getFDate());
        planning.setTDate(dto.getTDate() != null ? dto.getTDate() : planning.getTDate());
        planning.setSaleDate(dto.getSaleDate() != null ? dto.getSaleDate() : planning.getSaleDate());
        planning.setSearch(dto.getSearch() != null ? dto.getSearch() : planning.getSearch());
        planning.setRemarks(dto.getRemarks() != null ? dto.getRemarks() : planning.getRemarks());
        planning.setModifiedDate(now);
        planning.setModifiedBy(dto.getModifiedBy() != null ? dto.getModifiedBy() : "SYSTEM");

        planningMasterRepository.save(planning);

        // Delete and recreate details if provided
        if (dto.getPlanningDetails() != null) {
            planningDetailsRepository.deleteByPlanningMasterRefId(id);

            List<PlanningDetails> details = new ArrayList<>();
            for (PlanningDetailsDto detailDto : dto.getPlanningDetails()) {
                PlanningDetails detail = PlanningDetails.builder()
                        .planningMasterRefId(id)
                        .saleOrderMasterRefId(detailDto.getSaleOrderMasterRefId())
                        .truckRefId(detailDto.getTruckRefId())
                        .remarks(detailDto.getRemarks())
                        .originD(detailDto.getOriginD())
                        .destinationD(detailDto.getDestinationD())
                        .pickupDateD(detailDto.getPickupDateD())
                        .deliveryDateD(detailDto.getDeliveryDateD())
                        .sortBy(detailDto.getSortBy() != null ? detailDto.getSortBy() : 0)
                        .truckNameD(detailDto.getTruckNameD())
                        .driverNameD(detailDto.getDriverNameD())
                        .pickupTimeList(detailDto.getPickupTimeList())
                        .pickupQuantityList(detailDto.getPickupQuantityList())
                        .deliveryQuantityList(detailDto.getDeliveryQuantityList())
                        .deliveryTimeList(detailDto.getDeliveryTimeList())
                        .createdDate(now)
                        .modifiedDate(now)
                        .build();
                details.add(detail);
            }
            planningDetailsRepository.saveAll(details);
        }

        return getById(id);
    }

    /**
     * Delete planning record
     */
    @Transactional
    public void delete(Integer id) {
        PlanningMaster planning = planningMasterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planning Master not found: " + id));

        // Delete related details first
        planningDetailsRepository.deleteByPlanningMasterRefId(id);

        // Delete master record
        planningMasterRepository.delete(planning);
    }

    /**
     * Get planning records by company and date range
     */
    public List<PlanningMasterDto> getByCompanyAndDateRange(Integer companyId, LocalDateTime fromDate, LocalDateTime toDate) {
        return planningMasterRepository.findByCompanyAndDateRange(companyId, fromDate, toDate)
                .stream()
                .map(planningMasterMapper::toDto)
                .toList();
    }

    /**
     * Search planning records
     */
    public List<PlanningMasterDto> search(Integer companyId, String keyword) {
        return planningMasterRepository.searchByCompanyAndKeyword(companyId, keyword)
                .stream()
                .map(planningMasterMapper::toDto)
                .toList();
    }

    /**
     * Get planning records by employee
     */
    public List<PlanningMasterDto> getByCompanyAndEmployee(Integer companyId, Integer employeeId) {
        return planningMasterRepository.findByCompanyRefIdAndEmployeeRefIdAndActiveNot(companyId, employeeId, 2)
                .stream()
                .map(planningMasterMapper::toDto)
                .toList();
    }

    /**
     * SelectPLANING - Complex filtered search equivalent to .NET SelectPLANING method
     * Returns combined PlanningMaster and PlanningDetails data with dynamic filtering
     */
    public PlanningF5View selectPlanning(PlanningF5RequestDto filter) {
        // Build filter parameters
        Integer employeeId = filter.getEmployeeid() != null && filter.getEmployeeid() != 0 ? filter.getEmployeeid() : null;
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        // If search is provided, ignore date filters (as per .NET logic)
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            // When search is provided, we need to filter by CNumberDisplay
            // But since the repository query doesn't handle this, we'll handle it in service
            // For now, we'll use the date filter as fallback
            if (filter.getFromdate() != null && filter.getTodate() != null) {
                fromDate = filter.getFromdate().atStartOfDay();
                toDate = filter.getTodate().atTime(23, 59, 59);
            }
        } else {
            // Normal date filtering
            if (filter.getFromdate() != null && filter.getTodate() != null) {
                fromDate = filter.getFromdate().atStartOfDay();
                toDate = filter.getTodate().atTime(23, 59, 59);
            }
        }

        // Fetch planning masters
        List<PlanningMaster> masters = planningMasterRepository.findForSelectPlanning(
                filter.getComid(), employeeId, fromDate, toDate);

        // Apply search filter if provided (filter by CNumberDisplay)
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            masters = masters.stream()
                    .filter(m -> m.getCNumberDisplay() != null &&
                            m.getCNumberDisplay().equals(filter.getSearch().trim()))
                    .collect(Collectors.toList());
        }

        // Convert to ViewModels with all fields
        List<PlanningMasterViewModel> masterViewModels = masters.stream()
                .map(m -> {
                    String dateFormat = "dd/MM/yyyy";
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(dateFormat);
                    return PlanningMasterViewModel.builder()
                            .id(m.getId())
                            .sdId(m.getSdId())
                            .planningNo(m.getCNumber())
                            .planningNoDisplay(m.getCNumberDisplay())
                            .fDate(m.getFDate() != null ? m.getFDate().format(formatter) : "01/01/1900")
                            .tDate(m.getTDate() != null ? m.getTDate().format(formatter) : "01/01/1900")
                            .sFDate(m.getFDate() != null ? m.getFDate().format(formatter) : "01/01/1900")
                            .sTDate(m.getTDate() != null ? m.getTDate().format(formatter) : "01/01/1900")
                            .saleDate(m.getSaleDate() != null ? m.getSaleDate().format(formatter) : "01/01/1900")
                            .sSaleDate(m.getSaleDate() != null ? m.getSaleDate().format(formatter) : "01/01/1900")
                            .planningDate(m.getSaleDate() != null ? m.getSaleDate().format(formatter) : "01/01/1900")
                            .cNumberDisplay(m.getCNumberDisplay())
                            .remarks(m.getRemarks())
                            .active(m.getActive())
                            .createdDate(m.getCreatedDate() != null ? m.getCreatedDate().format(formatter) : "01/01/1900")
                            .createdBy(m.getCreatedBy())
                            .modifiedDate(m.getModifiedDate() != null ? m.getModifiedDate().format(formatter) : "01/01/1900")
                            .modifiedBy(m.getModifiedBy())
                            .build();
                })
                .toList();

        // Fetch planning details with joins
        List<Object[]> detailsRaw = planningDetailsRepository.findDetailsForSelectPlanning(
                filter.getComid(), employeeId, fromDate, toDate);

        // Apply search filter to details if needed
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            // Filter details based on masters that passed the search filter
            List<Integer> masterIds = masterViewModels.stream()
                    .map(PlanningMasterViewModel::getId)
                    .collect(Collectors.toList());
            detailsRaw = detailsRaw.stream()
                    .filter(row -> masterIds.contains((Integer) row[2])) // PLANINGMasterRefId is at index 2
                    .collect(Collectors.toList());
        }

        // Convert to ViewModels with comprehensive detail mapping using MapStruct
        List<PlanningDetailsModel> detailsViewModels = detailsRaw.stream()
                .map(planningSearchResultMapper::mapSelectPlanningResult)
                .toList();

        // Create and return the combined view
        return planningF5ViewMapper.createPlanningF5View(masterViewModels, detailsViewModels);
    }

    /**
     * PLANINGSearch - Search planning details by port codes and date range
     * Searches SaleOrderMaster with filters on Sport/Oport (ports)
     * Returns planning details ordered by pickup date
     * Equivalent to .NET PLANINGSearch method
     */
    public List<PlanningDetailsModel> planningSearch(PLANINGSearchRequestDto filter) {
        LocalDate fromDate = parseRequiredDate(filter.getFromdate(), "fromdate");
        LocalDate toDate = parseRequiredDate(filter.getTodate(), "todate");

        if (fromDate.isAfter(toDate)) {
            throw new InvalidRequestException("fromdate must be less than or equal to todate");
        }

        String search = filter.getSearch() != null ? filter.getSearch().trim() : null;
        boolean applyPortFilter = search != null && !search.isEmpty();

        // Parse search string into list of port codes (comma-separated)
        List<String> searchPorts = new java.util.ArrayList<>();
        if (applyPortFilter) {
            String[] ports = search.split(",");
            for (String port : ports) {
                String trimmedPort = port.trim();
                if (!trimmedPort.isEmpty()) {
                    searchPorts.add(trimmedPort);
                }
            }
        }

        // SQL IN () cannot be empty, so keep one dummy value when port filter is disabled
        // or when input resolves to no valid ports after trimming.
        if (searchPorts.isEmpty()) {
            searchPorts.add("__NO_PORT_FILTER__");
            applyPortFilter = false;
        }

        // Ensure employeeid is null or 0 for "all employees"
        Integer employeeId = parseOptionalInteger(filter.getEmployeeid(), "employeeid");
        if (employeeId != null && employeeId == 0) {
            employeeId = null;
        }

        StringBuilder sql = new StringBuilder("""
                SELECT
                    ISNULL(E.EmployeeName, '') as EmployeeName,
                    SM.PickupDate,
                    ISNULL(CONVERT(VARCHAR(26), SM.PickupDate, 20), '') as PickupDateD,
                    ISNULL(CONVERT(VARCHAR(26), SM.DeliveryDate, 20), '') as DeliveryDateD,
                    SM.Id,
                    SM.CNumberDisplay as JobNo,
                    ISNULL(CONVERT(VARCHAR(26), SM.PickupDate, 20), '') as SPickupDate,
                    ISNULL(CONVERT(VARCHAR(26), SM.DeliveryDate, 20), '') as SDeliveryDate,
                    SM.WareHouseEnterDate,
                    SM.WareHouseExitDate,
                    ISNULL(CONVERT(VARCHAR(26), SM.WareHouseEnterDate, 20), '') as SWareHouseEnterDate,
                    ISNULL(CONVERT(VARCHAR(26), SM.WareHouseExitDate, 20), '') as SWareHouseExitDate,
                    ISNULL(SM.WareHouseAddress, '') as WareHouseAddress,
                    ISNULL(SM.Origin, '') as Origin,
                    ISNULL(SM.Destination, '') as Destination,
                    ISNULL(SM.Origin, '') as OriginD,
                    ISNULL(SM.Destination, '') as DestinationD,
                    ISNULL(SM.Quantity, '') + '/' + ISNULL(SM.TotalWeight, '') as pkg,
                    COALESCE(NULLIF(SM.Loadingvesselname, ''), SM.Offvesselname, '') as VesselName,
                    CASE WHEN SM.SaleDate IS NULL THEN '' ELSE CONVERT(VARCHAR(10), SM.SaleDate, 103) END as JobDate,
                    ISNULL(C.CustomerName, '') as CustomerName,
                    CAST('' AS VARCHAR(200)) as TruckName,
                    0 as TruckRefid,
                    CAST('' AS VARCHAR(300)) as Remarks,
                    ISNULL(JS.Name, '') as JobStatus,
                    ISNULL(SM.PickupAddress, '') as PickupAddress,
                    ISNULL(SM.DeliveryAddress, '') as DeliveryAddress,
                    ISNULL(CONVERT(VARCHAR(26), SM.ETA, 20), '') as LETA,
                    ISNULL(CONVERT(VARCHAR(26), SM.OETA, 20), '') as OETA,
                    ISNULL(JT.Name, '') as JobName,
                    ISNULL(SM.AWBNo, '') as AWBNo,
                    ISNULL(SM.BLCopy, '') as BLCopy,
                    ISNULL(SM.SPort, '') as SPort,
                    ISNULL(SM.OPort, '') as OPort,
                    ISNULL(SM.truckSize, '') as truckSize,
                    ISNULL(SM.pickuptimelist, '') as pickuptimelist,
                    ISNULL(SM.pickupQuantitylist, '') as pickupQuantitylist,
                    ISNULL(SM.DeliveryQuantitylist, '') as DeliveryQuantitylist,
                    ISNULL(SM.Delivertimelist, '') as Delivertimelist,
                    0 as SDId,
                    0 as PLANINGMasterRefId,
                    0 as SaleOrderMasterRefId,
                    0 as SortBy,
                    CAST('' AS VARCHAR(200)) as TruckNameD,
                    CAST('' AS VARCHAR(200)) as DriverNameD
                FROM SaleOrderMaster SM WITH(NOLOCK)
                INNER JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId
                INNER JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = SM.JobMasterRefId
                LEFT JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = SM.JStatus
                LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = SM.EmployeeRefId
                WHERE SM.CompanyRefId = :companyId
                  AND SM.Active != 2
                  AND CAST(SM.PickupDate as DATE) BETWEEN :fromDate AND :toDate
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", filter.getComid())
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);

        if (employeeId != null) {
            sql.append(" AND SM.EmployeeRefId = :employeeId");
            params.addValue("employeeId", employeeId);
        }

        if (applyPortFilter) {
            sql.append(" AND (SM.SPort IN (:searchPorts) OR SM.OPort IN (:searchPorts))");
            params.addValue("searchPorts", searchPorts);
        }

        sql.append(" ORDER BY SM.PickupDate ASC");

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) ->
                PlanningDetailsModel.builder()
                        .employeeName(getNullableString(rs, "EmployeeName"))
                        .pickupDate(getNullableLocalDateTime(rs, "PickupDate"))
                        .pickupDateD(getNullableString(rs, "PickupDateD"))
                        .deliveryDateD(getNullableString(rs, "DeliveryDateD"))
                        .id(rs.getInt("Id"))
                        .jobNo(getNullableString(rs, "JobNo"))
                        .sPickupDate(getNullableString(rs, "SPickupDate"))
                        .sDeliveryDate(getNullableString(rs, "SDeliveryDate"))
                        .wareHouseEnterDate(getNullableLocalDateTime(rs, "WareHouseEnterDate"))
                        .wareHouseExitDate(getNullableLocalDateTime(rs, "WareHouseExitDate"))
                        .sWareHouseEnterDate(getNullableString(rs, "SWareHouseEnterDate"))
                        .sWareHouseExitDate(getNullableString(rs, "SWareHouseExitDate"))
                        .wareHouseAddress(getNullableString(rs, "WareHouseAddress"))
                        .origin(getNullableString(rs, "Origin"))
                        .destination(getNullableString(rs, "Destination"))
                        .originD(getNullableString(rs, "OriginD"))
                        .destinationD(getNullableString(rs, "DestinationD"))
                        .pkg(getNullableString(rs, "pkg"))
                        .vesselName(getNullableString(rs, "VesselName"))
                        .jobDate(getNullableString(rs, "JobDate"))
                        .customerName(getNullableString(rs, "CustomerName"))
                        .truckName(getNullableString(rs, "TruckName"))
                        .truckRefId(rs.getInt("TruckRefid"))
                        .remarks(getNullableString(rs, "Remarks"))
                        .jobStatus(getNullableString(rs, "JobStatus"))
                        .pickupAddress(getNullableString(rs, "PickupAddress"))
                        .deliveryAddress(getNullableString(rs, "DeliveryAddress"))
                        .leta(getNullableString(rs, "LETA"))
                        .oeta(getNullableString(rs, "OETA"))
                        .jobName(getNullableString(rs, "JobName"))
                        .awbNo(getNullableString(rs, "AWBNo"))
                        .blCopy(getNullableString(rs, "BLCopy"))
                        .sPort(getNullableString(rs, "SPort"))
                        .oPort(getNullableString(rs, "OPort"))
                        .truckSize(getNullableString(rs, "truckSize"))
                        .pickupTimeList(getNullableString(rs, "pickuptimelist"))
                        .pickupQuantityList(getNullableString(rs, "pickupQuantitylist"))
                        .deliveryQuantityList(getNullableString(rs, "DeliveryQuantitylist"))
                        .deliveryTimeList(getNullableString(rs, "Delivertimelist"))
                        .sdId(rs.getInt("SDId"))
                        .planningMasterRefId(rs.getInt("PLANINGMasterRefId"))
                        .saleOrderMasterRefId(rs.getInt("SaleOrderMasterRefId"))
                        .sortBy(rs.getInt("SortBy"))
                        .truckNameD(getNullableString(rs, "TruckNameD"))
                        .driverNameD(getNullableString(rs, "DriverNameD"))
                        .build());
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRequestException(fieldName + " is required");
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidRequestException(fieldName + " must be in yyyy-MM-dd format");
        }
    }

    private Integer parseOptionalInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new InvalidRequestException(fieldName + " must be a valid integer");
        }
    }

    private String getNullableString(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        String value = rs.getString(column);
        return value != null ? value : "";
    }

    private LocalDateTime getNullableLocalDateTime(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}

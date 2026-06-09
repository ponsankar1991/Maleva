package my.maleva.api.module.planning.service;

import my.maleva.api.module.planning.dto.PlanningDetailsDto;
import my.maleva.api.module.planning.dto.PlanningEditResponseDto;
import my.maleva.api.module.planning.dto.PlanningMasterDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.planning.mapper.PlanningMasterMapper;
import my.maleva.api.module.planning.mapper.PlanningF5ViewMapper;
import my.maleva.api.module.planning.mapper.PlanningQueryMapper;
import my.maleva.api.module.planning.entity.PlanningDetails;
import my.maleva.api.module.planning.entity.PlanningMaster;
import my.maleva.api.module.planning.repository.PlanningDetailsRepository;
import my.maleva.api.module.planning.repository.PlanningMasterRepository;
import my.maleva.api.module.planning.dto.PlanningF5View;
import my.maleva.api.module.planning.dto.request.PlanningF5RequestDto;
import my.maleva.api.module.planning.dto.request.PLANINGSearchRequestDto;
import my.maleva.api.module.planning.dto.PlanningMasterViewModel;
import my.maleva.api.module.planning.dto.PlanningDetailsModel;
import my.maleva.api.module.planning.dto.query.PlanningEditMasterRow;
import my.maleva.api.module.planning.dto.query.PlanningSelectMasterRow;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanningMasterService {

    private final PlanningMasterRepository planningMasterRepository;
    private final PlanningDetailsRepository planningDetailsRepository;
    private final PlanningMasterMapper planningMasterMapper;
    private final PlanningF5ViewMapper planningF5ViewMapper;
    private final PlanningQueryMapper planningQueryMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_PLANNING_MASTER_SQL = """
            SELECT
                A.Id as Id,
                A.CNumber as PLANINGNo,
                A.CNumberDisplay as PLANINGNoDisplay,
                CONVERT(VARCHAR(10), ISNULL(A.SaleDate, '1900-01-01'), 103) as PLANINGDate,
                ISNULL(A.Remarks, '') as Remarks,
                ISNULL(E.EmployeeName, '') as EmployeeName,
                (
                    SELECT COUNT(1)
                    FROM PLANINGDetails PD WITH(NOLOCK)
                    WHERE PD.PLANINGMasterRefId = A.Id
                ) as TotalOrders
            FROM PLANINGMaster A WITH(NOLOCK)
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId
            """;

    private static final String SELECT_PLANNING_DETAILS_SQL = """
            SELECT
                B.Id,
                0 as SDId,
                B.PLANINGMasterRefId,
                B.SaleOrderMasterRefId,
                ISNULL(B.TruckRefid, 0) as TruckRefid,
                ISNULL(T.TruckName, '') as TruckName,
                ISNULL(B.DriverName, '') as DriverName,
                SM.CNumberDisplay as JobNo,
                CONVERT(VARCHAR(10), ISNULL(SM.SaleDate, '1900-01-01'), 103) as JobDate,
                ISNULL(JS.Name, '') as JobStatus,
                ISNULL(B.OriginD, '') as OriginD,
                ISNULL(B.DestinationD, '') as DestinationD,
                ISNULL(C.CustomerName, '') as CustomerName,
                ISNULL(B.Remarks, '') as Remarks,
                ISNULL(B.TruckNameD, '') as TruckNameD,
                ISNULL(B.DriverNameD, '') as DriverNameD,
                ISNULL(B.SortBy, 0) as SortBy,
                ISNULL(CONVERT(VARCHAR(16), B.PickupDateD, 120), '') as PickupDateD,
                ISNULL(CONVERT(VARCHAR(16), B.DeliveryDateD, 120), '') as DeliveryDateD,
                ISNULL(B.pickuptimelist, '') as pickuptimelist,
                ISNULL(B.pickupQuantitylist, '') as pickupQuantitylist,
                ISNULL(B.DeliveryQuantitylist, '') as DeliveryQuantitylist,
                ISNULL(B.Delivertimelist, '') as Delivertimelist
            FROM PLANINGDetails B WITH(NOLOCK)
            INNER JOIN PLANINGMaster A WITH(NOLOCK) ON B.PLANINGMasterRefId = A.Id
            INNER JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId
            LEFT JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId
            LEFT JOIN TruckMaster T WITH(NOLOCK) ON T.Id = B.TruckRefid
            LEFT JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = SM.JStatus
            """;

    private static final String EDIT_PLANNING_MASTER_SQL = """
            SELECT
                A.Id as Id,
                A.CompanyRefId as CompanyRefId,
                A.UserRefId as UserRefId,
                A.EmployeeRefId as EmployeeRefId,
                A.LastEmployeeRefId as LastEmployeeRefId,
                CONVERT(VARCHAR(10), A.FDate, 23) as SFDate,
                CONVERT(VARCHAR(10), A.TDate, 23) as STDate,
                CONVERT(VARCHAR(10), A.SaleDate, 23) as SaleDate,
                CONVERT(VARCHAR(10), A.SaleDate, 23) as SSaleDate,
                A.CNumberDisplay as CNumberDisplay,
                A.CNumber as CNumber,
                ISNULL(A.Remarks, '') as Remarks,
                ISNULL(A.Search, '') as Search,
                A.Active as Active,
                CONVERT(VARCHAR(19), A.Created_Date, 120) as CreatedDate,
                A.Created_By as CreatedBy,
                CONVERT(VARCHAR(19), A.Modified_Date, 120) as ModifiedDate,
                A.Modified_By as ModifiedBy
            FROM PLANINGMaster A WITH(NOLOCK)
            WHERE A.Id = :planningId
              AND A.CompanyRefId = :companyId
              AND A.Active != 2
            """;

    private static final String EDIT_PLANNING_DETAILS_SQL = """
            SELECT
                B.Id,
                B.Id as SDId,
                B.PLANINGMasterRefId,
                B.SaleOrderMasterRefId,
                ISNULL(B.TruckRefid, 0) as TruckRefid,
                ISNULL(T.TruckName, '') as TruckName,
                ISNULL(B.DriverName, '') as DriverName,
                SM.CNumberDisplay as JobNo,
                CONVERT(VARCHAR(10), ISNULL(SM.SaleDate, '1900-01-01'), 103) as JobDate,
                ISNULL(J.Name, '') as JobStatus,
                ISNULL(JT.Name, '') as JobName,
                ISNULL(SM.AWBNo, '') as AWBNo,
                ISNULL(SM.BLCopy, '') as BLCopy,
                ISNULL(C.CustomerName, '') as CustomerName,
                ISNULL(B.Remarks, '') as Remarks,
                ISNULL(SM.Origin, '') as Origin,
                ISNULL(SM.Destination, '') as Destination,
                ISNULL(B.OriginD, '') as OriginD,
                ISNULL(B.DestinationD, '') as DestinationD,
                ISNULL(ISNULL(SM.Quantity, '') + '/' + ISNULL(SM.TotalWeight, ''), '') as pkg,
                COALESCE(
                    NULLIF(LTRIM(RTRIM(SM.Loadingvesselname)), ''),
                    NULLIF(LTRIM(RTRIM(SM.Offvesselname)), ''),
                    ''
                ) as VesselName,
                ISNULL(E.EmployeeName, '') as EmployeeName,
                ISNULL(SM.truckSize, '') as truckSize,
                ISNULL(CONVERT(VARCHAR(16), SM.PickupDate, 120), '') as SPickupDate,
                ISNULL(CONVERT(VARCHAR(16), B.PickupDateD, 120), '') as PickupDateD,
                ISNULL(CONVERT(VARCHAR(16), SM.DeliveryDate, 120), '') as SDeliveryDate,
                ISNULL(CONVERT(VARCHAR(16), B.DeliveryDateD, 120), '') as DeliveryDateD,
                ISNULL(CONVERT(VARCHAR(16), SM.ETA, 120), '') as LETA,
                ISNULL(CONVERT(VARCHAR(16), SM.OETA, 120), '') as OETA,
                SM.WareHouseEnterDate as WareHouseEnterDate,
                SM.WareHouseExitDate as WareHouseExitDate,
                ISNULL(CONVERT(VARCHAR(16), SM.WareHouseEnterDate, 120), '') as SWareHouseEnterDate,
                ISNULL(CONVERT(VARCHAR(16), SM.WareHouseExitDate, 120), '') as SWareHouseExitDate,
                ISNULL(SM.WareHouseAddress, '') as WareHouseAddress,
                ISNULL(SM.PickupAddress, '') as PickupAddress,
                ISNULL(SM.DeliveryAddress, '') as DeliveryAddress,
                ISNULL(SM.SPort, '') as SPort,
                ISNULL(SM.OPort, '') as OPort,
                ISNULL(B.SortBy, 0) as SortBy,
                ISNULL(B.TruckNameD, '') as TruckNameD,
                ISNULL(B.DriverNameD, '') as DriverNameD,
                ISNULL(SM.pickuptimelist, '') as pickuptimelist,
                ISNULL(SM.pickupQuantitylist, '') as pickupQuantitylist,
                ISNULL(SM.DeliveryQuantitylist, '') as DeliveryQuantitylist,
                ISNULL(SM.Delivertimelist, '') as Delivertimelist
            FROM PLANINGMaster A WITH(NOLOCK)
            INNER JOIN PLANINGDetails B WITH(NOLOCK) ON A.Id = B.PLANINGMasterRefId
            INNER JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId
            LEFT JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId
            LEFT JOIN TruckMaster T WITH(NOLOCK) ON T.Id = B.TruckRefid
            LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id = SM.JStatus
            LEFT JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = SM.JobMasterRefId
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = SM.LastEmployeeRefid
            WHERE A.Id = :planningId
              AND A.CompanyRefId = :companyId
            ORDER BY ISNULL(B.SortBy, 0) ASC, B.Id ASC
            """;

    public PlanningMasterService(
            PlanningMasterRepository planningMasterRepository,
            PlanningDetailsRepository planningDetailsRepository,
            PlanningMasterMapper planningMasterMapper,
            PlanningF5ViewMapper planningF5ViewMapper,
            PlanningQueryMapper planningQueryMapper,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.planningMasterRepository = planningMasterRepository;
        this.planningDetailsRepository = planningDetailsRepository;
        this.planningMasterMapper = planningMasterMapper;
        this.planningF5ViewMapper = planningF5ViewMapper;
        this.planningQueryMapper = planningQueryMapper;
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
        PlanningSelectionFilter criteria = buildSelectionFilter(filter);

        List<PlanningMasterViewModel> masterViewModels = namedParameterJdbcTemplate.query(
                        SELECT_PLANNING_MASTER_SQL + criteria.whereClause() + " ORDER BY A.SaleDate DESC, A.Id DESC",
                        criteria.params(),
                        (rs, rowNum) -> planningQueryMapper.toPlanningMasterViewModel(
                                PlanningSelectMasterRow.builder()
                                        .id(rs.getInt("Id"))
                                        .planningNo(rs.getInt("PLANINGNo"))
                                        .planningNoDisplay(getNullableString(rs, "PLANINGNoDisplay"))
                                        .planningDate(getNullableString(rs, "PLANINGDate"))
                                        .remarks(getNullableString(rs, "Remarks"))
                                        .employeeName(getNullableString(rs, "EmployeeName"))
                                        .totalOrders(rs.getInt("TotalOrders"))
                                        .build()
                        )
                );

        List<PlanningDetailsModel> detailsViewModels = namedParameterJdbcTemplate.query(
                SELECT_PLANNING_DETAILS_SQL + criteria.whereClause() + " ORDER BY ISNULL(B.SortBy, 0) ASC, B.Id ASC",
                criteria.params(),
                (rs, rowNum) -> PlanningDetailsModel.builder()
                        .id(rs.getInt("Id"))
                        .sdId(rs.getInt("SDId"))
                        .planningMasterRefId(rs.getInt("PLANINGMasterRefId"))
                        .saleOrderMasterRefId(rs.getInt("SaleOrderMasterRefId"))
                        .truckRefId(rs.getInt("TruckRefid"))
                        .truckName(getNullableString(rs, "TruckName"))
                        .driverName(getNullableString(rs, "DriverName"))
                        .jobNo(getNullableString(rs, "JobNo"))
                        .jobDate(getNullableString(rs, "JobDate"))
                        .jobStatus(getNullableString(rs, "JobStatus"))
                        .originD(getNullableString(rs, "OriginD"))
                        .destinationD(getNullableString(rs, "DestinationD"))
                        .customerName(getNullableString(rs, "CustomerName"))
                        .remarks(getNullableString(rs, "Remarks"))
                        .truckNameD(getNullableString(rs, "TruckNameD"))
                        .driverNameD(getNullableString(rs, "DriverNameD"))
                        .sortBy(rs.getInt("SortBy"))
                        .pickupDateD(getNullableString(rs, "PickupDateD"))
                        .deliveryDateD(getNullableString(rs, "DeliveryDateD"))
                        .pickupTimeList(getNullableString(rs, "pickuptimelist"))
                        .pickupQuantityList(getNullableString(rs, "pickupQuantitylist"))
                        .deliveryQuantityList(getNullableString(rs, "DeliveryQuantitylist"))
                        .deliveryTimeList(getNullableString(rs, "Delivertimelist"))
                        .build()
        );

        return planningF5ViewMapper.createPlanningF5View(masterViewModels, detailsViewModels);
    }

    public PlanningEditResponseDto editPlanning(Integer id, Integer planningNo, Integer companyId) {
        Integer resolvedPlanningId = resolvePlanningId(id, planningNo, companyId);

        PlanningEditMasterRow masterRow = findPlanningEditMaster(resolvedPlanningId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Planning not found: " + resolvedPlanningId));

        List<PlanningDetailsModel> saleDetails = namedParameterJdbcTemplate.query(
                EDIT_PLANNING_DETAILS_SQL,
                new MapSqlParameterSource()
                        .addValue("planningId", resolvedPlanningId)
                        .addValue("companyId", companyId),
                (rs, rowNum) -> PlanningDetailsModel.builder()
                        .id(rs.getInt("Id"))
                        .sdId(rs.getInt("SDId"))
                        .planningMasterRefId(rs.getInt("PLANINGMasterRefId"))
                        .saleOrderMasterRefId(rs.getInt("SaleOrderMasterRefId"))
                        .truckRefId(rs.getInt("TruckRefid"))
                        .truckName(getNullableString(rs, "TruckName"))
                        .driverName(getNullableString(rs, "DriverName"))
                        .jobNo(getNullableString(rs, "JobNo"))
                        .jobDate(getNullableString(rs, "JobDate"))
                        .jobStatus(getNullableString(rs, "JobStatus"))
                        .jobName(getNullableString(rs, "JobName"))
                        .awbNo(getNullableString(rs, "AWBNo"))
                        .blCopy(getNullableString(rs, "BLCopy"))
                        .customerName(getNullableString(rs, "CustomerName"))
                        .remarks(getNullableString(rs, "Remarks"))
                        .origin(getNullableString(rs, "Origin"))
                        .destination(getNullableString(rs, "Destination"))
                        .originD(getNullableString(rs, "OriginD"))
                        .destinationD(getNullableString(rs, "DestinationD"))
                        .pkg(getNullableString(rs, "pkg"))
                        .vesselName(getNullableString(rs, "VesselName"))
                        .employeeName(getNullableString(rs, "EmployeeName"))
                        .truckSize(getNullableString(rs, "truckSize"))
                        .sPickupDate(getNullableString(rs, "SPickupDate"))
                        .pickupDateD(getNullableString(rs, "PickupDateD"))
                        .sDeliveryDate(getNullableString(rs, "SDeliveryDate"))
                        .deliveryDateD(getNullableString(rs, "DeliveryDateD"))
                        .leta(getNullableString(rs, "LETA"))
                        .oeta(getNullableString(rs, "OETA"))
                        .wareHouseEnterDate(getNullableLocalDateTime(rs, "WareHouseEnterDate"))
                        .wareHouseExitDate(getNullableLocalDateTime(rs, "WareHouseExitDate"))
                        .sWareHouseEnterDate(getNullableString(rs, "SWareHouseEnterDate"))
                        .sWareHouseExitDate(getNullableString(rs, "SWareHouseExitDate"))
                        .wareHouseAddress(getNullableString(rs, "WareHouseAddress"))
                        .pickupAddress(getNullableString(rs, "PickupAddress"))
                        .deliveryAddress(getNullableString(rs, "DeliveryAddress"))
                        .sPort(getNullableString(rs, "SPort"))
                        .oPort(getNullableString(rs, "OPort"))
                        .sortBy(rs.getInt("SortBy"))
                        .truckNameD(getNullableString(rs, "TruckNameD"))
                        .driverNameD(getNullableString(rs, "DriverNameD"))
                        .pickupTimeList(getNullableString(rs, "pickuptimelist"))
                        .pickupQuantityList(getNullableString(rs, "pickupQuantitylist"))
                        .deliveryQuantityList(getNullableString(rs, "DeliveryQuantitylist"))
                        .deliveryTimeList(getNullableString(rs, "Delivertimelist"))
                        .build()
        );

        return planningQueryMapper.toPlanningEditResponse(masterRow, saleDetails);
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

    private PlanningSelectionFilter buildSelectionFilter(PlanningF5RequestDto filter) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", filter.getComid());

        StringBuilder whereClause = new StringBuilder("""
                WHERE A.CompanyRefId = :companyId
                  AND A.Active = 1
                """);

        if (filter.getEmployeeid() != null && filter.getEmployeeid() != 0) {
            whereClause.append(" AND A.EmployeeRefId = :employeeId");
            params.addValue("employeeId", filter.getEmployeeid());
        }

        String search = trimToNull(filter.getSearch());
        if (search != null) {
            whereClause.append(" AND A.CNumberDisplay = :search");
            params.addValue("search", search);
            return new PlanningSelectionFilter(whereClause.toString(), params);
        }

        if (filter.getFromdate() == null || filter.getTodate() == null) {
            throw new InvalidRequestException("fromdate and todate are required when search is empty");
        }

        if (filter.getFromdate().isAfter(filter.getTodate())) {
            throw new InvalidRequestException("fromdate must be less than or equal to todate");
        }

        whereClause.append(" AND CAST(A.SaleDate as DATE) BETWEEN :fromDate AND :toDate");
        params.addValue("fromDate", filter.getFromdate());
        params.addValue("toDate", filter.getTodate());

        return new PlanningSelectionFilter(whereClause.toString(), params);
    }

    private Optional<PlanningEditMasterRow> findPlanningEditMaster(Integer planningId, Integer companyId) {
        List<PlanningEditMasterRow> rows = namedParameterJdbcTemplate.query(
                EDIT_PLANNING_MASTER_SQL,
                new MapSqlParameterSource()
                        .addValue("planningId", planningId)
                        .addValue("companyId", companyId),
                (rs, rowNum) -> PlanningEditMasterRow.builder()
                        .id(rs.getInt("Id"))
                        .companyRefId(rs.getInt("CompanyRefId"))
                        .userRefId(rs.getInt("UserRefId"))
                        .employeeRefId(rs.getInt("EmployeeRefId"))
                        .lastEmployeeRefId(rs.getInt("LastEmployeeRefId"))
                        .sFDate(getNullableString(rs, "SFDate"))
                        .sTDate(getNullableString(rs, "STDate"))
                        .saleDate(getNullableString(rs, "SaleDate"))
                        .sSaleDate(getNullableString(rs, "SSaleDate"))
                        .cNumberDisplay(getNullableString(rs, "CNumberDisplay"))
                        .cNumber(rs.getInt("CNumber"))
                        .remarks(getNullableString(rs, "Remarks"))
                        .search(getNullableString(rs, "Search"))
                        .active(rs.getInt("Active"))
                        .createdDate(getNullableString(rs, "CreatedDate"))
                        .createdBy(getNullableString(rs, "CreatedBy"))
                        .modifiedDate(getNullableString(rs, "ModifiedDate"))
                        .modifiedBy(getNullableString(rs, "ModifiedBy"))
                        .build()
        );

        return rows.stream().findFirst();
    }

    private Integer resolvePlanningId(Integer id, Integer planningNo, Integer companyId) {
        if (id != null && id > 0) {
            return id;
        }

        if (planningNo != null && planningNo > 0) {
            return planningMasterRepository.findByCompanyRefIdAndCNumber(companyId, planningNo)
                    .map(PlanningMaster::getId)
                    .orElseThrow(() -> new EntityNotFoundException("Planning not found for number: " + planningNo));
        }

        throw new InvalidRequestException("Either id or planningNo is required");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record PlanningSelectionFilter(String whereClause, MapSqlParameterSource params) {
    }
}

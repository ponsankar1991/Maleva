package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;
import my.maleva.api.module.vessalplanning.entity.VesselPlanningMaster;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningMasterRepository;
import my.maleva.api.module.vessalplanning.service.IVesselPlanningMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VesselPlanningMasterService implements IVesselPlanningMasterService {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningMasterService.class);

    private static final String SELECT_MASTER_SQL = """
            SELECT A.Id,A.CNumber as VESSELPLANINGNo,A.CNumberDisplay as VESSELPLANINGNoDisplay,
                   CONVERT(VARCHAR(10),ISNULL(A.SaleDate,'1900-01-01'),103) as VESSELPLANINGDate,
                    CONVERT(VARCHAR(10),A.FDate,23) as SFDate,
                   CONVERT(VARCHAR(10),A.TDate,23) as STDate,
                   ISNULL(A.Remarks,'') as Remarks, ISNULL(A.Search,'') as Search

            FROM VESSELPLANINGMaster A WITH(NOLOCK)
            """;

    private static final String SELECT_DETAILS_SQL = """
            SELECT B.Id,
                   B.Id as SDId,
                   B.VESSELPLANINGMasterRefId,
                   B.SaleOrderMasterRefId,
                   ISNULL(S.Origin,'') as Origin,
                   ISNULL(S.Destination,'') as Destination,
                   S.CNumberDisplay as JobNo,
                   CONVERT(VARCHAR(10),ISNULL(S.SaleDate,'1900-01-01'),103) as JobDate,
                   ISNULL(J.Name,'') as JobStatus,
                   ISNULL(S.SCN,'') as SCN,
                   ISNULL(S.LSCN,'') as LSCN,
                   S.ETA as ETA,
                   ISNULL(CONVERT(VARCHAR(19),S.ETA,120),'') as SETA,
                   S.ETB as ETB,
                   ISNULL(CONVERT(VARCHAR(19),S.ETB,120),'') as SETB,
                   S.ETD as ETD,
                   ISNULL(CONVERT(VARCHAR(19),S.ETD,120),'') as SETD,
                   S.OETA as OETA,
                   ISNULL(CONVERT(VARCHAR(19),S.OETA,120),'') as SOETA,
                   S.OETB as OETB,
                   ISNULL(CONVERT(VARCHAR(19),S.OETB,120),'') as SOETB,
                   S.OETD as OETD,
                   ISNULL(CONVERT(VARCHAR(19),S.OETD,120),'') as SOETD,
                   S.PickupDate as PickupDate,
                   ISNULL(CONVERT(VARCHAR(19),S.PickupDate,120),'') as SPickupDate,
                   S.DeliveryDate as DeliveryDate,
                   ISNULL(CONVERT(VARCHAR(19),S.DeliveryDate,120),'') as SDeliveryDate,
                   S.WareHouseEnterDate as WareHouseEnterDate,
                   ISNULL(CONVERT(VARCHAR(19),S.WareHouseEnterDate,120),'') as SWareHouseEnterDate,
                   S.WareHouseExitDate as WareHouseExitDate,
                   ISNULL(CONVERT(VARCHAR(19),S.WareHouseExitDate,120),'') as SWareHouseExitDate,
                   ISNULL(S.WareHouseAddress,'') as WareHouseAddress,
                   ISNULL(S.Quantity,'') + '/' + ISNULL(S.TotalWeight,'') as pkg,
                   ISNULL(S.Loadingvesselname,'') as Loadingvesselname,
                   ISNULL(S.BLCopy,'') as BLCopy,
                   ISNULL(S.TruckSize,'') as TruckSize,
                   ISNULL(S.Offvesselname,'') as Offvesselname,
                   ISNULL(S.Commodity,'') as Commodity,
                   ISNULL(S.Vessel,'') as Vessel,
                   ISNULL(S.OVessel,'') as OVessel,
                   ISNULL(S.SPort,'') as SPort,
                   ISNULL(S.OPort,'') as OPort,
                   ISNULL(JT.Name,'') as JobName,
                   ISNULL(S.AWBNo,'') as AWBNo,
                   ISNULL(S.Remarks,'') as Remarks1,
                   ISNULL(S.Cargo,'') as Cargo,
                   ISNULL(S.PTW,'') as PTW,
                   ISNULL(S.ZB,'') as ZB,
                   ISNULL(S.ZB2,'') as ZB2,
                   ISNULL(S.ZBRef,'') as ZBRef,
                   ISNULL(S.ZBRef2,'') as ZBRef2,
                   ISNULL(S.PortCharges,0) as PortCharges,
                   ISNULL(S.PortChargesRef,'') as PortChargesRef,
                   ISNULL(Ag.AgentName,'') as AgentName,
                   ISNULL(Ag.MobileNo,'') as AgentPhone,
                   ISNULL(OAg.AgentName,'') as OAgentName,
                   ISNULL(OAg.MobileNo,'') as OAgentPhone,
                   ISNULL(S.BoardingOfficerRefid,0) as BoardingOfficerRefid,
                   ISNULL(EB.EmployeeName,'') as BoardingOfficerName,
                   ISNULL(S.BoardingOfficer1Refid,0) as BoardingOfficer1Refid,
                   ISNULL(EB1.EmployeeName,'') as BoardingOfficerName1,
                   ISNULL(S.BoardingAmount,0) as BoardingAmount,
                   ISNULL(S.BoardingAmount1,0) as BoardingAmount1,
                   ISNULL(C.CustomerName,'') as CustomerName,
                   ISNULL(E.EmployeeName,'') as EmployeeName,
                   ISNULL(B.Remarks,'') as Remarks,
                   ISNULL(S.ETA,S.OETA) as DETA
            FROM VESSELPLANINGMaster A WITH(NOLOCK)
            INNER JOIN VESSELPLANINGDetails B WITH(NOLOCK) ON A.Id=B.VESSELPLANINGMasterRefId
            INNER JOIN SaleOrderMaster S WITH(NOLOCK) ON S.Id=B.SaleOrderMasterRefId
            INNER JOIN Customer C WITH(NOLOCK) ON C.Id=S.CustomerRefId
            INNER JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id=S.JobMasterRefId
            LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id=S.JStatus
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id=ISNULL(S.LastEmployeeRefid,S.EmployeeRefId)
            LEFT JOIN Agent Ag WITH(NOLOCK) ON Ag.Id=S.AgentMasterRefid
            LEFT JOIN Agent OAg WITH(NOLOCK) ON OAg.Id=S.OAgentMasterRefid
            LEFT JOIN EmployeeMaster EB WITH(NOLOCK) ON EB.Id=S.BoardingOfficerRefid
            LEFT JOIN EmployeeMaster EB1 WITH(NOLOCK) ON EB1.Id=S.BoardingOfficer1Refid
            """;

    private static final String EDIT_MASTER_SQL = """
            SELECT A.Id,A.CompanyRefId,A.UserRefId,A.EmployeeRefId,
                   CONVERT(VARCHAR(10),A.FDate,23) as SFDate,
                   CONVERT(VARCHAR(10),A.TDate,23) as STDate,
                   CONVERT(VARCHAR(10),A.SaleDate,23) as SaleDate,
                   CONVERT(VARCHAR(10),A.SaleDate,23) as SSaleDate,
                   A.CNumberDisplay,A.CNumber,ISNULL(A.Remarks,'') as Remarks,
                   ISNULL(A.Search,'') as Search,A.Active,
                   CONVERT(VARCHAR(19),A.Created_Date,120) as CreatedDate,A.Created_By as CreatedBy,
                   CONVERT(VARCHAR(19),A.Modified_Date,120) as ModifiedDate,A.Modified_By as ModifiedBy
            FROM VESSELPLANINGMaster A WITH(NOLOCK)
            WHERE A.Id=:planningId AND A.CompanyRefId=:companyId AND A.Active!=2
            """;

    private static final String EDIT_DETAILS_SQL = """
            SELECT B.Id,
                   B.Id as SDId,
                   B.VESSELPLANINGMasterRefId,
                   B.SaleOrderMasterRefId,
                   ISNULL(S.Origin,'') as Origin,
                   ISNULL(S.Destination,'') as Destination,
                   S.CNumberDisplay as JobNo,
                   CONVERT(VARCHAR(10),ISNULL(S.SaleDate,'1900-01-01'),103) as JobDate,
                   ISNULL(J.Name,'') as JobStatus,
                   ISNULL(S.SCN,'') as SCN,
                   ISNULL(S.LSCN,'') as LSCN,
                   S.ETA as ETA,
                   ISNULL(CONVERT(VARCHAR(19),S.ETA,120),'') as SETA,
                   S.ETB as ETB,
                   ISNULL(CONVERT(VARCHAR(19),S.ETB,120),'') as SETB,
                   S.ETD as ETD,
                   ISNULL(CONVERT(VARCHAR(19),S.ETD,120),'') as SETD,
                   S.OETA as OETA,
                   ISNULL(CONVERT(VARCHAR(19),S.OETA,120),'') as SOETA,
                   S.OETB as OETB,
                   ISNULL(CONVERT(VARCHAR(19),S.OETB,120),'') as SOETB,
                   S.OETD as OETD,
                   ISNULL(CONVERT(VARCHAR(19),S.OETD,120),'') as SOETD,
                   S.PickupDate as PickupDate,
                   ISNULL(CONVERT(VARCHAR(19),S.PickupDate,120),'') as SPickupDate,
                   S.DeliveryDate as DeliveryDate,
                   ISNULL(CONVERT(VARCHAR(19),S.DeliveryDate,120),'') as SDeliveryDate,
                   S.WareHouseEnterDate as WareHouseEnterDate,
                   ISNULL(CONVERT(VARCHAR(19),S.WareHouseEnterDate,120),'') as SWareHouseEnterDate,
                   S.WareHouseExitDate as WareHouseExitDate,
                   ISNULL(CONVERT(VARCHAR(19),S.WareHouseExitDate,120),'') as SWareHouseExitDate,
                   ISNULL(S.WareHouseAddress,'') as WareHouseAddress,
                   ISNULL(S.Quantity,'') + '/' + ISNULL(S.TotalWeight,'') as pkg,
                   ISNULL(S.Loadingvesselname,'') as Loadingvesselname,
                   ISNULL(S.BLCopy,'') as BLCopy,
                   ISNULL(S.TruckSize,'') as TruckSize,
                   ISNULL(S.Offvesselname,'') as Offvesselname,
                   ISNULL(S.Commodity,'') as Commodity,
                   ISNULL(S.Vessel,'') as Vessel,
                   ISNULL(S.OVessel,'') as OVessel,
                   ISNULL(S.SPort,'') as SPort,
                   ISNULL(S.OPort,'') as OPort,
                   ISNULL(JT.Name,'') as JobName,
                   ISNULL(S.AWBNo,'') as AWBNo,
                   ISNULL(S.Remarks,'') as Remarks1,
                   ISNULL(S.Cargo,'') as Cargo,
                   ISNULL(S.PTW,'') as PTW,
                   ISNULL(S.ZB,'') as ZB,
                   ISNULL(S.ZB2,'') as ZB2,
                   ISNULL(S.ZBRef,'') as ZBRef,
                   ISNULL(S.ZBRef2,'') as ZBRef2,
                   ISNULL(S.PortCharges,0) as PortCharges,
                   ISNULL(S.PortChargesRef,'') as PortChargesRef,
                   ISNULL(Ag.AgentName,'') as AgentName,
                   ISNULL(Ag.MobileNo,'') as AgentPhone,
                   ISNULL(OAg.AgentName,'') as OAgentName,
                   ISNULL(OAg.MobileNo,'') as OAgentPhone,
                   ISNULL(S.BoardingOfficerRefid,0) as BoardingOfficerRefid,
                   ISNULL(EB.EmployeeName,'') as BoardingOfficerName,
                   ISNULL(S.BoardingOfficer1Refid,0) as BoardingOfficer1Refid,
                   ISNULL(EB1.EmployeeName,'') as BoardingOfficerName1,
                   ISNULL(S.BoardingAmount,0) as BoardingAmount,
                   ISNULL(S.BoardingAmount1,0) as BoardingAmount1,
                   ISNULL(C.CustomerName,'') as CustomerName,
                   ISNULL(E.EmployeeName,'') as EmployeeName,
                   ISNULL(B.Remarks,'') as Remarks,
                   ISNULL(S.ETA,S.OETA) as DETA
            FROM VESSELPLANINGMaster A WITH(NOLOCK)
            INNER JOIN VESSELPLANINGDetails B WITH(NOLOCK) ON A.Id=B.VESSELPLANINGMasterRefId
            INNER JOIN SaleOrderMaster S WITH(NOLOCK) ON S.Id=B.SaleOrderMasterRefId
            INNER JOIN Customer C WITH(NOLOCK) ON C.Id=S.CustomerRefId
            INNER JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id=S.JobMasterRefId
            LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id=S.JStatus
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id=ISNULL(S.LastEmployeeRefid,S.EmployeeRefId)
            LEFT JOIN Agent Ag WITH(NOLOCK) ON Ag.Id=S.AgentMasterRefid
            LEFT JOIN Agent OAg WITH(NOLOCK) ON OAg.Id=S.OAgentMasterRefid
            LEFT JOIN EmployeeMaster EB WITH(NOLOCK) ON EB.Id=S.BoardingOfficerRefid
            LEFT JOIN EmployeeMaster EB1 WITH(NOLOCK) ON EB1.Id=S.BoardingOfficer1Refid
            WHERE A.Id=:planningId AND A.CompanyRefId=:companyId
            ORDER BY B.Id ASC
            """;

    private static final String VIEW_SQL = """
            SELECT CONVERT(VARCHAR(10),A.FDate,23) as SFDate,
                   CONVERT(VARCHAR(10),A.TDate,23) as STDate,
                   A.SaleDate,
                   CONVERT(VARCHAR(10),A.SaleDate,23) as SSaleDate,
                   A.CNumberDisplay as PlaningNo,
                   ISNULL(A.Remarks,'') as Remarks,
                   ISNULL(B.Remarks,'') as JRemarks,
                   S.Id,ISNULL(S.Origin,'') as Origin,ISNULL(S.Destination,'') as Destination,
                   S.CNumberDisplay as JobNo,
                   CONVERT(VARCHAR(10),ISNULL(S.SaleDate,'1900-01-01'),103) as JobDate,
                   ISNULL(J.Name,'') as JobStatus,
                   ISNULL(S.ETA,S.OETA) as DETA,ISNULL(S.ETB,'1900-01-01') as DETB,
                   CONVERT(VARCHAR(19),ISNULL(S.PickupDate,'1900-01-01'),120) as PickupDate,
                   CONVERT(VARCHAR(19),ISNULL(S.DeliveryDate,'1900-01-01'),120) as DeliveryDate,
                   ISNULL(S.Quantity,'') + '/' + ISNULL(S.TotalWeight,'') as pkg,
                   COALESCE(NULLIF(S.Loadingvesselname,''),NULLIF(S.Offvesselname,''),'') as Vessel,
                   ISNULL(S.Commodity,'') as Commodity,
                   ISNULL(S.PTW,'') as PTW,
                   ISNULL(C.CustomerName,'') as CustomerName,
                   ISNULL(E.EmployeeName,'') as EmployeeName,
                   ISNULL(S.Cargo,'') as Cargo
            FROM VESSELPLANINGMaster A WITH(NOLOCK)
            INNER JOIN VESSELPLANINGDetails B WITH(NOLOCK) ON A.Id=B.VESSELPLANINGMasterRefId
            INNER JOIN SaleOrderMaster S WITH(NOLOCK) ON S.Id=B.SaleOrderMasterRefId
            INNER JOIN Customer C WITH(NOLOCK) ON C.Id=S.CustomerRefId
            LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id=S.JStatus
            LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id=S.LastEmployeeRefid
            WHERE A.Id=:soId AND A.CompanyRefId=:companyId
            ORDER BY ISNULL(S.ETA,S.OETA) ASC,B.Id ASC
            """;

    private final VesselPlanningMasterRepository masterRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VesselPlanningMasterService(VesselPlanningMasterRepository masterRepository, NamedParameterJdbcTemplate jdbcTemplate) {
        this.masterRepository = masterRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public VesselPlanningLegacyDtos.F5View selectVesselPlanning(VesselPlanningLegacyDtos.F5Request filter) {
        SelectionFilter selectionFilter = buildSelectionFilter(filter);
        List<VesselPlanningLegacyDtos.MasterViewModel> masters = jdbcTemplate.query(
                SELECT_MASTER_SQL + selectionFilter.whereClause + " ORDER BY A.SaleDate ASC",
                selectionFilter.params,
                (rs, rowNum) -> VesselPlanningLegacyDtos.MasterViewModel.builder()
                        .id(getInt(rs, "Id"))
                        .vesselPlanningNo(getInt(rs, "VESSELPLANINGNo"))
                        .vesselPlanningNoDisplay(getString(rs, "VESSELPLANINGNoDisplay"))
                        .vesselPlanningDate(getString(rs, "VESSELPLANINGDate"))
                        .remarks(getString(rs, "Remarks"))
                        .search(getString(rs, "Search"))
                        .sFDate(getString(rs, "SFDate"))
                        .sTDate(getString(rs, "STDate"))
                        .build());
        List<VesselPlanningLegacyDtos.DetailsModel> details = jdbcTemplate.query(
                SELECT_DETAILS_SQL + selectionFilter.whereClause + " ORDER BY B.Id ASC",
                selectionFilter.params,
                (rs, rowNum) -> mapDetail(rs));
        return VesselPlanningLegacyDtos.F5View.builder().salemaster(masters).saledetails(details).build();
    }

    public VesselPlanningLegacyDtos.EditResponse editVesselPlanning(Integer id, Integer vesselPlanningNo, Integer companyId) {
        Integer planningId = resolvePlanningId(id, vesselPlanningNo, companyId);
        List<VesselPlanningLegacyDtos.EditResponse> masterRows = jdbcTemplate.query(
                EDIT_MASTER_SQL,
                new MapSqlParameterSource().addValue("planningId", planningId).addValue("companyId", companyId),
                (rs, rowNum) -> VesselPlanningLegacyDtos.EditResponse.builder()
                        .id(getInt(rs, "Id"))
                        .companyRefId(getInt(rs, "CompanyRefId"))
                        .userRefId(getInt(rs, "UserRefId"))
                        .employeeRefId(getInt(rs, "EmployeeRefId"))
                        .sFDate(getString(rs, "SFDate"))
                        .sTDate(getString(rs, "STDate"))
                        .saleDate(getString(rs, "SaleDate"))
                        .sSaleDate(getString(rs, "SSaleDate"))
                        .cNumberDisplay(getString(rs, "CNumberDisplay"))
                        .cNumber(getInt(rs, "CNumber"))
                        .remarks(getString(rs, "Remarks"))
                        .search(getString(rs, "Search"))
                        .active(getInt(rs, "Active"))
                        .createdDate(getString(rs, "CreatedDate"))
                        .createdBy(getString(rs, "CreatedBy"))
                        .modifiedDate(getString(rs, "ModifiedDate"))
                        .modifiedBy(getString(rs, "ModifiedBy"))
                        .build());
        VesselPlanningLegacyDtos.EditResponse master = masterRows.stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Vessel planning not found: " + planningId));
        master.setSaleDetails(jdbcTemplate.query(
                EDIT_DETAILS_SQL,
                new MapSqlParameterSource().addValue("planningId", planningId).addValue("companyId", companyId),
                (rs, rowNum) -> mapDetail(rs)));
        return master;
    }

    public List<VesselPlanningLegacyDtos.DetailsModel> vesselPlanningSearch(VesselPlanningLegacyDtos.SearchRequest filter) {
        LocalDate fromDate = parseDate(filter.getFromdate(), "fromdate");
        LocalDate toDate = parseDate(filter.getTodate(), "todate");
        if (fromDate.isAfter(toDate)) {
            throw new InvalidRequestException("fromdate must be less than or equal to todate");
        }


        int etaType = filter.getEtaType() != null ? filter.getEtaType() : 0;
        String detaExpr = etaType == 1 ? "S.OETA" : etaType == 2 ? "S.ETA" : "ISNULL(S.ETA,S.OETA)";

        StringBuilder sql = new StringBuilder("""
                SELECT S.Id as Id,
                       0 as SDId,
                       0 as VESSELPLANINGMasterRefId,
                       S.Id as SaleOrderMasterRefId,
                       ISNULL(S.Origin,'') as Origin,
                       ISNULL(S.Destination,'') as Destination,
                       S.CNumberDisplay as JobNo,
                       CONVERT(VARCHAR(10),ISNULL(S.SaleDate,'1900-01-01'),103) as JobDate,
                       ISNULL(J.Name,'') as JobStatus,
                       ISNULL(S.SCN,'') as SCN,
                       ISNULL(S.LSCN,'') as LSCN,
                       S.ETA as ETA,
                       ISNULL(CONVERT(VARCHAR(19),S.ETA,120),'') as SETA,
                       S.ETB as ETB,
                       ISNULL(CONVERT(VARCHAR(19),S.ETB,120),'') as SETB,
                       S.ETD as ETD,
                       ISNULL(CONVERT(VARCHAR(19),S.ETD,120),'') as SETD,
                       S.OETA as OETA,
                       ISNULL(CONVERT(VARCHAR(19),S.OETA,120),'') as SOETA,
                       S.OETB as OETB,
                       ISNULL(CONVERT(VARCHAR(19),S.OETB,120),'') as SOETB,
                       S.OETD as OETD,
                       ISNULL(CONVERT(VARCHAR(19),S.OETD,120),'') as SOETD,
                       S.PickupDate as PickupDate,
                       ISNULL(CONVERT(VARCHAR(19),S.PickupDate,120),'') as SPickupDate,
                       S.DeliveryDate as DeliveryDate,
                       ISNULL(CONVERT(VARCHAR(19),S.DeliveryDate,120),'') as SDeliveryDate,
                       S.WareHouseEnterDate as WareHouseEnterDate,
                       ISNULL(CONVERT(VARCHAR(19),S.WareHouseEnterDate,120),'') as SWareHouseEnterDate,
                       S.WareHouseExitDate as WareHouseExitDate,
                       ISNULL(CONVERT(VARCHAR(19),S.WareHouseExitDate,120),'') as SWareHouseExitDate,
                       ISNULL(S.WareHouseAddress,'') as WareHouseAddress,
                       ISNULL(S.Quantity,'') + '/' + ISNULL(S.TotalWeight,'') as pkg,
                       ISNULL(S.Loadingvesselname,'') as Loadingvesselname,
                       ISNULL(S.BLCopy,'') as BLCopy,
                       ISNULL(S.TruckSize,'') as TruckSize,
                       ISNULL(S.Offvesselname,'') as Offvesselname,
                       ISNULL(S.Commodity,'') as Commodity,
                       ISNULL(S.Vessel,'') as Vessel,
                       ISNULL(S.OVessel,'') as OVessel,
                       ISNULL(S.SPort,'') as SPort,
                       ISNULL(S.OPort,'') as OPort,
                       ISNULL(JT.Name,'') as JobName,
                       ISNULL(S.AWBNo,'') as AWBNo,
                       ISNULL(S.Remarks,'') as Remarks1,
                       ISNULL(S.Cargo,'') as Cargo,
                       ISNULL(S.PTW,'') as PTW,
                       ISNULL(S.ZB,'') as ZB,
                       ISNULL(S.ZB2,'') as ZB2,
                       ISNULL(S.ZBRef,'') as ZBRef,
                       ISNULL(S.ZBRef2,'') as ZBRef2,
                       ISNULL(S.PortCharges,0) as PortCharges,
                       ISNULL(S.PortChargesRef,'') as PortChargesRef,
                       ISNULL(Ag.AgentName,'') as AgentName,
                       ISNULL(Ag.MobileNo,'') as AgentPhone,
                       ISNULL(OAg.AgentName,'') as OAgentName,
                       ISNULL(OAg.MobileNo,'') as OAgentPhone,
                       ISNULL(S.BoardingOfficerRefid,0) as BoardingOfficerRefid,
                       ISNULL(EB.EmployeeName,'') as BoardingOfficerName,
                       ISNULL(S.BoardingOfficer1Refid,0) as BoardingOfficer1Refid,
                       ISNULL(EB1.EmployeeName,'') as BoardingOfficerName1,
                       ISNULL(S.BoardingAmount,0) as BoardingAmount,
                       ISNULL(S.BoardingAmount1,0) as BoardingAmount1,
                       ISNULL(C.CustomerName,'') as CustomerName,
                       ISNULL(E.EmployeeName,'') as EmployeeName,
                       CAST('' AS VARCHAR(300)) as Remarks,
                """).append(detaExpr).append("""
                       as DETA
                FROM SaleOrderMaster S WITH(NOLOCK)
                INNER JOIN Customer C WITH(NOLOCK) ON C.Id=S.CustomerRefId
                INNER JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id=S.JobMasterRefId
                LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id=S.JStatus
                LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id=ISNULL(S.LastEmployeeRefid,S.EmployeeRefId)
                LEFT JOIN Agent Ag WITH(NOLOCK) ON Ag.Id=S.AgentMasterRefid
                LEFT JOIN Agent OAg WITH(NOLOCK) ON OAg.Id=S.OAgentMasterRefid
                LEFT JOIN EmployeeMaster EB WITH(NOLOCK) ON EB.Id=S.BoardingOfficerRefid
                LEFT JOIN EmployeeMaster EB1 WITH(NOLOCK) ON EB1.Id=S.BoardingOfficer1Refid
                WHERE S.CompanyRefId=:companyId AND S.Active!=2
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("companyId", filter.getComid())
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);

        if (filter.getEmployeeid() != null && filter.getEmployeeid() != 0) {
            sql.append(" AND ISNULL(S.LastEmployeeRefid,S.EmployeeRefId)=:employeeId");
            params.addValue("employeeId", filter.getEmployeeid());
        }

        if (filter.isDeliveryDone()){

            sql.append("AND J.Name NOT IN ('DELIVERY DONE','WAITING FOR POD','WAITING FOR BILLING','JOB COMPLET','Z-CANCEL')" );

        }

        List<String> ports = splitCsv(trimToNull(filter.getSearch()));
        if (!ports.isEmpty()) {
            sql.append(" AND (S.SPort IN (:ports) OR S.OPort IN (:ports))");
            params.addValue("ports", ports);
        }
        


        if (etaType == 1) {
            sql.append(" AND CAST(S.OETA as DATE) BETWEEN :fromDate AND :toDate");
        } else if (etaType == 2) {
            sql.append(" AND CAST(S.ETA as DATE) BETWEEN :fromDate AND :toDate");
        } else {
            sql.append(" AND (CAST(S.ETA as DATE) BETWEEN :fromDate AND :toDate OR CAST(S.OETA as DATE) BETWEEN :fromDate AND :toDate)");
        }

        sql.append(" ORDER BY DETA ASC, S.Id ASC");
        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> mapDetail(rs));
    }

    public List<VesselPlanningLegacyDtos.ViewModel> vesselPlanningView(Integer soId, Integer companyId) {
        if (soId == null || soId <= 0 || companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Valid SoId and companyId are required");
        }
        return jdbcTemplate.query(VIEW_SQL,
                new MapSqlParameterSource().addValue("soId", soId).addValue("companyId", companyId),
                (rs, rowNum) -> VesselPlanningLegacyDtos.ViewModel.builder()
                        .sFDate(getString(rs, "SFDate"))
                        .sTDate(getString(rs, "STDate"))
                        .saleDate(getDateTime(rs, "SaleDate"))
                        .sSaleDate(getString(rs, "SSaleDate"))
                        .planingNo(getString(rs, "PlaningNo"))
                        .remarks(getString(rs, "Remarks"))
                        .jRemarks(getString(rs, "JRemarks"))
                        .id(getInt(rs, "Id"))
                        .origin(getString(rs, "Origin"))
                        .destination(getString(rs, "Destination"))
                        .jobNo(getString(rs, "JobNo"))
                        .jobDate(getString(rs, "JobDate"))
                        .jobStatus(getString(rs, "JobStatus"))
                        .deta(getDateTime(rs, "DETA"))
                        .detb(getDateTime(rs, "DETB"))
                        .pickupDate(getString(rs, "PickupDate"))
                        .deliveryDate(getString(rs, "DeliveryDate"))
                        .pkg(getString(rs, "pkg"))
                        .vessel(getString(rs, "Vessel"))
                        .commodity(getString(rs, "Commodity"))
                        .ptw(getString(rs, "PTW"))
                        .customerName(getString(rs, "CustomerName"))
                        .employeeName(getString(rs, "EmployeeName"))
                        .cargo(getString(rs, "Cargo"))
                        .build());
    }

    private VesselPlanningLegacyDtos.DetailsModel mapDetail(ResultSet rs) {
        try {
            return VesselPlanningLegacyDtos.DetailsModel.builder()
                    .id(getInt(rs, "Id"))
                    .sdId(getInt(rs, "SDId"))
                    .vesselPlanningMasterRefId(getInt(rs, "VESSELPLANINGMasterRefId"))
                    .saleOrderMasterRefId(getInt(rs, "SaleOrderMasterRefId"))
                    .origin(getString(rs, "Origin"))
                    .destination(getString(rs, "Destination"))
                    .jobNo(getString(rs, "JobNo"))
                    .jobDate(getString(rs, "JobDate"))
                    .jobStatus(getString(rs, "JobStatus"))
                    .scn(getString(rs, "SCN"))
                    .lscn(getString(rs, "LSCN"))
                    .deta(getDateTime(rs, "DETA"))
                    .eta(getDateTime(rs, "ETA"))
                    .seta(getString(rs, "SETA"))
                    .etb(getDateTime(rs, "ETB"))
                    .setb(getString(rs, "SETB"))
                    .etd(getDateTime(rs, "ETD"))
                    .setd(getString(rs, "SETD"))
                    .oeta(getDateTime(rs, "OETA"))
                    .soeta(getString(rs, "SOETA"))
                    .oetb(getDateTime(rs, "OETB"))
                    .soetb(getString(rs, "SOETB"))
                    .oetd(getDateTime(rs, "OETD"))
                    .soetd(getString(rs, "SOETD"))
                    .pickupDate(getDateTime(rs, "PickupDate"))
                    .sPickupDate(getString(rs, "SPickupDate"))
                    .deliveryDate(getDateTime(rs, "DeliveryDate"))
                    .sDeliveryDate(getString(rs, "SDeliveryDate"))
                    .wareHouseEnterDate(getDateTime(rs, "WareHouseEnterDate"))
                    .sWareHouseEnterDate(getString(rs, "SWareHouseEnterDate"))
                    .sWareHouseExitDate(getString(rs, "SWareHouseExitDate"))
                    .wareHouseExitDate(getDateTime(rs, "WareHouseExitDate"))
                    .wareHouseAddress(getString(rs, "WareHouseAddress"))
                    .pkg(getString(rs, "pkg"))
                    .loadingvesselname(getString(rs, "Loadingvesselname"))
                    .blCopy(getString(rs, "BLCopy"))
                    .truckSize(getString(rs, "TruckSize"))
                    .offvesselname(getString(rs, "Offvesselname"))
                    .commodity(getString(rs, "Commodity"))
                    .vessel(getString(rs, "Vessel"))
                    .oVessel(getString(rs, "OVessel"))
                    .sPort(getString(rs, "SPort"))
                    .oPort(getString(rs, "OPort"))
                    .jobName(getString(rs, "JobName"))
                    .awbNo(getString(rs, "AWBNo"))
                    .remarks1(getString(rs, "Remarks1"))
                    .ptw(getString(rs, "PTW"))
                    .zb(getString(rs, "ZB"))
                    .zb2(getString(rs, "ZB2"))
                    .zbRef(getString(rs, "ZBRef"))
                    .zbRef2(getString(rs, "ZBRef2"))
                    .portCharges(getDouble(rs, "PortCharges"))
                    .portChargesRef(getString(rs, "PortChargesRef"))
                    .agentName(getString(rs, "AgentName"))
                    .agentPhone(getString(rs, "AgentPhone"))
                    .oAgentName(getString(rs, "OAgentName"))
                    .oAgentPhone(getString(rs, "OAgentPhone"))
                    .boardingOfficerRefid(getInt(rs, "BoardingOfficerRefid"))
                    .boardingOfficerName(getString(rs, "BoardingOfficerName"))
                    .boardingOfficer1Refid(getInt(rs, "BoardingOfficer1Refid"))
                    .boardingOfficerName1(getString(rs, "BoardingOfficerName1"))
                    .boardingAmount(getDouble(rs, "BoardingAmount"))
                    .boardingAmount1(getDouble(rs, "BoardingAmount1"))
                    .customerName(getString(rs, "CustomerName"))
                    .employeeName(getString(rs, "EmployeeName"))
                    .remarks(getString(rs, "Remarks"))
                    .cargo(getString(rs, "Cargo"))
                    .build();
        } catch (SQLException ex) {
            throw new InvalidRequestException("Unable to map vessel planning detail row", ex);
        }
    }

    private SelectionFilter buildSelectionFilter(VesselPlanningLegacyDtos.F5Request filter) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("companyId", filter.getComid());
        StringBuilder where = new StringBuilder(" WHERE A.CompanyRefId=:companyId AND A.Active=1");
        if (filter.getEmployeeid() != null && filter.getEmployeeid() != 0) {
            where.append(" AND A.EmployeeRefId=:employeeId");
            params.addValue("employeeId", filter.getEmployeeid());
        }
        String search = trimToNull(filter.getSearch());
        if (search != null) {
            where.append(" AND A.CNumberDisplay=:search");
            params.addValue("search", search);
            return new SelectionFilter(where.toString(), params);
        }
        if (filter.getFromdate() == null || filter.getTodate() == null) {
            throw new InvalidRequestException("fromdate and todate are required when search is empty");
        }
        if (filter.getFromdate().isAfter(filter.getTodate())) {
            throw new InvalidRequestException("fromdate must be less than or equal to todate");
        }
        where.append(" AND CAST(A.SaleDate as DATE) BETWEEN :fromDate AND :toDate");
        params.addValue("fromDate", filter.getFromdate()).addValue("toDate", filter.getTodate());
        return new SelectionFilter(where.toString(), params);
    }

    private Integer resolvePlanningId(Integer id, Integer vesselPlanningNo, Integer companyId) {
        if (id != null && id > 0) return id;
        if (vesselPlanningNo != null && vesselPlanningNo > 0) {
            VesselPlanningMaster master = masterRepository.findByCompanyRefIdAndCNumber(companyId, vesselPlanningNo)
                    .orElseThrow(() -> new EntityNotFoundException("Vessel planning not found for number: " + vesselPlanningNo));
            return master.getId();
        }
        throw new InvalidRequestException("Either id or vesselPlanningNo is required");
    }

    private LocalDate parseDate(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new InvalidRequestException(field + " is required");
        try { return LocalDate.parse(value.trim()); } catch (DateTimeParseException ex) { throw new InvalidRequestException(field + " must be in yyyy-MM-dd format"); }
    }

    private List<String> splitCsv(String value) {
        List<String> parts = new ArrayList<>();
        if (value == null) return parts;
        for (String item : value.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) parts.add(trimmed);
        }
        return parts;
    }

    private String trimToNull(String value) { if (value == null) return null; String trimmed = value.trim(); return trimmed.isEmpty() ? null : trimmed; }
    private String getString(ResultSet rs, String column) throws SQLException { try { String v = rs.getString(column); return v != null ? v : ""; } catch (SQLException ex) { return ""; } }
    private Integer getInt(ResultSet rs, String column) throws SQLException { try { int v = rs.getInt(column); return rs.wasNull() ? null : v; } catch (SQLException ex) { return null; } }
    private Double getDouble(ResultSet rs, String column) throws SQLException { try { double v = rs.getDouble(column); return rs.wasNull() ? null : v; } catch (SQLException ex) { return null; } }
    private LocalDateTime getDateTime(ResultSet rs, String column) throws SQLException { try { Timestamp t = rs.getTimestamp(column); return t != null ? t.toLocalDateTime() : null; } catch (SQLException ex) { return null; } }

    private record SelectionFilter(String whereClause, MapSqlParameterSource params) {}
}

package my.maleva.api.repo;

import my.maleva.api.model.PlanningDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlanningDetailsRepository extends JpaRepository<PlanningDetails, Integer> {

    /**
     * Find all details by planning master reference ID
     */
    List<PlanningDetails> findByPlanningMasterRefId(Integer planningMasterRefId);

    /**
     * Find details by sale order master reference ID
     */
    List<PlanningDetails> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find details by truck reference ID
     */
    List<PlanningDetails> findByTruckRefId(Integer truckRefId);

    /**
     * Find details by planning master and sorted
     */
    @Query("SELECT p FROM PlanningDetails p WHERE p.planningMasterRefId = :masterRefId " +
            "ORDER BY p.sortBy ASC")
    List<PlanningDetails> findByPlanningMasterRefIdSorted(@Param("masterRefId") Integer masterRefId);

    /**
     * Delete all details by planning master reference ID
     */
    void deleteByPlanningMasterRefId(Integer planningMasterRefId);

    /**
     * Find planning details for SelectPLANING with joins
     * Uses correct table names: PLANINGMaster and PLANINGDetails
     * Fetches comprehensive logistics and tracking data
     */
    @Query(value = "SELECT B.Id, B.SDId, B.PLANINGMasterRefId, B.SaleOrderMasterRefId, " +
            "B.TruckRefid, ISNULL(T.TruckName, '') as TruckName, ISNULL(D.DriverName, '') as DriverName, " +
            "SM.CNumberDisplay as JobNo, FORMAT(ISNULL(SM.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as JobDate, " +
            "ISNULL(JS.Name, '') as JobStatus, ISNULL(B.OriginD, '') as OriginD, " +
            "ISNULL(B.DestinationD, '') as DestinationD, ISNULL(C.CustomerName, '') as CustomerName, " +
            "ISNULL(B.Remarks, '') as Remarks, ISNULL(B.TruckNameD, '') as TruckNameD, " +
            "ISNULL(B.DriverNameD, '') as DriverNameD, B.SortBy, " +
            "FORMAT(ISNULL(B.PickupDateD, '1900-01-01'), 'dd/MM/yyyy') as PickupDateD, " +
            "FORMAT(ISNULL(B.DeliveryDateD, '1900-01-01'), 'dd/MM/yyyy') as DeliveryDateD, " +
            "ISNULL(B.pickuptimelist, '') as pickuptimelist, " +
            "ISNULL(B.pickupQuantitylist, '') as pickupQuantitylist, " +
            "ISNULL(B.DeliveryQuantitylist, '') as DeliveryQuantitylist, " +
            "ISNULL(B.Delivertimelist, '') as Delivertimelist " +
            "FROM PLANINGDetails B WITH(NOLOCK) " +
            "INNER JOIN PLANINGMaster A WITH(NOLOCK) ON B.PLANINGMasterRefId = A.Id " +
            "INNER JOIN SaleOrderMaster SM WITH(NOLOCK) ON SM.Id = B.SaleOrderMasterRefId " +
            "LEFT JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId " +
            "LEFT JOIN TruckMaster T WITH(NOLOCK) ON T.Id = B.TruckRefid " +
            "LEFT JOIN DriverMaster D WITH(NOLOCK) ON D.Id = T.DriverRefId " +
            "LEFT JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = SM.JStatus " +
            "WHERE A.CompanyRefId = :companyId AND A.Active = 1 " +
            "AND (:employeeId IS NULL OR A.EmployeeRefId = :employeeId) " +
            "AND (:fromDate IS NULL OR :toDate IS NULL OR A.SaleDate BETWEEN :fromDate AND :toDate) " +
            "ORDER BY B.SortBy ASC",
            nativeQuery = true)
    List<Object[]> findDetailsForSelectPlanning(
            @Param("companyId") Integer companyId,
            @Param("employeeId") Integer employeeId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * PLANINGSearch - Search planning details by port and date range
     * Searches SaleOrderMaster with Sport/Oport filters
     * Returns comprehensive details for matching records
     */
    @Query(value = "SELECT ISNULL(E.EmployeeName, '') as EmployeeName, SM.PickupDate, " +
            "CASE WHEN SM.PickupDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.PickupDate, 20) END as PickupDateD, " +
            "CASE WHEN SM.DeliveryDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.DeliveryDate, 20) END as DeliveryDateD, " +
            "SM.Id, SM.CNumberDisplay as JobNo, " +
            "CASE WHEN SM.PickupDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.PickupDate, 20) END as SPickupDate, " +
            "CASE WHEN SM.DeliveryDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.DeliveryDate, 20) END as SDeliveryDate, " +
            "SM.WareHouseEnterDate, SM.WareHouseExitDate, " +
            "CASE WHEN SM.WareHouseEnterDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.WareHouseEnterDate, 20) END as SWareHouseEnterDate, " +
            "CASE WHEN SM.WareHouseExitDate IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.WareHouseExitDate, 20) END as SWareHouseExitDate, " +
            "SM.WareHouseAddress, SM.Origin, SM.Destination, SM.Origin as OriginD, " +
            "SM.Destination as DestinationD, (SM.Quantity + '/' + SM.TotalWeight) as pkg, " +
            "COALESCE(NULLIF(SM.Loadingvesselname, ''), SM.Offvesselname) as VesselName, " +
            "CASE WHEN SM.SaleDate IS NULL THEN '' ELSE CONVERT(VARCHAR(10), SM.SaleDate, 103) END as JobDate, " +
            "C.CustomerName, '' as TruckName, 0 as TruckRefid, '' as Remarks, " +
            "ISNULL(JS.Name, '') as JobStatus, SM.PickupAddress, SM.DeliveryAddress, " +
            "CASE WHEN SM.ETA IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.ETA, 20) END as LETA, " +
            "CASE WHEN SM.OETA IS NULL THEN '' ELSE CONVERT(VARCHAR(26), SM.OETA, 20) END as OETA, " +
            "JT.Name as JobName, SM.AWBNo, SM.BLCopy, SM.SPort, SM.OPort, SM.truckSize, " +
            "SM.pickuptimelist, SM.pickupQuantitylist, SM.DeliveryQuantitylist, SM.Delivertimelist, " +
            "0 as SDId, 0 as PLANINGMasterRefId, 0 as SaleOrderMasterRefId, 0 as SortBy, " +
            "'' as TruckNameD, '' as DriverNameD " +
            "FROM SaleOrderMaster SM WITH(NOLOCK) " +
            "INNER JOIN Customer C WITH(NOLOCK) ON C.Id = SM.CustomerRefId " +
            "INNER JOIN JobTypeMaster JT WITH(NOLOCK) ON JT.Id = SM.JobMasterRefId " +
            "LEFT JOIN JobStatusMaster JS WITH(NOLOCK) ON JS.Id = SM.JStatus " +
            "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = SM.EmployeeRefId " +
            "WHERE SM.CompanyRefId = :companyId AND SM.Active != 2 " +
            "AND CAST(SM.PickupDate as DATE) BETWEEN :fromDate AND :toDate " +
            "AND (:employeeId IS NULL OR :employeeId = 0 OR SM.EmployeeRefId = :employeeId) " +
            "AND (:applyPortFilter = 0 OR SM.SPort IN (:searchPorts) OR SM.OPort IN (:searchPorts)) " +
            "ORDER BY SM.PickupDate ASC",
            nativeQuery = true)
    List<Object[]> planningSearch(
            @Param("companyId") Integer companyId,
            @Param("applyPortFilter") Integer applyPortFilter,
            @Param("searchPorts") List<String> searchPorts,
            @Param("employeeId") Integer employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}

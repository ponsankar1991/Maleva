package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.entity.RTIRouteActivities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RTIRouteActivitiesRepository extends JpaRepository<RTIRouteActivities, Integer> {
    List<RTIRouteActivities> findByRtiMasterRefIdOrderBySequenceNoAsc(Integer rtiMasterRefId);
    void deleteByRtiMasterRefId(Integer rtiMasterRefId);

    @Query(value = """
        SELECT 
            CASE 
                WHEN NULLIF(RM.OutsideTruck, '') IS NOT NULL AND TM.TruckName != 'NONE' THEN TM.TruckName + ' - ' + RM.OutsideTruck
                WHEN NULLIF(RM.OutsideTruck, '') IS NOT NULL THEN RM.OutsideTruck
                ELSE TM.TruckName 
            END AS lorryNo,
            CASE 
                WHEN NULLIF(RM.OutsideDriver, '') IS NOT NULL AND DM.DriverName != 'NONE' THEN DM.DriverName + ' - ' + RM.OutsideDriver
                WHEN NULLIF(RM.OutsideDriver, '') IS NOT NULL THEN RM.OutsideDriver
                ELSE DM.DriverName
            END AS driverName,
            COALESCE(NULLIF(RA.DriverNumber, ''), DM.MobileNo) AS driverNumber,
            EM.EmployeeName AS agentName,
            COALESCE(RA.AgentMobileNo, EM.MobileNo, DM.MobileNo) AS contact,
            COALESCE(LAG(RA.LocationName) OVER (PARTITION BY RA.RTIMasterRefId ORDER BY RA.SequenceNo), SOM.Origin, '') AS fromLocation,
            RA.ETA AS eta,
            RA.ActivityType AS jobType,
            LTRIM(RTRIM(UPPER(RA.LocationName))) AS port,
            RA.Remarks AS remarks,
            RA.FullRoute AS fullRoute,
            RA.MarqisStatus AS marqisStatus
        FROM RTIRouteActivities RA
        INNER JOIN RTIMaster RM ON RA.RTIMasterRefId = RM.Id
        LEFT JOIN SaleOrderMaster SOM ON RM.CNumber = SOM.CNumber AND RM.CompanyRefId = SOM.CompanyRefId
        LEFT JOIN TruckMaster TM ON RM.TruckRefid = TM.Id
        LEFT JOIN DriverMaster DM ON RM.DriverRefid = DM.Id
        INNER JOIN EmployeeMaster EM ON RA.EmployeeRefId = EM.Id
        WHERE RA.CompanyRefId = :companyRefId
          AND RA.ETA >= :fromDate 
          AND RA.ETA <= :toDate
        ORDER BY RA.ETA ASC
    """, nativeQuery = true)
    List<Object[]> getForwardingPlanningReport(
            @Param("companyRefId") Integer companyRefId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}

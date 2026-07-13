package my.maleva.api.module.rti.repository.impl;

import my.maleva.api.common.exception.RtiJobWiseQueryException;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import my.maleva.api.module.rti.mapper.RtiJobWiseRowMapper;
import my.maleva.api.module.rti.repository.RtiJobWiseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class RtiJobWiseRepositoryImpl implements RtiJobWiseRepository {

    private static final Logger logger = LoggerFactory.getLogger(RtiJobWiseRepositoryImpl.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RtiJobWiseRowMapper rowMapper;

    public RtiJobWiseRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, RtiJobWiseRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public List<RtiJobWiseViewResponse> findJobWiseView(LocalDate fromDate, LocalDate toDate) {
        String sql = """
            WITH Legs AS (
                SELECT 
                    RD.RTIMasterRefId,
                    RD.PickupDateD, 
                    RD.DeliveryDateD, 
                    RD.OriginD, 
                    RD.DestinationD,
                    RM.CNumberDisplay     AS RTINumber,
                    RM.Remarks, 
                    RM.PickupCount, 
                    RM.DropCount,
                    SM.CNumberDisplay     AS SaleOrderNumber,
                    SM.Loadingvesselname  AS VesselNameRaw,
                    SM.Commodity, 
                    SM.Quantity, 
                    SM.TruckSize,
                    DM.DriverName,
                    TM.TruckNumber, 
                    TM.TruckType
                FROM RTIDetails RD WITH (NOLOCK)
                INNER JOIN RTIMaster RM WITH (NOLOCK)       
                    ON RM.Id = RD.RTIMasterRefId
                LEFT JOIN SaleOrderMaster SM WITH (NOLOCK)  
                    ON SM.Id = RD.SaleOrderMasterRefId
                LEFT JOIN DriverMaster DM WITH (NOLOCK)     
                    ON DM.Id = RM.DriverRefid
                LEFT JOIN TruckMaster TM WITH (NOLOCK)      
                    ON TM.Id = RM.TruckRefid
                WHERE RD.PickupDateD >= :fromDate
                  AND RD.PickupDateD <  DATEADD(DAY, 1, :toDate)
                  AND RM.Active = 1
            )
            SELECT
                L.RTIMasterRefId                                     AS JobRefId,
                MAX(L.RTINumber)                                     AS RTINumber,
                STRING_AGG(L.SaleOrderNumber, ' / ')                 AS JobNumber,
                STRING_AGG(L.VesselNameRaw, ' / ')                   AS VesselName,
                STRING_AGG(
                    CONCAT_WS(' - ', NULLIF(L.Commodity, ''), NULLIF(CAST(L.Quantity AS VARCHAR(50)), '')), 
                    ' / '
                )                                                    AS CargoDetails,
                L.OriginD                                             AS CollectAt,
                L.DestinationD                                        AS DeliveryAt,
                MIN(L.PickupDateD)                                    AS CollectionDate,
                MAX(L.DeliveryDateD)                                  AS DeliveryDate,
                MAX(L.TruckNumber)                                    AS TruckNumber,
                MAX(L.DriverName)                                     AS DriverName,
                MAX(COALESCE(NULLIF(L.TruckSize,''), L.TruckType))    AS TruckSize,
                MAX(L.PickupCount)                                    AS PickupCount,
                MAX(L.DropCount)                                      AS DropCount,
                MAX(L.Remarks)                                        AS Remarks,
                COUNT(*)                                              AS LegCount
            FROM Legs L
            GROUP BY L.RTIMasterRefId, L.OriginD, L.DestinationD
            ORDER BY L.RTIMasterRefId, MIN(L.PickupDateD);
            """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fromDate", java.sql.Date.valueOf(fromDate));
        params.addValue("toDate", java.sql.Date.valueOf(toDate));

        long startTime = System.currentTimeMillis();
        try {
            List<RtiJobWiseViewResponse> results = jdbcTemplate.query(sql, params, rowMapper);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Query and object mapping completed in {} ms. Returned {} rows.", duration, results.size());
            return results;
        } catch (DataAccessException e) {
            logger.error("Database error while fetching RTI Job Wise View", e);
            throw new RtiJobWiseQueryException("Failed to execute RTI Job Wise View query.", e);
        }
    }
}

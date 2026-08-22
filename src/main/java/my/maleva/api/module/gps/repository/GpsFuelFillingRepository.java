package my.maleva.api.module.gps.repository;

import my.maleva.api.module.fleet.entity.FuelFillings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS-side access to the FuelFillings table.
 *
 * The entity itself lives in the fleet module; this repository exists so the GPS
 * sync owns its own query surface (window reads and the delete half of the
 * upsert) without widening the fleet CRUD repository.
 */
@Repository
public interface GpsFuelFillingRepository extends JpaRepository<FuelFillings, Integer> {

    List<FuelFillings> findByCompanyRefIdAndTimeBetweenOrderByTimeAsc(
            Integer companyRefId, LocalDateTime from, LocalDateTime to);

    List<FuelFillings> findByCompanyRefIdAndTruckRefIdAndTimeBetweenOrderByTimeAsc(
            Integer companyRefId, Integer truckRefId, LocalDateTime from, LocalDateTime to);

    /**
     * Every filling of one truck on one day, oldest first. Feeds the matching
     * against the fuel entries of that day.
     */
    @Query("select f from FuelFillings f "
            + "where f.companyRefId = :companyRefId "
            + "and f.truckRefId = :truckRefId "
            + "and f.time >= :dayStart and f.time < :nextDayStart "
            + "order by f.time asc")
    List<FuelFillings> findForTruckOnDay(@Param("companyRefId") Integer companyRefId,
                                         @Param("truckRefId") Integer truckRefId,
                                         @Param("dayStart") LocalDateTime dayStart,
                                         @Param("nextDayStart") LocalDateTime nextDayStart);

    /**
     * Delete half of the upsert: the legacy job deleted by truck plus exact time
     * before inserting, so re-running a window overwrites rather than duplicates.
     * Company is included here, which the legacy delete omitted.
     */
    @Modifying
    @Query("delete from FuelFillings f "
            + "where f.companyRefId = :companyRefId and f.truckRefId = :truckRefId and f.time = :time")
    int deleteForUpsert(@Param("companyRefId") Integer companyRefId,
                        @Param("truckRefId") Integer truckRefId,
                        @Param("time") LocalDateTime time);
}

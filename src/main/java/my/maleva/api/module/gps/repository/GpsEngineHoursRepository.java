package my.maleva.api.module.gps.repository;

import my.maleva.api.module.fleet.entity.EngineHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/** GPS-side access to the EngineHours table. */
@Repository
public interface GpsEngineHoursRepository extends JpaRepository<EngineHours, Integer> {

    List<EngineHours> findByCompanyRefIdAndBeginTimeBetweenOrderByBeginTimeAsc(
            Integer companyRefId, LocalDateTime from, LocalDateTime to);

    List<EngineHours> findByCompanyRefIdAndTruckRefIdAndBeginTimeBetweenOrderByBeginTimeAsc(
            Integer companyRefId, Integer truckRefId, LocalDateTime from, LocalDateTime to);

    /** Engine hours are keyed on the interval start, matching the legacy delete. */
    @Modifying
    @Query("delete from EngineHours e "
            + "where e.companyRefId = :companyRefId and e.truckRefId = :truckRefId "
            + "and e.beginTime = :beginTime")
    int deleteForUpsert(@Param("companyRefId") Integer companyRefId,
                        @Param("truckRefId") Integer truckRefId,
                        @Param("beginTime") LocalDateTime beginTime);
}

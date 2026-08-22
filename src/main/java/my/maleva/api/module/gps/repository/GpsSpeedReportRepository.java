package my.maleva.api.module.gps.repository;

import my.maleva.api.module.fleet.entity.SpeedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/** GPS-side access to the SpeedReport table. */
@Repository
public interface GpsSpeedReportRepository extends JpaRepository<SpeedReport, Integer> {

    List<SpeedReport> findByCompanyRefIdAndTimeBetweenOrderByTimeAsc(
            Integer companyRefId, LocalDateTime from, LocalDateTime to);

    List<SpeedReport> findByCompanyRefIdAndTruckRefIdAndTimeBetweenOrderByTimeAsc(
            Integer companyRefId, Integer truckRefId, LocalDateTime from, LocalDateTime to);

    @Modifying
    @Query("delete from SpeedReport s "
            + "where s.companyRefId = :companyRefId and s.truckRefId = :truckRefId and s.time = :time")
    int deleteForUpsert(@Param("companyRefId") Integer companyRefId,
                        @Param("truckRefId") Integer truckRefId,
                        @Param("time") LocalDateTime time);
}

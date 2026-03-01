package my.maleva.api.repo;

import my.maleva.api.model.SpeedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SpeedReportRepository - Repository for SpeedReport
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SpeedReportRepository extends JpaRepository<SpeedReport, Integer> {

    /**
     * Find all SpeedReport records by company ID
     */
    List<SpeedReport> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all SpeedReport records by truck ID
     */
    List<SpeedReport> findByTruckRefId(Integer truckRefId);

    /**
     * Find SpeedReport records by company and truck
     */
    List<SpeedReport> findByCompanyRefIdAndTruckRefId(Integer companyRefId, Integer truckRefId);

    /**
     * Find SpeedReport records by time range
     */
    List<SpeedReport> findByTimeGreaterThanEqualAndTimeLessThanEqual(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find SpeedReport records by company and time range
     */
    List<SpeedReport> findByCompanyRefIdAndTimeGreaterThanEqualAndTimeLessThanEqual(
        Integer companyRefId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Count SpeedReport records by company ID
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count SpeedReport records by truck ID
     */
    long countByTruckRefId(Integer truckRefId);
}


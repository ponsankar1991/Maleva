package my.maleva.api.repo;

import my.maleva.api.model.TollEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TollEntryRepository - Repository for TollEntry
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TollEntryRepository extends JpaRepository<TollEntry, Integer> {

    /**
     * Find all TollEntry records by company ID
     */
    List<TollEntry> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all active TollEntry records by company
     */
    List<TollEntry> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find TollEntry by C Number and Company
     */
    Optional<TollEntry> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find all TollEntry records by user ID
     */
    List<TollEntry> findByUserRefId(Integer userRefId);

    /**
     * Find all TollEntry records by employee ID
     */
    List<TollEntry> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find all TollEntry records by truck ID
     */
    List<TollEntry> findByTruckRefid(Integer truckRefid);

    /**
     * Find TollEntry records by date range
     */
    List<TollEntry> findBySaleDateGreaterThanEqualAndSaleDateLessThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find TollEntry records by company and date range
     */
    List<TollEntry> findByCompanyRefIdAndSaleDateGreaterThanEqualAndSaleDateLessThanEqual(
        Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count TollEntry records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active TollEntry records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}


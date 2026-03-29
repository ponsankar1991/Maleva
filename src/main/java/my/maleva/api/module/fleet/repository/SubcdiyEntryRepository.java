package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.SubcdiyEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SubcdiyEntryRepository - Repository for SubcdiyEntry
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SubcdiyEntryRepository extends JpaRepository<SubcdiyEntry, Integer> {

    /**
     * Find all SubcdiyEntry records by active status
     */
    List<SubcdiyEntry> findByActive(Integer active);

    /**
     * Find all active SubcdiyEntry records
     */
    List<SubcdiyEntry> findByActiveEquals(Integer active);

    /**
     * Find SubcdiyEntry records by entry date
     */
    List<SubcdiyEntry> findByEntryDate(LocalDate entryDate);

    /**
     * Find SubcdiyEntry records by date range
     */
    List<SubcdiyEntry> findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(
        LocalDate startDate, LocalDate endDate);

    /**
     * Find SubcdiyEntry records by amount range
     */
    List<SubcdiyEntry> findByAmountGreaterThanEqualAndAmountLessThanEqual(
        BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find SubcdiyEntry records by created date range
     */
    List<SubcdiyEntry> findByCreatedDateGreaterThanEqualAndCreatedDateLessThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count SubcdiyEntry records by active status
     */
    long countByActive(Integer active);

    /**
     * Check if SubcdiyEntry exists by entry date
     */
    boolean existsByEntryDate(LocalDate entryDate);
}


package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.Summon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SummonRepository - Repository for Summon
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SummonRepository extends JpaRepository<Summon, Integer> {

    /**
     * Find all Summon records by truck name
     */
    List<Summon> findByTruckName(String truckName);

    /**
     * Find all Summon records by driver name
     */
    List<Summon> findByDriverName(String driverName);

    /**
     * Find all Summon records by company ID
     */
    List<Summon> findByComid(Integer comid);

    /**
     * Find Summon records by entry date
     */
    List<Summon> findByEntryDate(LocalDate entryDate);

    /**
     * Find Summon records by date range
     */
    List<Summon> findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(
        LocalDate startDate, LocalDate endDate);

    /**
     * Find Summon records by amount range
     */
    List<Summon> findByAmountGreaterThanEqualAndAmountLessThanEqual(
        BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find Summon records by country
     */
    List<Summon> findByCountry(String country);

    /**
     * Find Summon by truck name and driver name
     */
    Optional<Summon> findByTruckNameAndDriverName(String truckName, String driverName);

    /**
     * Count Summon records by company ID
     */
    long countByComid(Integer comid);

    /**
     * Count Summon records by country
     */
    long countByCountry(String country);
}


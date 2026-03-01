package my.maleva.api.repo;

import my.maleva.api.model.TruckSpareParts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TruckSparePartsRepository - Repository for TruckSpareParts
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TruckSparePartsRepository extends JpaRepository<TruckSpareParts, Integer> {

    /**
     * Find all TruckSpareParts records by company ID
     */
    List<TruckSpareParts> findByComid(Integer comid);

    /**
     * Find all TruckSpareParts records by truck name
     */
    List<TruckSpareParts> findByTruckName(String truckName);

    /**
     * Find all TruckSpareParts records by truck name and company
     */
    List<TruckSpareParts> findByTruckNameAndComid(String truckName, Integer comid);

    /**
     * Find all TruckSpareParts records by driver name
     */
    List<TruckSpareParts> findByDriverName(String driverName);

    /**
     * Find all TruckSpareParts records by driver name and company
     */
    List<TruckSpareParts> findByDriverNameAndComid(String driverName, Integer comid);

    /**
     * Find all TruckSpareParts records by spare parts type
     */
    List<TruckSpareParts> findBySpareParts(String spareParts);

    /**
     * Find all TruckSpareParts records by entry date range
     */
    List<TruckSpareParts> findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(
        LocalDate startDate, LocalDate endDate);

    /**
     * Find all TruckSpareParts records by company and entry date range
     */
    List<TruckSpareParts> findByComidAndEntryDateGreaterThanEqualAndEntryDateLessThanEqual(
        Integer comid, LocalDate startDate, LocalDate endDate);

    /**
     * Count TruckSpareParts records by company
     */
    long countByComid(Integer comid);

    /**
     * Count TruckSpareParts records by truck name and company
     */
    long countByTruckNameAndComid(String truckName, Integer comid);
}


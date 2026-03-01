package my.maleva.api.repo;

import my.maleva.api.model.TollEntryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TollEntryDetailsRepository - Repository for TollEntryDetails
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TollEntryDetailsRepository extends JpaRepository<TollEntryDetails, Integer> {

    /**
     * Find all TollEntryDetails records by toll entry master ID
     */
    List<TollEntryDetails> findByTollEntryMasterRefId(Integer tollEntryMasterRefId);

    /**
     * Find all TollEntryDetails records by transaction type
     */
    List<TollEntryDetails> findByTransType(String transType);

    /**
     * Find all TollEntryDetails records by vehicle class
     */
    List<TollEntryDetails> findByVehicleClass(Integer vehicleClass);

    /**
     * Find all TollEntryDetails records by entry date range
     */
    List<TollEntryDetails> findByEntryDateGreaterThanEqualAndEntryDateLessThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all TollEntryDetails records by vehicle number
     */
    List<TollEntryDetails> findByVehicleNumber(String vehicleNumber);

    /**
     * Count TollEntryDetails records by toll entry master ID
     */
    long countByTollEntryMasterRefId(Integer tollEntryMasterRefId);

    /**
     * Delete all TollEntryDetails by toll entry master ID
     */
    void deleteByTollEntryMasterRefId(Integer tollEntryMasterRefId);
}


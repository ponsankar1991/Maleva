package my.maleva.api.repo;

import my.maleva.api.model.TruckMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TruckMasterRepository - Repository for TruckMaster
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TruckMasterRepository extends JpaRepository<TruckMaster, Integer> {

    /**
     * Find all TruckMaster records by company ID
     */
    List<TruckMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all active TruckMaster records by company
     */
    List<TruckMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find TruckMaster by truck name and company
     */
    Optional<TruckMaster> findByTruckNameAndCompanyRefId(String truckName, Integer companyRefId);

    /**
     * Find TruckMaster by truck number and company
     */
    Optional<TruckMaster> findByTruckNumberAndCompanyRefId(String truckNumber, Integer companyRefId);

    /**
     * Find TruckMaster by C Number and Company
     */
    Optional<TruckMaster> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find all TruckMaster records by truck type
     */
    List<TruckMaster> findByTruckType(String truckType);

    /**
     * Find all TruckMaster records by company and truck type
     */
    List<TruckMaster> findByCompanyRefIdAndTruckType(Integer companyRefId, String truckType);

    /**
     * Find all TruckMaster records by vehicle type
     */
    List<TruckMaster> findByVehicleType(String vehicleType);

    /**
     * Count TruckMaster records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active TruckMaster records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if truck number exists for company
     */
    boolean existsByTruckNumberAndCompanyRefId(String truckNumber, Integer companyRefId);
}


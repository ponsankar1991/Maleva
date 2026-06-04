package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.common.dto.ComboListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /**
     * Get Trucks as ComboListModel for dropdown/UI
     * Equivalent to .NET GetTruck method
     * SELECT Id, TruckName as AccountName FROM TruckMaster
     * WHERE CompanyRefId = ? AND Active = 1
     */
    @Query(value = "SELECT new my.maleva.api.common.dto.ComboListModel(t.id, t.truckName) " +
           "FROM TruckMaster t " +
           "WHERE t.companyRefId = :companyId AND t.active = 1 " +
           "ORDER BY t.truckName ASC")
    List<ComboListModel> getTruckCombo(@Param("companyId") Integer companyId);

    /**
     * Get Trucks as ComboListModel with Type filter
     * Equivalent to .NET GetTruck method with type parameter
     * SELECT Id, TruckName as AccountName FROM TruckMaster
     * WHERE CompanyRefId = ? AND Active = 1 AND TruckType = ?
     */
    @Query(value = "SELECT new my.maleva.api.common.dto.ComboListModel(t.id, t.truckName) " +
           "FROM TruckMaster t " +
           "WHERE t.companyRefId = :companyId AND t.active = 1 AND t.truckType = :truckType " +
           "ORDER BY t.truckName ASC")
    List<ComboListModel> getTruckComboByType(
        @Param("companyId") Integer companyId,
        @Param("truckType") String truckType);

    boolean existsByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);
}

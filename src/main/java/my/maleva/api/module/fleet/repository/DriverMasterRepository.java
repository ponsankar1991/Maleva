package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.DriverMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DriverMasterRepository - Data access layer for DriverMaster entity
 * Supports CRUD operations and search queries
 */
@Repository
public interface DriverMasterRepository extends JpaRepository<DriverMaster, Integer> {

    /**
     * Find all drivers by company ID
     */
    List<DriverMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all drivers by company ID, excluding deleted records
     */
    List<DriverMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    /**
     * Find driver by account reference ID
     */
    List<DriverMaster> findByAccountRefid(Integer accountRefid);

    /**
     * Check if driver exists with specific criteria
     */
    boolean existsByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    /**
     * Find driver by name and company ID
     */
    Optional<DriverMaster> findByDriverNameAndCompanyRefId(String driverName, Integer companyRefId);

    /**
     * Find driver by mobile number and company ID
     */
    Optional<DriverMaster> findByMobileNoAndCompanyRefId(String mobileNo, Integer companyRefId);
}

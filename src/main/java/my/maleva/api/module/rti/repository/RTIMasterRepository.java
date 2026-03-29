package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.entity.RTIMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RTIMasterRepository
 * Spring Data JPA Repository for RTIMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface RTIMasterRepository extends JpaRepository<RTIMaster, Integer> {

    /**
     * Find all RTIMaster records by company ID
     */
    List<RTIMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active RTIMaster records by company ID
     */
    List<RTIMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find RTIMaster by CNumber
     */
    Optional<RTIMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find RTIMaster by employee
     */
    List<RTIMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Find RTIMaster by agent
     */
    List<RTIMaster> findByCompanyRefIdAndAgentMasterRefId(Integer companyRefId, Integer agentMasterRefId);

    /**
     * Find RTIMaster by date range
     */
    List<RTIMaster> findByCompanyRefIdAndSaleDateBetween(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find RTIMaster by CNumberDisplay
     */
    Optional<RTIMaster> findByCNumberDisplay(String cNumberDisplay);

    /**
     * Find sleeping RTI records
     */
    List<RTIMaster> findByCompanyRefIdAndSleeping(Integer companyRefId, Integer sleeping);

    /**
     * Find RTIMaster by truck
     */
    List<RTIMaster> findByCompanyRefIdAndTruckRefId(Integer companyRefId, Integer truckRefId);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count RTIMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active RTIMaster by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}


package my.maleva.api.module.prealert.repository;

import my.maleva.api.module.prealert.entity.PreAlertMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreAlertMasterRepository extends JpaRepository<PreAlertMaster, Integer> {

    /**
     * Find all PreAlertMaster records by company ID
     */
    List<PreAlertMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active PreAlertMaster records by company ID
     */
    List<PreAlertMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find PreAlertMaster by customer reference ID
     */
    List<PreAlertMaster> findByCustomerMasterRefId(Integer customerMasterRefId);

    /**
     * Find PreAlertMaster by job type reference ID
     */
    List<PreAlertMaster> findByJobTypeMasterRefId(Integer jobTypeMasterRefId);

    /**
     * Find PreAlertMaster by port
     */
    List<PreAlertMaster> findByPort(String port);

    /**
     * Find PreAlertMaster by vessel name
     */
    List<PreAlertMaster> findByVessel(String vessel);

    /**
     * Find PreAlertMaster records within a date range
     */
    @Query("SELECT p FROM PreAlertMaster p WHERE p.companyRefId = :companyId " +
            "AND p.entryDate >= :fromDate AND p.entryDate <= :toDate")
    List<PreAlertMaster> findByDateRange(
            @Param("companyId") Integer companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /**
     * Find PreAlertMaster by CNumber
     */
    Optional<PreAlertMaster> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find PreAlertMaster by CNumberDisplay
     */
    Optional<PreAlertMaster> findByCNumberDisplay(String cNumberDisplay);

    /**
     * Find all PreAlertMaster records by company ID and customer ID
     */
    List<PreAlertMaster> findByCompanyRefIdAndCustomerMasterRefId(Integer companyRefId, Integer customerMasterRefId);

    /**
     * Find all PreAlertMaster records by sale order master reference ID
     */
    List<PreAlertMaster> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count active PreAlertMaster records by company ID
     */
    Long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}


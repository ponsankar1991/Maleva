package my.maleva.api.module.prealert.repository;

import my.maleva.api.module.prealert.entity.PreAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PreAlertRepository extends JpaRepository<PreAlert, Integer> {

    /**
     * Find all PreAlert records by company ID
     */
    List<PreAlert> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active PreAlert records by company ID
     */
    List<PreAlert> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find all PreAlert records by PreAlertMaster reference ID
     */
    List<PreAlert> findByPreAlertMasterRefId(Integer preAlertMasterRefId);

    /**
     * Find all PreAlert records by customer reference ID
     */
    List<PreAlert> findByCustomerMasterRefId(Integer customerMasterRefId);

    /**
     * Find all PreAlert records by employee reference ID
     */
    List<PreAlert> findByEmployeeMasterRefId(Integer employeeMasterRefId);

    /**
     * Find all PreAlert records by job type reference ID
     */
    List<PreAlert> findByJobTypeMasterRefId(Integer jobTypeMasterRefId);

    /**
     * Find all PreAlert records by job status reference ID
     */
    List<PreAlert> findByJobStatusMasterRefId(Integer jobStatusMasterRefId);

    /**
     * Find all PreAlert records by boarding officer reference ID
     */
    List<PreAlert> findByBoardingOfficerRefId(Integer boardingOfficerRefId);

    /**
     * Find all PreAlert records by sale order master reference ID
     */
    List<PreAlert> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find PreAlert records by vessel name
     */
    List<PreAlert> findByVessel(String vessel);

    /**
     * Find PreAlert records by port
     */
    List<PreAlert> findByPort(String port);

    /**
     * Find PreAlert records by job number
     */
    List<PreAlert> findByJobNo(String jobNo);

    /**
     * Find all PreAlert records by PreAlertMaster ID and active status
     */
    List<PreAlert> findByPreAlertMasterRefIdAndActive(Integer preAlertMasterRefId, Integer active);

    /**
     * Count PreAlert records by PreAlertMaster ID
     */
    Long countByPreAlertMasterRefId(Integer preAlertMasterRefId);

    /**
     * Delete PreAlert records by PreAlertMaster ID
     */
    void deleteByPreAlertMasterRefId(Integer preAlertMasterRefId);

    /**
     * Find PreAlert records by company ID and customer ID
     */
    List<PreAlert> findByCompanyRefIdAndCustomerMasterRefId(Integer companyRefId, Integer customerMasterRefId);
}



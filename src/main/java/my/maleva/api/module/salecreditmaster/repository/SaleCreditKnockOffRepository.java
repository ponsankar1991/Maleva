package my.maleva.api.module.salecreditmaster.repository;

import my.maleva.api.module.salecreditmaster.entity.SaleCreditKnockOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleCreditKnockOffRepository
 * Spring Data JPA Repository for SaleCreditKnockOff entity
 */
@Repository
public interface SaleCreditKnockOffRepository extends JpaRepository<SaleCreditKnockOff, Integer> {

    /**
     * Find all SaleCreditKnockOff records by Sale Credit Master Reference ID
     */
    List<SaleCreditKnockOff> findBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Find SaleCreditKnockOff records by company ID
     */
    List<SaleCreditKnockOff> findByCompanyRefId(Integer companyRefId);

    /**
     * Find SaleCreditKnockOff records by Sale Master Reference ID
     */
    List<SaleCreditKnockOff> findBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Find SaleCreditKnockOff records by customer ID
     */
    List<SaleCreditKnockOff> findByCustomerOpenRefId(Integer customerOpenRefId);

    /**
     * Count knock-off records by Sale Credit Master Reference ID
     */
    long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Find records by company and Sale Credit Master
     */
    List<SaleCreditKnockOff> findByCompanyRefIdAndSaleCreditMasterRefId(Integer companyRefId, Integer saleCreditMasterRefId);
}


package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.entity.SportSaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SportSaleOrderRepository - Repository for SportSaleOrder
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SportSaleOrderRepository extends JpaRepository<SportSaleOrder, Integer> {

    /**
     * Find all SportSaleOrder records by company ID
     */
    List<SportSaleOrder> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active SportSaleOrder records by company ID
     */
    List<SportSaleOrder> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find all SportSaleOrder records by customer ID
     */
    List<SportSaleOrder> findByCustomerRefId(Integer customerRefId);

    /**
     * Find all SportSaleOrder records by company and customer
     */
    List<SportSaleOrder> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);

    /**
     * Find all SportSaleOrder records by job master ID
     */
    List<SportSaleOrder> findByJobMasterRefId(Integer jobMasterRefId);

    /**
     * Find all SportSaleOrder records by employee ID
     */
    List<SportSaleOrder> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find all SportSaleOrder records by status
     */
    List<SportSaleOrder> findByJStatus(Integer jStatus);

    /**
     * Find SportSaleOrder by AWB Number
     */
    Optional<SportSaleOrder> findByAwbNo(String awbNo);

    /**
     * Find SportSaleOrder records by date range
     */
    List<SportSaleOrder> findByCreatedDateGreaterThanEqualAndCreatedDateLessThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count SportSaleOrder records by company ID
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active SportSaleOrder records by company ID
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}


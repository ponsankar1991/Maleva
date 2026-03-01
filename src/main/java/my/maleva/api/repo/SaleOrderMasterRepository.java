package my.maleva.api.repo;

import my.maleva.api.model.SaleOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderMasterRepository - Repository for SaleOrderMaster
 */
@Repository
public interface SaleOrderMasterRepository extends JpaRepository<SaleOrderMaster, Integer> {

    List<SaleOrderMaster> findByCompanyRefId(Integer companyRefId);
    List<SaleOrderMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);
    List<SaleOrderMaster> findByCustomerRefId(Integer customerRefId);
    List<SaleOrderMaster> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);
    List<SaleOrderMaster> findByEmployeeRefId(Integer employeeRefId);
    List<SaleOrderMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);
    List<SaleOrderMaster> findByUserRefId(Integer userRefId);
    Optional<SaleOrderMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    @Query("SELECT s FROM SaleOrderMaster s WHERE s.companyRefId = :companyRefId " +
           "AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<SaleOrderMaster> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    List<SaleOrderMaster> findByJobMasterRefId(Integer jobMasterRefId);
    List<SaleOrderMaster> findByAgentMasterRefId(Integer agentMasterRefId);
    List<SaleOrderMaster> findByDriverRefid(Integer driverRefid);
    long countByCompanyRefId(Integer companyRefId);
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}


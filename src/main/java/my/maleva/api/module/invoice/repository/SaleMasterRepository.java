package my.maleva.api.module.invoice.repository;

import my.maleva.api.module.invoice.entity.SaleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleMasterRepository
 * Spring Data JPA Repository for SaleMaster entity
 */
@Repository
public interface SaleMasterRepository extends JpaRepository<SaleMaster, Integer> {

    List<SaleMaster> findByCompanyRefId(Integer companyRefId);
    List<SaleMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);
    List<SaleMaster> findByCustomerRefId(Integer customerRefId);
    List<SaleMaster> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);
    List<SaleMaster> findByEmployeeRefId(Integer employeeRefId);
    List<SaleMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);
    List<SaleMaster> findByUserRefId(Integer userRefId);
    List<SaleMaster> findByCompanyRefIdAndBillType(Integer companyRefId, String billType);
    List<SaleMaster> findByCompanyRefIdAndSaleType(Integer companyRefId, String saleType);

    @Query("SELECT sm FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId " +
           "AND sm.saleDate BETWEEN :startDate AND :endDate ORDER BY sm.saleDate DESC")
    List<SaleMaster> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    Optional<SaleMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    @Query("SELECT sm FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId " +
           "AND sm.saleDate >= :startDate AND sm.saleDate <= :endDate " +
           "AND sm.active = :active ORDER BY sm.saleDate DESC")
    List<SaleMaster> findByDateAndStatus(@Param("companyRefId") Integer companyRefId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("active") Integer active);

    long countByCompanyRefId(Integer companyRefId);
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
    long countByCustomerRefId(Integer customerRefId);

    List<SaleMaster> findByJobMasterRefId(Integer jobMasterRefId);
    List<SaleMaster> findByAgentMasterRefId(Integer agentMasterRefId);
    List<SaleMaster> findByDriverRefid(Integer driverRefid);

    @Query("SELECT MAX(sm.cNumber) FROM SaleMaster sm WHERE sm.companyRefId = :companyRefId")
    Optional<Integer> findMaxCNumberByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * One-time write-back of the QNE identity after a successful invoice push
     * (QNE's Id and InvoiceCode land in QNEId/QNECode). The empty-code guard
     * is the only dedup mechanism — the invoice POST is create-once, and the
     * live PUT update never rewrites these columns (legacy contract).
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE SaleMaster sm SET sm.qneId = :qneId, sm.qneCode = :qneCode " +
           "WHERE sm.id = :id AND (sm.qneCode IS NULL OR sm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);
}


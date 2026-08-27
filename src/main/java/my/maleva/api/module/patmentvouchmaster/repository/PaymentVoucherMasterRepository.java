package my.maleva.api.module.patmentvouchmaster.repository;

import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaymentVoucherMasterRepository extends JpaRepository<PaymentVoucherMaster, Integer> {

    /**
     * Find distinct PayTo values for a company with active status
     * Equivalent to .NET SelectPaymentTo method
     * Returns distinct, trimmed, non-empty PayTo values ordered alphabetically
     * SQL: select distinct LTRIM(RTRIM(PayTo)) from PaymentVoucherMaster
     *      where PayTo != '' and PayTo is not null and Active != 2 and CompanyRefId = ?
     *      order by PayTo
     */
    @Query(value = "SELECT DISTINCT LTRIM(RTRIM(A.PayTo)) as payTo " +
            "FROM PaymentVoucherMaster A " +
            "WHERE A.PayTo != '' " +
            "AND A.PayTo IS NOT NULL " +
            "AND A.Active != 2 " +
            "AND A.CompanyRefId = :companyRefId " +
            "ORDER BY LTRIM(RTRIM(A.PayTo)) ASC",
            nativeQuery = true)
    List<String> findDistinctPayToByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * Find distinct PayFrom values for a company with active status
     * Equivalent to .NET SelectPaymentFrom method
     * Returns distinct, trimmed, non-empty PayFrom values ordered alphabetically
     * SQL: select distinct LTRIM(RTRIM(PayFrom)) from PaymentVoucherMaster
     *      where PayFrom != '' and PayFrom is not null and Active != 2 and CompanyRefId = ?
     *      order by PayFrom
     */
    @Query(value = "SELECT DISTINCT LTRIM(RTRIM(A.PayFrom)) as payFrom " +
            "FROM PaymentVoucherMaster A " +
            "WHERE A.PayFrom != '' " +
            "AND A.PayFrom IS NOT NULL " +
            "AND A.Active != 2 " +
            "AND A.CompanyRefId = :companyRefId " +
            "ORDER BY LTRIM(RTRIM(A.PayFrom)) ASC",
            nativeQuery = true)
    List<String> findDistinctPayFromByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * One-time write-back of the QNE identity after a successful voucher push
     * (QNE's Id and PaymentCode land in QNEId/QNECode). The empty-code guard
     * is the only dedup mechanism — the PaymentVouchers POST is create-once.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE PaymentVoucherMaster pvm SET pvm.qneId = :qneId, pvm.qneCode = :qneCode " +
           "WHERE pvm.id = :id AND (pvm.qneCode IS NULL OR pvm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);
}

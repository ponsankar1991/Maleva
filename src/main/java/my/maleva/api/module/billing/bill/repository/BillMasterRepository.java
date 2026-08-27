package my.maleva.api.module.billing.bill.repository;

import my.maleva.api.module.billing.bill.entity.BillMaster;
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

@Repository
public interface BillMasterRepository extends JpaRepository<BillMaster, Integer> {
    List<BillMaster> findByCompanyRefId(Integer companyRefId);
    List<BillMaster> findBySupplierRefId(Integer supplierRefId);

    /** Look a bill up by the running number the clerk types on the edit screen. */
    Optional<BillMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Live bills already carrying this supplier invoice number.
     *
     * <p>The supplier invoice number is the bill's natural key, so this is
     * what makes a repeated save recognisable as the same bill rather than a
     * second one. Newest first, so a caller can answer with the bill that
     * actually won the race.
     */
    @Query("SELECT b FROM BillMaster b WHERE b.companyRefId = :companyRefId "
            + "AND b.active <> 2 AND TRIM(b.invoiceNo) = :invoiceNo "
            + "ORDER BY b.id DESC")
    List<BillMaster> findLiveByInvoiceNo(@Param("companyRefId") Integer companyRefId,
                                         @Param("invoiceNo") String invoiceNo);

    /**
     * Bills just entered for the same supplier, date and amount.
     *
     * <p>The fallback duplicate check for bills saved without a supplier
     * invoice number, where nothing else distinguishes a double-click from a
     * genuine second bill. Bounded by {@code since} so entering the same
     * amount again tomorrow is still allowed.
     */
    @Query("SELECT b FROM BillMaster b WHERE b.companyRefId = :companyRefId "
            + "AND b.active <> 2 AND b.supplierRefId = :supplierRefId "
            + "AND b.saleDate = :saleDate AND b.amount = :amount "
            + "AND b.createdDate >= :since ORDER BY b.id DESC")
    List<BillMaster> findRecentlyEnteredLikeThis(@Param("companyRefId") Integer companyRefId,
                                                 @Param("supplierRefId") Integer supplierRefId,
                                                 @Param("saleDate") LocalDateTime saleDate,
                                                 @Param("amount") Float amount,
                                                 @Param("since") LocalDateTime since);

    /**
     * One-time write-back of the QNE identity after a successful bill push
     * (QNE's Id and BillCode land in QNEId/QNECode). The empty-code guard is
     * the only dedup mechanism — the bill POST is create-once.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE BillMaster bm SET bm.qneId = :qneId, bm.qneCode = :qneCode " +
           "WHERE bm.id = :id AND (bm.qneCode IS NULL OR bm.qneCode = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);
}

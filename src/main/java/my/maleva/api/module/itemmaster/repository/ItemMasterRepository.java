package my.maleva.api.module.itemmaster.repository;

import my.maleva.api.module.itemmaster.entity.ItemMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, Integer> {
    List<ItemMaster> findByCompanyRefId(Integer companyRefId);
    List<ItemMaster> findByProdCode(String prodCode);

    // Get product list for company with only active items, sorted by product name
    @Query("SELECT e FROM ItemMaster e WHERE e.companyRefId = :companyRefId AND e.activestatus = 1 ORDER BY e.pName ASC")
    List<ItemMaster> findProductListByCompanyId(@Param("companyRefId") Integer companyRefId);

    /**
     * One-time write-back of the QNE identity after a successful stock push
     * (QNE's Id and StockCode land in QNEId/QNECode). REQUIRES_NEW because
     * the create push runs in an after-commit hook.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE ItemMaster i SET i.qneId = :qneId, i.qneCode = :qneCode " +
           "WHERE i.id = :id AND (i.qneId IS NULL OR i.qneId = '')")
    int claimQneIdentity(@Param("id") Integer id,
                         @Param("qneId") String qneId,
                         @Param("qneCode") String qneCode);

    /** Items never synced to QNE — the reconcile job's work list. */
    @Query("SELECT i FROM ItemMaster i WHERE i.companyRefId = :companyRefId " +
           "AND (i.qneId IS NULL OR i.qneId = '')")
    List<ItemMaster> findQneReconcileCandidates(@Param("companyRefId") Integer companyRefId);

    /**
     * Reconcile write-back matched on the trimmed product code — legacy
     * matched {@code trim(Prod_Code)} because production rows carry
     * untrimmed codes.
     */
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE ItemMaster i SET i.qneId = :qneId, i.qneCode = :qneCode " +
           "WHERE i.companyRefId = :companyRefId AND TRIM(i.prodCode) = :stockCode " +
           "AND (i.qneId IS NULL OR i.qneId = '')")
    int reconcileQneIdentity(@Param("companyRefId") Integer companyRefId,
                             @Param("stockCode") String stockCode,
                             @Param("qneId") String qneId,
                             @Param("qneCode") String qneCode);
}

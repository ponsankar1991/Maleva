package my.maleva.api.module.billing.bill.repository;

import my.maleva.api.module.billing.bill.entity.BillMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BillMasterRepository extends JpaRepository<BillMaster, Integer> {
    List<BillMaster> findByCompanyRefId(Integer companyRefId);
    List<BillMaster> findBySupplierRefId(Integer supplierRefId);

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

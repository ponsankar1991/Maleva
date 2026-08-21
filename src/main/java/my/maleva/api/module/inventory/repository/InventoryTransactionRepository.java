package my.maleva.api.module.inventory.repository;

import my.maleva.api.module.inventory.entity.InventoryTransaction;
import my.maleva.api.module.inventory.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    /**
     * Issues of one item grouped by truck: how much each truck took, how often,
     * and when it last drew stock. Each row is
     * [truckRefId, truckName, totalQuantity, timesIssued, lastIssuedDate].
     */
    @Query("select t.truckRefId, tm.truckName, sum(t.quantity), count(t), max(t.createdDate) "
         + "from InventoryTransaction t left join t.truck tm "
         + "where t.companyRefId = :companyRefId and t.productRefId = :productRefId "
         + "and t.transactionType = :outType and t.truckRefId is not null "
         + "group by t.truckRefId, tm.truckName "
         + "order by sum(t.quantity) desc")
    List<Object[]> findTruckUsage(@Param("companyRefId") Integer companyRefId,
                                  @Param("productRefId") Integer productRefId,
                                  @Param("outType") TransactionType outType);

    /**
     * Full ledger for one product in one company, newest first.
     */
    List<InventoryTransaction> findByCompanyRefIdAndProductRefIdOrderByCreatedDateDesc(
            Integer companyRefId, Integer productRefId);

    /**
     * All movements for a company, newest first (e.g. a daily stock movement report).
     */
    List<InventoryTransaction> findByCompanyRefIdOrderByCreatedDateDesc(Integer companyRefId);

    /**
     * Full life story of one physical repairable unit / tool: every issue to a truck
     * and every repair-complete, newest first — answers "which truck was this on, and why".
     */
    List<InventoryTransaction> findByCompanyRefIdAndAssetSerialNoOrderByCreatedDateDesc(
            Integer companyRefId, String assetSerialNo);
}

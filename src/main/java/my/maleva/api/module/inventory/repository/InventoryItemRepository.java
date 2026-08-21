package my.maleva.api.module.inventory.repository;

import my.maleva.api.module.inventory.entity.InventoryItem;
import my.maleva.api.module.inventory.entity.ItemType;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Integer> {

    Optional<InventoryItem> findByCompanyRefIdAndProductRefId(Integer companyRefId, Integer productRefId);

    boolean existsByCompanyRefIdAndProductRefId(Integer companyRefId, Integer productRefId);

    /**
     * Search across every item type. The caller passes an already-lowercased
     * "%term%" (or "%" for no filter), so there is no nullable parameter to
     * reason about at query-execution time.
     */
    @Query("select i from InventoryItem i join fetch i.productMaster p "
         + "where i.companyRefId = :companyRefId and i.active = 1 and ("
         + "  lower(p.prodCode) like :term or lower(p.pname) like :term "
         + "  or lower(coalesce(i.category, '')) like :term "
         + "  or lower(coalesce(i.brand, '')) like :term "
         + "  or exists (select a.id from InventoryAsset a where a.companyRefId = i.companyRefId "
         + "             and a.productRefId = i.productRefId and lower(a.serialNo) like :term)) "
         + "order by p.prodCode")
    List<InventoryItem> search(@Param("companyRefId") Integer companyRefId,
                               @Param("term") String term);

    /**
     * Same search, narrowed to a single item type.
     */
    @Query("select i from InventoryItem i join fetch i.productMaster p "
         + "where i.companyRefId = :companyRefId and i.active = 1 and i.itemType = :itemType and ("
         + "  lower(p.prodCode) like :term or lower(p.pname) like :term "
         + "  or lower(coalesce(i.category, '')) like :term "
         + "  or lower(coalesce(i.brand, '')) like :term "
         + "  or exists (select a.id from InventoryAsset a where a.companyRefId = i.companyRefId "
         + "             and a.productRefId = i.productRefId and lower(a.serialNo) like :term)) "
         + "order by p.prodCode")
    List<InventoryItem> searchByType(@Param("companyRefId") Integer companyRefId,
                                     @Param("itemType") ItemType itemType,
                                     @Param("term") String term);

    /**
     * Quantity-tracked items at or below their reorder level.
     * Serialised items are excluded - their availability is a unit count, not a balance.
     */
    @Query("select i from InventoryItem i "
         + "join fetch i.productMaster p "
         + "join ProductMasterCStock s "
         + "  on s.companyRefId = i.companyRefId and s.productRefId = i.productRefId "
         + "where i.companyRefId = :companyRefId and i.active = 1 "
         + "and i.itemType in (my.maleva.api.module.inventory.entity.ItemType.CONSUMABLE, "
         + "                   my.maleva.api.module.inventory.entity.ItemType.PART) "
         + "and i.minQty is not null and s.cstock <= i.minQty "
         + "order by p.prodCode")
    List<InventoryItem> findLowStock(@Param("companyRefId") Integer companyRefId);

    /**
     * Active products that have no workshop settings yet. These are the ones a
     * user can add to the store; anything already set up is excluded so the
     * picker cannot offer a choice that would be rejected.
     */
    @Query("select p from ProductMaster p "
         + "where p.companyRefId = :companyRefId and p.activestatus = 1 "
         + "and not exists (select i.id from InventoryItem i "
         + "                where i.companyRefId = p.companyRefId and i.productRefId = p.id) "
         + "order by p.prodCode")
    List<ProductMaster> findProductsWithoutInventorySettings(
            @Param("companyRefId") Integer companyRefId);
}

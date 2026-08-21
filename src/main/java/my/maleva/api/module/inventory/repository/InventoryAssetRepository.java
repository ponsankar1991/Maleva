package my.maleva.api.module.inventory.repository;

import my.maleva.api.module.inventory.entity.AssetStatus;
import my.maleva.api.module.inventory.entity.InventoryAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryAssetRepository extends JpaRepository<InventoryAsset, Integer> {

    Optional<InventoryAsset> findByCompanyRefIdAndProductRefIdAndSerialNo(
            Integer companyRefId, Integer productRefId, String serialNo);

    boolean existsByCompanyRefIdAndProductRefIdAndSerialNo(
            Integer companyRefId, Integer productRefId, String serialNo);

    List<InventoryAsset> findByCompanyRefIdAndProductRefId(Integer companyRefId, Integer productRefId);

    List<InventoryAsset> findByCompanyRefIdAndStatus(Integer companyRefId, AssetStatus status);

    /**
     * Row-locking read used before every status transition (issue / return-for-repair / mark-repaired)
     * so two people acting on the same serial at once can't both succeed.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from InventoryAsset a where a.companyRefId = :companyRefId " +
           "and a.productRefId = :productRefId and a.serialNo = :serialNo")
    Optional<InventoryAsset> lockBySerial(@Param("companyRefId") Integer companyRefId,
                                           @Param("productRefId") Integer productRefId,
                                           @Param("serialNo") String serialNo);

    long countByCompanyRefIdAndProductRefIdAndStatus(Integer companyRefId, Integer productRefId, AssetStatus status);

    /**
     * Unit counts per product per status for a set of products, so a list screen
     * resolves every row's availability in one query instead of one query per row.
     * Each row is [productRefId, AssetStatus, count].
     */
    @Query("select a.productRefId, a.status, count(a) from InventoryAsset a "
         + "where a.companyRefId = :companyRefId and a.productRefId in :productRefIds "
         + "group by a.productRefId, a.status")
    List<Object[]> countByProductAndStatus(@Param("companyRefId") Integer companyRefId,
                                           @Param("productRefIds") List<Integer> productRefIds);
}

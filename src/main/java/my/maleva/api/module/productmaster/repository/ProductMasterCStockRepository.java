package my.maleva.api.module.productmaster.repository;

import my.maleva.api.module.productmaster.entity.ProductMasterCStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterCStockRepository extends JpaRepository<ProductMasterCStock, Integer> {

    /**
     * Find all CStock records by company ID
     */
    List<ProductMasterCStock> findByCompanyRefId(Integer companyRefId);

    /**
     * Row-locking read used by InventoryService before every IN/OUT so two concurrent
     * movements on the same product can't both read the same balance and overwrite each other.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductMasterCStock s where s.companyRefId = :companyRefId and s.productRefId = :productRefId")
    Optional<ProductMasterCStock> lockByCompanyAndProduct(@Param("companyRefId") Integer companyRefId,
                                                           @Param("productRefId") Integer productRefId);

    /**
     * Balances for a set of products in one company - used to build list screens
     * without firing a query per row.
     */
    List<ProductMasterCStock> findByCompanyRefIdAndProductRefIdIn(Integer companyRefId, List<Integer> productRefIds);

    /**
     * Find all CStock records by product reference ID
     */
    List<ProductMasterCStock> findByProductRefId(Integer productRefId);


    /**
     * Find all CStock records by company and product
     */
    List<ProductMasterCStock> findByCompanyRefIdAndProductRefId(Integer companyRefId, Integer productRefId);

    /**
     * Delete CStock by product reference ID
     */
    void deleteByProductRefId(Integer productRefId);

    /**
     * Count CStock records by product ID
     */
    Long countByProductRefId(Integer productRefId);

    /**
     * Count CStock records by company
     */
    Long countByCompanyRefId(Integer companyRefId);
}


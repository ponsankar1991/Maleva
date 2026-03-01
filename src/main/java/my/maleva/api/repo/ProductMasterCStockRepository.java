package my.maleva.api.repo;

import my.maleva.api.model.ProductMasterCStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterCStockRepository extends JpaRepository<ProductMasterCStock, Integer> {

    /**
     * Find all CStock records by company ID
     */
    List<ProductMasterCStock> findByCompanyRefId(Integer companyRefId);

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


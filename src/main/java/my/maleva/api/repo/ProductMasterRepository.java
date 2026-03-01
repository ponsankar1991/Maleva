package my.maleva.api.repo;

import my.maleva.api.model.ProductMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMasterRepository extends JpaRepository<ProductMaster, Integer> {

    /**
     * Find all ProductMaster records by company ID
     */
    List<ProductMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active ProductMaster records by company ID
     */
    List<ProductMaster> findByCompanyRefIdAndActivestatus(Integer companyRefId, Integer activestatus);

    /**
     * Find ProductMaster by product code
     */
    Optional<ProductMaster> findByCompanyRefIdAndProdCode(Integer companyRefId, String prodCode);

    /**
     * Find ProductMaster by product name
     */
    List<ProductMaster> findByCompanyRefIdAndPnameContainingIgnoreCase(Integer companyRefId, String pname);

    /**
     * Find ProductMaster by HSN Code
     */
    List<ProductMaster> findByHsnCode(String hsnCode);

    /**
     * Find ProductMaster by Tax Code
     */
    List<ProductMaster> findByTaxCode(Integer taxCode);

    /**
     * Find ProductMaster by UOM Code
     */
    List<ProductMaster> findByUomCode(Integer uomCode);


    /**
     * Check if product code exists
     */
    Boolean existsByCompanyRefIdAndProdCode(Integer companyRefId, String prodCode);

    /**
     * Find ProductMaster by second product code
     */
    Optional<ProductMaster> findBySecondPCode(String secondPCode);

    /**
     * Find products by is product flag
     */
    List<ProductMaster> findByCompanyRefIdAndIsProduct(Integer companyRefId, Integer isProduct);

    /**
     * Count products by company
     */
    Long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active products by company
     */
    Long countByCompanyRefIdAndActivestatus(Integer companyRefId, Integer activestatus);
}


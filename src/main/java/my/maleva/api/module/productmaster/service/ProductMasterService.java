package my.maleva.api.module.productmaster.service;

import my.maleva.api.module.productmaster.dto.ProductMasterDto;
import java.util.List;
import java.util.Optional;

public interface ProductMasterService {

    /**
     * Get all ProductMaster records by company ID
     */
    List<ProductMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active ProductMaster records by company ID
     */
    List<ProductMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get ProductMaster by ID
     */
    Optional<ProductMasterDto> getById(Integer id);

    /**
     * Create new ProductMaster record
     */
    ProductMasterDto create(ProductMasterDto dto);

    /**
     * Update existing ProductMaster record
     */
    ProductMasterDto update(Integer id, ProductMasterDto dto);

    /**
     * Delete ProductMaster record
     */
    boolean delete(Integer id);

    /**
     * Get ProductMaster by product code
     */
    Optional<ProductMasterDto> getByProdCode(Integer companyRefId, String prodCode);

    /**
     * Get ProductMaster records by product name (search)
     */
    List<ProductMasterDto> searchByProductName(Integer companyRefId, String pname);

    /**
     * Get ProductMaster records by HSN Code
     */
    List<ProductMasterDto> getByHsnCode(String hsnCode);

    /**
     * Get ProductMaster records by Tax Code
     */
    List<ProductMasterDto> getByTaxCode(Integer taxCode);

    /**
     * Get ProductMaster records by UOM Code
     */
    List<ProductMasterDto> getByUomCode(Integer uomCode);

    /**
     * Check if product code exists
     */
    boolean existsByProdCode(Integer companyRefId, String prodCode);

    /**
     * Get ProductMaster by second product code
     */
    Optional<ProductMasterDto> getBySecondPCode(String secondPCode);

    /**
     * Get products by is product flag
     */
    List<ProductMasterDto> getByIsProduct(Integer companyRefId, Integer isProduct);

    /**
     * Count products by company
     */
    Long countByCompanyId(Integer companyRefId);

    /**
     * Count active products by company
     */
    Long countActiveByCompanyId(Integer companyRefId);

    /**
     * Activate ProductMaster record
     */
    ProductMasterDto activate(Integer id);

    /**
     * Deactivate ProductMaster record
     */
    ProductMasterDto deactivate(Integer id);

    /**
     * Execute SP_ProductMaster stored procedure for bulk operations
     */
    void executeProductMasterStoredProcedure(String detailsJson, Integer companyId, Integer commonCompanyDiffStock);
}


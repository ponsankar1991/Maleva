package my.maleva.api.service;

import my.maleva.api.dto.ProductMasterCStockDto;
import java.util.List;
import java.util.Optional;

public interface ProductMasterCStockService {

    /**
     * Get all CStock records by company ID
     */
    List<ProductMasterCStockDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get all CStock records by product ID
     */
    List<ProductMasterCStockDto> getAllByProductId(Integer productRefId);

    /**
     * Get CStock by ID
     */
    Optional<ProductMasterCStockDto> getById(Integer id);

    /**
     * Get CStock by company and product
     */
    List<ProductMasterCStockDto> getByCompanyAndProduct(Integer companyRefId, Integer productRefId);

    /**
     * Create new CStock record
     */
    ProductMasterCStockDto create(ProductMasterCStockDto dto);

    /**
     * Update CStock record
     */
    ProductMasterCStockDto update(Integer id, ProductMasterCStockDto dto);

    /**
     * Delete CStock record
     */
    boolean delete(Integer id);

    /**
     * Delete all CStock records by product ID
     */
    void deleteByProductId(Integer productRefId);

    /**
     * Count CStock records by product
     */
    Long countByProductId(Integer productRefId);

    /**
     * Count CStock records by company
     */
    Long countByCompanyId(Integer companyRefId);

    /**
     * Update CStock value
     */
    ProductMasterCStockDto updateCStock(Integer id, Double newCStock);
}


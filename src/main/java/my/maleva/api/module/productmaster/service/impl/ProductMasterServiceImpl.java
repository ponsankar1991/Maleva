package my.maleva.api.module.productmaster.service.impl;

import my.maleva.api.module.productmaster.dto.ProductMasterDto;
import my.maleva.api.module.productmaster.mapper.ProductMasterMapper;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.productmaster.repository.ProductMasterRepository;
import my.maleva.api.module.productmaster.repository.ProductMasterCStockRepository;
import my.maleva.api.module.productmaster.service.ProductMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProductMaster Service Implementation
 * Handles business logic for ProductMaster operations
 * Incorporates SP_ProductMaster stored procedure logic
 */
@Service
@Transactional
public class ProductMasterServiceImpl implements ProductMasterService {

    private static final Logger logger = LoggerFactory.getLogger(ProductMasterServiceImpl.class);

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Autowired
    private ProductMasterCStockRepository cstockRepository;

    @Autowired
    private ProductMasterMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ProductMasterDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all ProductMaster records for company: {}", companyRefId);
        return productMasterRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMasterDto> getActiveByCompanyId(Integer companyRefId) {
        logger.info("Fetching active ProductMaster records for company: {}", companyRefId);
        return productMasterRepository.findByCompanyRefIdAndActivestatus(companyRefId, 1)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductMasterDto> getById(Integer id) {
        logger.info("Fetching ProductMaster by ID: {}", id);
        return productMasterRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ProductMasterDto create(ProductMasterDto dto) {
        logger.info("Creating new ProductMaster for company: {}", dto.getCompanyRefId());

        // Check if product code already exists
        if (productMasterRepository.existsByCompanyRefIdAndProdCode(dto.getCompanyRefId(), dto.getProdCode())) {
            throw new RuntimeException("Product Code '" + dto.getProdCode() + "' already exists for this company");
        }

        ProductMaster entity = mapper.toEntity(dto);
        entity.setActivestatus(1);

        ProductMaster saved = productMasterRepository.save(entity);
        logger.info("ProductMaster created successfully with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductMasterDto update(Integer id, ProductMasterDto dto) {
        logger.info("Updating ProductMaster with ID: {}", id);

        ProductMaster entity = productMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductMaster not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);

        ProductMaster updated = productMasterRepository.save(entity);
        logger.info("ProductMaster updated successfully with ID: {}", id);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting ProductMaster with ID: {}", id);

        if (!productMasterRepository.existsById(id)) {
            logger.warn("ProductMaster not found with ID: {}", id);
            return false;
        }

        // Delete associated CStock records first
        cstockRepository.deleteByProductRefId(id);

        productMasterRepository.deleteById(id);
        logger.info("ProductMaster deleted successfully with ID: {}", id);

        return true;
    }

    @Override
    public Optional<ProductMasterDto> getByProdCode(Integer companyRefId, String prodCode) {
        logger.info("Fetching ProductMaster by product code: {}", prodCode);
        return productMasterRepository.findByCompanyRefIdAndProdCode(companyRefId, prodCode)
                .map(mapper::toDto);
    }

    @Override
    public List<ProductMasterDto> searchByProductName(Integer companyRefId, String pname) {
        logger.info("Searching ProductMaster by name: {}", pname);
        return productMasterRepository.findByCompanyRefIdAndPnameContainingIgnoreCase(companyRefId, pname)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMasterDto> getByHsnCode(String hsnCode) {
        logger.info("Fetching ProductMaster by HSN Code: {}", hsnCode);
        return productMasterRepository.findByHsnCode(hsnCode)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMasterDto> getByTaxCode(Integer taxCode) {
        logger.info("Fetching ProductMaster by Tax Code: {}", taxCode);
        return productMasterRepository.findByTaxCode(taxCode)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMasterDto> getByUomCode(Integer uomCode) {
        logger.info("Fetching ProductMaster by UOM Code: {}", uomCode);
        return productMasterRepository.findByUomCode(uomCode)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByProdCode(Integer companyRefId, String prodCode) {
        logger.info("Checking if product code exists: {}", prodCode);
        return productMasterRepository.existsByCompanyRefIdAndProdCode(companyRefId, prodCode);
    }

    @Override
    public Optional<ProductMasterDto> getBySecondPCode(String secondPCode) {
        logger.info("Fetching ProductMaster by second product code: {}", secondPCode);
        return productMasterRepository.findBySecondPCode(secondPCode)
                .map(mapper::toDto);
    }

    @Override
    public List<ProductMasterDto> getByIsProduct(Integer companyRefId, Integer isProduct) {
        logger.info("Fetching ProductMaster by is product flag: {}", isProduct);
        return productMasterRepository.findByCompanyRefIdAndIsProduct(companyRefId, isProduct)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByCompanyId(Integer companyRefId) {
        logger.info("Counting ProductMaster records for company: {}", companyRefId);
        return productMasterRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    public Long countActiveByCompanyId(Integer companyRefId) {
        logger.info("Counting active ProductMaster records for company: {}", companyRefId);
        return productMasterRepository.countByCompanyRefIdAndActivestatus(companyRefId, 1);
    }

    @Override
    @Transactional
    public ProductMasterDto activate(Integer id) {
        logger.info("Activating ProductMaster with ID: {}", id);

        ProductMaster entity = productMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductMaster not found with ID: " + id));

        entity.setActivestatus(1);
        ProductMaster updated = productMasterRepository.save(entity);

        logger.info("ProductMaster activated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public ProductMasterDto deactivate(Integer id) {
        logger.info("Deactivating ProductMaster with ID: {}", id);

        ProductMaster entity = productMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductMaster not found with ID: " + id));

        entity.setActivestatus(0);
        ProductMaster updated = productMasterRepository.save(entity);

        logger.info("ProductMaster deactivated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }

    /**
     * Execute SP_ProductMaster stored procedure for bulk product operations
     *
     * This method incorporates the business logic from SP_ProductMaster:
     * - Accepts product details in JSON format
     * - Validates product code uniqueness
     * - Auto-creates default Tax and UOM if not provided
     * - Auto-generates product code digits
     * - Creates ProductMasterCStock records
     * - Handles insert/update operations
     * - Manages transaction with rollback on error
     *
     * @param detailsJson JSON containing product details
     * @param companyId Company Reference ID
     * @param commonCompanyDiffStock Flag for stock distribution across companies
     */
    @Override
    @Transactional
    public void executeProductMasterStoredProcedure(String detailsJson, Integer companyId, Integer commonCompanyDiffStock) {
        logger.info("Executing SP_ProductMaster stored procedure for company: {}", companyId);

        try {
            // Call the stored procedure using JdbcTemplate
            String sql = "EXEC SP_ProductMaster @details = ?, @comid = ?, @CommonCompanyDiffStock = ?";

            jdbcTemplate.update(sql, detailsJson, companyId, commonCompanyDiffStock);

            logger.info("SP_ProductMaster executed successfully for company: {}", companyId);
        } catch (Exception e) {
            logger.error("Error executing SP_ProductMaster for company: {}", companyId, e);
            throw new RuntimeException("Error executing SP_ProductMaster: " + e.getMessage(), e);
        }
    }
}


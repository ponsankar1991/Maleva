package my.maleva.api.module.productmaster.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
     * Execute SP_ProductMaster logic directly using JDBC - NO STORED PROCEDURE CALL
     * ✅ Replaces entire SP_ProductMaster with direct JDBC implementation
     * - Parses JSON input
     * - Validates product code uniqueness
     * - Auto-creates default Tax and UOM if not provided
     * - Auto-generates product code digits
     * - Inserts/Updates ProductMaster records
     * - Creates ProductMasterCStock records
     * - Handles transactions with atomicity
     *
     * @param detailsJson JSON containing product details
     * @param companyId Company Reference ID
     * @param commonCompanyDiffStock Flag for stock distribution across companies
     */
    @Override
    @Transactional
    public void executeProductMasterStoredProcedure(String detailsJson, Integer companyId, Integer commonCompanyDiffStock) {
        logger.info("Executing SP_ProductMaster logic directly with JDBC (NO SP call) for company: {}", companyId);

        try {
            // Parse JSON to list of maps
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> productRecords = objectMapper.readValue(
                detailsJson,
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {}
            );

            logger.debug("Parsed {} product records from JSON", productRecords.size());

            // Process each product record (equivalent to SP WHILE loop)
            for (java.util.Map<String, Object> productMap : productRecords) {
                logger.debug("Processing product record: {}", productMap);

                Integer id = safeToInt(productMap.get("Id"));
                String productCode = safeToString(productMap.get("ProductCode"));
                String productName = safeToString(productMap.get("ProductName"));
                String secondCode = safeToString(productMap.get("SecondCode"));
                String hsnCode = safeToString(productMap.get("HSNCode"));
                String printerName = safeToString(productMap.get("PrinterName"));
                Integer brandId = safeToInt(productMap.get("BrandId"));
                Integer uomId = safeToInt(productMap.get("UOMId"));
                Double mrp = safeToDouble(productMap.get("MRP"));
                Double purchaseRate = safeToDouble(productMap.get("PurchaseRate"));
                Double landingCost = safeToDouble(productMap.get("LandingCost"));
                Double salesRate = safeToDouble(productMap.get("SalesRate"));
                Integer saleRateType = safeToInt(productMap.get("SalesRateType"));
                String remarks = safeToString(productMap.get("Remarks"));
                Integer active = safeToInt(productMap.get("Active"), 1);
                String brand = safeToString(productMap.get("Brand"));
                String uom = safeToString(productMap.get("UOM"));
                Integer isProduct = safeToInt(productMap.get("IsProduct"));

                // =====================================================================
                // Validate product code uniqueness
                // =====================================================================
                String validationSql;
                Integer existingCount;

                if (id == 0) {
                    // New product - check if code already exists (not soft-deleted)
                    validationSql = "SELECT COUNT(*) FROM ProductMaster WHERE CompanyRefId = ? AND Prod_Code = ? AND Activestatus != 2";
                    existingCount = jdbcTemplate.queryForObject(validationSql, Integer.class, companyId, productCode);
                } else {
                    // Update - check if code exists for other products (not this one, not soft-deleted)
                    validationSql = "SELECT COUNT(*) FROM ProductMaster WHERE CompanyRefId = ? AND Id != ? AND Prod_Code = ? AND Activestatus != 2";
                    existingCount = jdbcTemplate.queryForObject(validationSql, Integer.class, companyId, id, productCode);
                }

                if (existingCount > 0) {
                    logger.warn("Product code '{}' already exists", productCode);
                    throw new RuntimeException("This Product Code '" + productCode + "' Already Exits !!!.");
                }

                // =====================================================================
                // Resolve Brand (Tax) - auto-create if needed
                // =====================================================================
                if (!brand.isEmpty() && brandId == 0) {
                    Integer brandCheck = jdbcTemplate.queryForObject(
                        "SELECT ISNULL((SELECT Id FROM TaxMaster WHERE CompanyRefId = ? AND Description = ? AND Active = 1 LIMIT 1), 0)",
                        Integer.class, companyId, brand
                    );
                    if (brandCheck > 0) {
                        brandId = brandCheck;
                    }
                }

                // If still no brand, create default or use existing default
                if (brandId == 0) {
                    Integer brandCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM TaxMaster WHERE CompanyRefId = ?",
                        Integer.class, companyId
                    );

                    if (brandCount == 0) {
                        // Insert default Tax
                        jdbcTemplate.update(
                            "INSERT INTO TaxMaster (CompanyRefId, Code, Description, Tax, TaxIO, Modified_Date, Modified_By, Active) VALUES (?, ?, ?, ?, ?, GETDATE(), SYSTEM_USER, ?)",
                            companyId, "TX", "TAX0", 0, 1, 1
                        );
                        brandId = jdbcTemplate.queryForObject(
                            "SELECT IDENT_CURRENT('TaxMaster') + IDENT_SEED('TaxMaster')",
                            Integer.class
                        );
                    } else {
                        // Use first existing default
                        brandId = jdbcTemplate.queryForObject(
                            "SELECT TOP 1 Id FROM TaxMaster WHERE CompanyRefId = ?",
                            Integer.class, companyId
                        );
                    }
                }

                // =====================================================================
                // Resolve UOM - auto-create if needed
                // =====================================================================
                if (!uom.isEmpty() && uomId == 0) {
                    Integer uomCheck = jdbcTemplate.queryForObject(
                        "SELECT ISNULL((SELECT Id FROM UOM WHERE CompanyRefId = ? AND Description = ? AND Active = 1 LIMIT 1), 0)",
                        Integer.class, companyId, uom
                    );
                    if (uomCheck > 0) {
                        uomId = uomCheck;
                    }
                }

                // If still no UOM, create default or use existing default
                if (uomId == 0) {
                    Integer uomCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM UOM WHERE CompanyRefId = ?",
                        Integer.class, companyId
                    );

                    if (uomCount == 0) {
                        // Insert default UOM
                        jdbcTemplate.update(
                            "INSERT INTO UOM (CompanyRefId, Code, Description, Created_Date, Modified_Date, Modified_By, Active) VALUES (?, ?, ?, GETDATE(), GETDATE(), SYSTEM_USER, ?)",
                            companyId, "KGS", "KILOGRAM", 1
                        );
                        uomId = jdbcTemplate.queryForObject(
                            "SELECT IDENT_CURRENT('UOM')",
                            Integer.class
                        );
                    } else {
                        // Use first existing default
                        uomId = jdbcTemplate.queryForObject(
                            "SELECT TOP 1 Id FROM UOM WHERE CompanyRefId = ?",
                            Integer.class, companyId
                        );
                    }
                }

                // =====================================================================
                // Generate product code digits (for numeric/short codes)
                // =====================================================================
                Integer pCodeDigits = 0;
                if (productCode != null && !productCode.isEmpty()) {
                    // Check if it looks like a numeric code (no letters, length < 8)
                    if (!productCode.matches(".*[A-Z].*") && productCode.length() < 8) {
                        try {
                            pCodeDigits = Integer.parseInt(productCode);
                        } catch (NumberFormatException e) {
                            pCodeDigits = 0;
                        }
                    }
                }

                // =====================================================================
                // Set printer name default
                // =====================================================================
                if (printerName == null || printerName.isEmpty()) {
                    printerName = productName;
                }

                // =====================================================================
                // INSERT or UPDATE ProductMaster
                // =====================================================================
                if (id == 0) {
                    // INSERT new product
                    logger.debug("Inserting new ProductMaster: {}", productCode);

                    String insertSql = """
                        INSERT INTO ProductMaster
                        (CompanyRefId, Prod_Code, PCode_Digits, PName, SecondPCode, HSNCode,
                         PrintName, Tax_Code, UOM_Code, MRP, PurchaseRate, LandingCost,
                         SalesRate, SaleRateType, Remarks, Modified_Date, Modified_By,
                         Activestatus, Created_Date, IsProduct)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), SYSTEM_USER, ?, GETDATE(), ?)
                        """;

                    // Use array to store the generated ID (workaround for lambda variable scope)
                    final int[] generatedId = new int[1];

                    try (java.sql.Connection conn = jdbcTemplate.getDataSource().getConnection();
                         java.sql.PreparedStatement ps = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                        ps.setInt(1, companyId);
                        ps.setString(2, productCode);
                        ps.setInt(3, pCodeDigits);
                        ps.setString(4, productName);
                        ps.setString(5, secondCode);
                        ps.setString(6, hsnCode);
                        ps.setString(7, printerName);
                        ps.setInt(8, brandId);
                        ps.setInt(9, uomId);
                        ps.setDouble(10, mrp);
                        ps.setDouble(11, purchaseRate);
                        ps.setDouble(12, landingCost);
                        ps.setDouble(13, salesRate);
                        ps.setInt(14, saleRateType);
                        ps.setString(15, remarks);
                        ps.setInt(16, 1); // Activestatus = 1 (active)
                        ps.setInt(17, isProduct);

                        ps.executeUpdate();

                        try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                generatedId[0] = rs.getInt(1);
                            }
                        }
                    } catch (java.sql.SQLException e) {
                        logger.error("Error inserting ProductMaster", e);
                        throw new RuntimeException("Error inserting product master: " + e.getMessage(), e);
                    }

                    id = generatedId[0];
                    logger.debug("Inserted ProductMaster with ID: {}", id);

                    // =====================================================================
                    // Create ProductMasterCStock records
                    // =====================================================================
                    if (commonCompanyDiffStock == 1) {
                        // Create stock entry for each company under MComid
                        jdbcTemplate.update(
                            """
                            INSERT INTO ProductMasterCStock (CompanyRefId, ProductRefId, CStock)
                            SELECT Id, ?, 0 FROM Company WHERE MComid = ?
                            """,
                            id, companyId
                        );
                    } else {
                        // Create stock entry for current company only
                        jdbcTemplate.update(
                            "INSERT INTO ProductMasterCStock (CompanyRefId, ProductRefId, CStock) VALUES (?, ?, ?)",
                            companyId, id, 0
                        );
                    }

                    logger.debug("Created ProductMasterCStock records for product ID: {}", id);

                } else {
                    // UPDATE existing product
                    logger.debug("Updating existing ProductMaster ID: {}", id);

                    String updateSql = """
                        UPDATE ProductMaster SET
                            CompanyRefId = ?, Prod_Code = ?, PCode_Digits = ?, PName = ?,
                            SecondPCode = ?, HSNCode = ?, PrintName = ?, Tax_Code = ?,
                            UOM_Code = ?, MRP = ?, PurchaseRate = ?, LandingCost = ?,
                            SalesRate = ?, SaleRateType = ?, Remarks = ?,
                            Modified_Date = GETDATE(), Modified_By = SYSTEM_USER, IsProduct = ?
                        WHERE Id = ?
                        """;

                    jdbcTemplate.update(updateSql,
                        companyId, productCode, pCodeDigits, productName, secondCode,
                        hsnCode, printerName, brandId, uomId, mrp, purchaseRate,
                        landingCost, salesRate, saleRateType, remarks, isProduct, id
                    );

                    logger.debug("Updated ProductMaster ID: {}", id);
                }
            }

            logger.info("SP_ProductMaster logic executed successfully for company: {}", companyId);

        } catch (Exception e) {
            logger.error("Error executing SP_ProductMaster logic for company: {}", companyId, e);
            throw new RuntimeException("Error executing product master operations: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    // Helper methods for safe type conversions
    // =====================================================================

    private Integer safeToInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return 0;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Integer safeToInt(Object value, Integer defaultValue) {
        Integer result = safeToInt(value);
        return result != 0 ? result : (defaultValue != null ? defaultValue : 0);
    }

    private Double safeToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return 0.0;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String safeToString(Object value) {
        if (value == null) return "";
        if (value instanceof String) {
            String str = ((String) value).trim();
            return str.equalsIgnoreCase("null") ? "" : str;
        }
        return value.toString().trim();
    }
}


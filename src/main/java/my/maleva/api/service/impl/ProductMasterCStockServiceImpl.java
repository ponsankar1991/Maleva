package my.maleva.api.service.impl;

import my.maleva.api.dto.ProductMasterCStockDto;
import my.maleva.api.mapper.ProductMasterCStockMapper;
import my.maleva.api.model.ProductMasterCStock;
import my.maleva.api.repo.ProductMasterCStockRepository;
import my.maleva.api.service.ProductMasterCStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProductMasterCStock Service Implementation
 * Handles business logic for ProductMasterCStock operations
 */
@Service
@Transactional
public class ProductMasterCStockServiceImpl implements ProductMasterCStockService {

    private static final Logger logger = LoggerFactory.getLogger(ProductMasterCStockServiceImpl.class);

    @Autowired
    private ProductMasterCStockRepository cstockRepository;

    @Autowired
    private ProductMasterCStockMapper mapper;

    @Override
    public List<ProductMasterCStockDto> getAllByCompanyId(Integer companyRefId) {
        logger.info("Fetching all CStock records for company: {}", companyRefId);
        return cstockRepository.findByCompanyRefId(companyRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMasterCStockDto> getAllByProductId(Integer productRefId) {
        logger.info("Fetching all CStock records for product: {}", productRefId);
        return cstockRepository.findByProductRefId(productRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductMasterCStockDto> getById(Integer id) {
        logger.info("Fetching CStock by ID: {}", id);
        return cstockRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    public List<ProductMasterCStockDto> getByCompanyAndProduct(Integer companyRefId, Integer productRefId) {
        logger.info("Fetching CStock for company: {} and product: {}", companyRefId, productRefId);
        return cstockRepository.findByCompanyRefIdAndProductRefId(companyRefId, productRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductMasterCStockDto create(ProductMasterCStockDto dto) {
        logger.info("Creating new CStock for product: {}", dto.getProductRefId());

        ProductMasterCStock entity = mapper.toEntity(dto);
        if (entity.getCstock() == null) {
            entity.setCstock(0.0);
        }

        ProductMasterCStock saved = cstockRepository.save(entity);
        logger.info("CStock created successfully with ID: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductMasterCStockDto update(Integer id, ProductMasterCStockDto dto) {
        logger.info("Updating CStock with ID: {}", id);

        ProductMasterCStock entity = cstockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CStock not found with ID: " + id));

        mapper.updateEntityFromDto(dto, entity);

        ProductMasterCStock updated = cstockRepository.save(entity);
        logger.info("CStock updated successfully with ID: {}", id);

        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        logger.info("Deleting CStock with ID: {}", id);

        if (!cstockRepository.existsById(id)) {
            logger.warn("CStock not found with ID: {}", id);
            return false;
        }

        cstockRepository.deleteById(id);
        logger.info("CStock deleted successfully with ID: {}", id);

        return true;
    }

    @Override
    @Transactional
    public void deleteByProductId(Integer productRefId) {
        logger.info("Deleting all CStock records for product: {}", productRefId);
        cstockRepository.deleteByProductRefId(productRefId);
    }

    @Override
    public Long countByProductId(Integer productRefId) {
        logger.info("Counting CStock records for product: {}", productRefId);
        return cstockRepository.countByProductRefId(productRefId);
    }

    @Override
    public Long countByCompanyId(Integer companyRefId) {
        logger.info("Counting CStock records for company: {}", companyRefId);
        return cstockRepository.countByCompanyRefId(companyRefId);
    }

    @Override
    @Transactional
    public ProductMasterCStockDto updateCStock(Integer id, Double newCStock) {
        logger.info("Updating CStock value for record: {}", id);

        ProductMasterCStock entity = cstockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CStock not found with ID: " + id));

        entity.setCstock(newCStock);
        ProductMasterCStock updated = cstockRepository.save(entity);

        logger.info("CStock value updated successfully with ID: {}", id);
        return mapper.toDto(updated);
    }
}


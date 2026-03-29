package my.maleva.api.module.itemmaster.service;

import my.maleva.api.module.itemmaster.dto.ItemMasterDto;
import my.maleva.api.module.productmaster.dto.ProductListDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.itemmaster.mapper.ItemMasterMapper;
import my.maleva.api.module.productmaster.mapper.ProductListMapper;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemMasterService {

    private final ItemMasterRepository repository;
    private final ItemMasterMapper mapper;
    private final ProductListMapper productListMapper;

    public ItemMasterService(ItemMasterRepository repository, ItemMasterMapper mapper, ProductListMapper productListMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.productListMapper = productListMapper;
    }

    /**
     * Get all items
     */
    @Transactional(readOnly = true)
    public List<ItemMasterDto> listAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get item by ID
     */
    @Transactional(readOnly = true)
    public ItemMasterDto getById(Integer id) {
        ItemMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ItemMaster not found with id: " + id));
        return mapper.toDto(entity);
    }

    /**
     * Create new item
     */
    @Transactional
    public ItemMasterDto create(ItemMasterDto dto) {
        ItemMaster entity = mapper.toEntity(dto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        ItemMaster saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    /**
     * Update existing item
     */
    @Transactional
    public ItemMasterDto update(Integer id, ItemMasterDto dto) {
        ItemMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ItemMaster not found with id: " + id));
        mapper.updateFromDto(dto, entity);
        entity.setModifiedDate(LocalDateTime.now());
        ItemMaster updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    /**
     * Delete item by ID
     */
    @Transactional
    public void delete(Integer id) {
        ItemMaster entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ItemMaster not found with id: " + id));
        repository.delete(entity);
    }

    /**
     * Get product list for a company with only active items (Activestatus=1)
     * Returns: Id, ProductName, SaleRate, PurRate, MRP, ProductCode
     * Sorted by product name
     */
    @Transactional(readOnly = true)
    public List<ProductListDto> getProductList(Integer companyRefId) {
        return repository.findProductListByCompanyId(companyRefId)
                .stream()
                .map(productListMapper::toDto)
                .collect(Collectors.toList());
    }
}

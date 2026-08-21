package my.maleva.api.module.inventory.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.inventory.dto.*;
import my.maleva.api.module.inventory.entity.AssetStatus;
import my.maleva.api.module.inventory.entity.InventoryAsset;
import my.maleva.api.module.inventory.exception.InvalidAssetStateException;
import my.maleva.api.module.inventory.repository.InventoryAssetRepository;
import my.maleva.api.module.inventory.repository.InventoryTransactionRepository;
import my.maleva.api.module.inventory.service.InventoryService;
import my.maleva.api.module.inventory.service.RepairableAssetService;
import my.maleva.api.module.productmaster.repository.ProductMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repairable Asset / Tool Service Implementation
 * Tracks each physical unit through AVAILABLE -> INSTALLED -> UNDER_REPAIR -> AVAILABLE ...
 * Stock quantity (ProductMasterCStock, via InventoryService) only ever counts AVAILABLE units —
 * a unit currently bolted onto a truck or sitting in the repair bay is not "available stock".
 */
@Service
@Transactional
public class RepairableAssetServiceImpl implements RepairableAssetService {

    private static final Logger logger = LoggerFactory.getLogger(RepairableAssetServiceImpl.class);

    @Autowired
    private InventoryAssetRepository assetRepository;

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public InventoryAssetDto registerAsset(RegisterAssetRequestDto request) {
        validateProductExists(request.getProductRefId());

        if (assetRepository.existsByCompanyRefIdAndProductRefIdAndSerialNo(
                request.getCompanyRefId(), request.getProductRefId(), request.getSerialNo())) {
            throw new InvalidRequestException("Serial No '" + request.getSerialNo()
                    + "' is already registered for this product");
        }

        InventoryAsset asset = assetRepository.save(
                InventoryAsset.builder()
                        .companyRefId(request.getCompanyRefId())
                        .productRefId(request.getProductRefId())
                        .serialNo(request.getSerialNo())
                        .status(AssetStatus.AVAILABLE)
                        .modifiedBy(request.getCreatedBy())
                        .build());

        inventoryService.stockIn(StockInRequestDto.builder()
                .companyRefId(request.getCompanyRefId())
                .productRefId(request.getProductRefId())
                .quantity(BigDecimal.ONE)
                .referenceType("ASSET_REGISTER")
                .referenceId(asset.getId())
                .assetSerialNo(request.getSerialNo())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build());

        logger.info("Asset registered: company={}, product={}, serial={}",
                request.getCompanyRefId(), request.getProductRefId(), request.getSerialNo());

        return toDto(asset);
    }

    @Override
    @Transactional
    public InventoryAssetDto issueAsset(IssueAssetRequestDto request) {
        InventoryAsset asset = lockAsset(request.getCompanyRefId(), request.getProductRefId(), request.getSerialNo());

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new InvalidAssetStateException(
                    "Serial No '" + request.getSerialNo() + "' is not available (current status: "
                            + asset.getStatus() + ")");
        }

        asset.setStatus(AssetStatus.INSTALLED);
        asset.setCurrentTruckRefId(request.getTruckRefId());
        asset.setModifiedBy(request.getCreatedBy());
        assetRepository.save(asset);

        inventoryService.stockOut(StockOutRequestDto.builder()
                .companyRefId(request.getCompanyRefId())
                .productRefId(request.getProductRefId())
                .quantity(BigDecimal.ONE)
                .referenceType("JOB_ORDER")
                .referenceId(request.getJobOrderRefId())
                .truckRefId(request.getTruckRefId())
                .assetSerialNo(request.getSerialNo())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build());

        logger.info("Asset issued: serial={}, truck={}", request.getSerialNo(), request.getTruckRefId());

        return toDto(asset);
    }

    @Override
    @Transactional
    public InventoryAssetDto returnForRepair(ReturnForRepairRequestDto request) {
        InventoryAsset asset = lockAsset(request.getCompanyRefId(), request.getProductRefId(), request.getSerialNo());

        if (asset.getStatus() != AssetStatus.INSTALLED) {
            throw new InvalidAssetStateException(
                    "Serial No '" + request.getSerialNo() + "' is not installed (current status: "
                            + asset.getStatus() + ")");
        }

        asset.setStatus(AssetStatus.UNDER_REPAIR);
        asset.setCurrentTruckRefId(null);
        asset.setModifiedBy(request.getCreatedBy());
        assetRepository.save(asset);

        logger.info("Asset returned for repair: serial={}", request.getSerialNo());

        return toDto(asset);
    }

    @Override
    @Transactional
    public InventoryAssetDto markRepaired(MarkRepairedRequestDto request) {
        InventoryAsset asset = lockAsset(request.getCompanyRefId(), request.getProductRefId(), request.getSerialNo());

        if (asset.getStatus() != AssetStatus.UNDER_REPAIR) {
            throw new InvalidAssetStateException(
                    "Serial No '" + request.getSerialNo() + "' is not under repair (current status: "
                            + asset.getStatus() + ")");
        }

        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setModifiedBy(request.getCreatedBy());
        assetRepository.save(asset);

        inventoryService.stockIn(StockInRequestDto.builder()
                .companyRefId(request.getCompanyRefId())
                .productRefId(request.getProductRefId())
                .quantity(BigDecimal.ONE)
                .referenceType("REPAIR_COMPLETE")
                .referenceId(asset.getId())
                .assetSerialNo(request.getSerialNo())
                .remarks(request.getRemarks())
                .createdBy(request.getCreatedBy())
                .build());

        logger.info("Asset repair complete, back in stock: serial={}", request.getSerialNo());

        return toDto(asset);
    }

    @Override
    public List<InventoryAssetDto> getAssetsByProduct(Integer companyRefId, Integer productRefId) {
        return assetRepository.findByCompanyRefIdAndProductRefId(companyRefId, productRefId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<InventoryAssetDto> getAssetsByStatus(Integer companyRefId, String status) {
        AssetStatus parsed;
        try {
            parsed = AssetStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Status must be one of "
                    + java.util.Arrays.toString(AssetStatus.values()) + ", got: " + status);
        }
        return assetRepository.findByCompanyRefIdAndStatus(companyRefId, parsed)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<InventoryTransactionDto> getAssetHistory(Integer companyRefId, String serialNo) {
        return transactionRepository.findByCompanyRefIdAndAssetSerialNoOrderByCreatedDateDesc(companyRefId, serialNo)
                .stream()
                .map(e -> InventoryTransactionDto.builder()
                        .id(e.getId())
                        .companyRefId(e.getCompanyRefId())
                        .productRefId(e.getProductRefId())
                        .productCode(e.getProductMaster() != null ? e.getProductMaster().getProdCode() : null)
                        .productName(e.getProductMaster() != null ? e.getProductMaster().getPname() : null)
                        .transactionType(e.getTransactionType().name())
                        .quantity(e.getQuantity())
                        .balanceAfter(e.getBalanceAfter())
                        .referenceType(e.getReferenceType())
                        .referenceId(e.getReferenceId())
                        .truckRefId(e.getTruckRefId())
                        .truckName(e.getTruck() != null ? e.getTruck().getTruckName() : null)
                        .assetSerialNo(e.getAssetSerialNo())
                        .remarks(e.getRemarks())
                        .createdBy(e.getCreatedBy())
                        .createdDate(e.getCreatedDate())
                        .build())
                .collect(Collectors.toList());
    }

    private InventoryAsset lockAsset(Integer companyRefId, Integer productRefId, String serialNo) {
        return assetRepository.lockBySerial(companyRefId, productRefId, serialNo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Serial No '" + serialNo + "' not found for this product"));
    }

    private void validateProductExists(Integer productRefId) {
        if (!productMasterRepository.existsById(productRefId)) {
            throw new EntityNotFoundException("ProductMaster not found with ID: " + productRefId);
        }
    }

    private InventoryAssetDto toDto(InventoryAsset a) {
        return InventoryAssetDto.builder()
                .id(a.getId())
                .companyRefId(a.getCompanyRefId())
                .productRefId(a.getProductRefId())
                .prodCode(a.getProductMaster() != null ? a.getProductMaster().getProdCode() : null)
                .pname(a.getProductMaster() != null ? a.getProductMaster().getPname() : null)
                .serialNo(a.getSerialNo())
                .status(a.getStatus().name())
                .currentTruckRefId(a.getCurrentTruckRefId())
                .currentTruckName(a.getCurrentTruck() != null ? a.getCurrentTruck().getTruckName() : null)
                .createdDate(a.getCreatedDate())
                .modifiedDate(a.getModifiedDate())
                .modifiedBy(a.getModifiedBy())
                .build();
    }
}

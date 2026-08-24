package my.maleva.api.module.inventory.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.inventory.dto.InventoryTransactionDto;
import my.maleva.api.module.inventory.dto.ReceivePurchaseLineRequestDto;
import my.maleva.api.module.inventory.dto.StockInRequestDto;
import my.maleva.api.module.inventory.dto.StockOutRequestDto;
import my.maleva.api.module.inventory.dto.TruckUsageDto;
import my.maleva.api.module.inventory.entity.InventoryTransaction;
import my.maleva.api.module.inventory.entity.TransactionType;
import my.maleva.api.module.inventory.exception.InsufficientStockException;
import my.maleva.api.module.inventory.repository.InventoryTransactionRepository;
import my.maleva.api.module.inventory.service.InventoryService;
import my.maleva.api.module.productmaster.entity.ProductMasterCStock;
import my.maleva.api.module.productmaster.repository.ProductMasterCStockRepository;
import my.maleva.api.module.productmaster.repository.ProductMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory IN/OUT Service Implementation
 * Every stock movement is recorded as an InventoryTransaction row (the audit ledger)
 * and the running balance in ProductMasterCStock is updated in the same transaction,
 * under a row lock, so concurrent IN/OUT calls on the same product can't lose an update.
 */
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private ProductMasterCStockRepository cstockRepository;

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Override
    @Transactional
    public InventoryTransactionDto stockIn(StockInRequestDto request) {
        validateProductExists(request.getProductRefId());

        ProductMasterCStock stock = cstockRepository
                .lockByCompanyAndProduct(request.getCompanyRefId(), request.getProductRefId())
                .orElseGet(() -> cstockRepository.save(
                        ProductMasterCStock.builder()
                                .companyRefId(request.getCompanyRefId())
                                .productRefId(request.getProductRefId())
                                .cstock(0.0)
                                .modifiedBy(request.getCreatedBy())
                                .build()));

        BigDecimal currentBalance = toAmount(stock.getCstock());
        BigDecimal newBalance = currentBalance.add(request.getQuantity());

        stock.setCstock(newBalance.doubleValue());
        stock.setModifiedBy(request.getCreatedBy());
        cstockRepository.save(stock);

        InventoryTransaction saved = transactionRepository.save(
                InventoryTransaction.builder()
                        .companyRefId(request.getCompanyRefId())
                        .productRefId(request.getProductRefId())
                        .transactionType(TransactionType.IN)
                        .quantity(request.getQuantity())
                        .balanceAfter(newBalance)
                        .unitCost(request.getUnitCost())
                        // Left null rather than zero when no price came in, so a
                        // receipt of unknown value cannot be read as a free one.
                        .totalValue(request.getUnitCost() == null ? null
                                : request.getQuantity().multiply(request.getUnitCost())
                                        .setScale(2, RoundingMode.HALF_UP))
                        .referenceType(request.getReferenceType())
                        .referenceId(request.getReferenceId())
                        .truckRefId(request.getTruckRefId())
                        .assetSerialNo(request.getAssetSerialNo())
                        .remarks(request.getRemarks())
                        .createdBy(request.getCreatedBy())
                        .build());

        logger.info("Stock IN: company={}, product={}, qty={}, newBalance={}",
                request.getCompanyRefId(), request.getProductRefId(), request.getQuantity(), newBalance);

        return toDto(saved);
    }

    @Override
    @Transactional
    public InventoryTransactionDto stockOut(StockOutRequestDto request) {
        validateProductExists(request.getProductRefId());

        ProductMasterCStock stock = cstockRepository
                .lockByCompanyAndProduct(request.getCompanyRefId(), request.getProductRefId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No stock record found for product " + request.getProductRefId()
                                + " in company " + request.getCompanyRefId()));

        BigDecimal currentBalance = toAmount(stock.getCstock());
        BigDecimal newBalance = currentBalance.subtract(request.getQuantity());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.getProductRefId()
                            + ": available " + currentBalance + ", requested " + request.getQuantity());
        }

        stock.setCstock(newBalance.doubleValue());
        stock.setModifiedBy(request.getCreatedBy());
        cstockRepository.save(stock);

        InventoryTransaction saved = transactionRepository.save(
                InventoryTransaction.builder()
                        .companyRefId(request.getCompanyRefId())
                        .productRefId(request.getProductRefId())
                        .transactionType(TransactionType.OUT)
                        .quantity(request.getQuantity())
                        .balanceAfter(newBalance)
                        .unitCost(request.getUnitCost())
                        // Null rather than zero when no price came in - see stockIn.
                        .totalValue(request.getUnitCost() == null ? null
                                : request.getQuantity().multiply(request.getUnitCost())
                                        .setScale(2, RoundingMode.HALF_UP))
                        .referenceType(request.getReferenceType())
                        .referenceId(request.getReferenceId())
                        .truckRefId(request.getTruckRefId())
                        .assetSerialNo(request.getAssetSerialNo())
                        .remarks(request.getRemarks())
                        .createdBy(request.getCreatedBy())
                        .build());

        logger.info("Stock OUT: company={}, product={}, qty={}, newBalance={}",
                request.getCompanyRefId(), request.getProductRefId(), request.getQuantity(), newBalance);

        return toDto(saved);
    }

    @Override
    public BigDecimal getCurrentStock(Integer companyRefId, Integer productRefId) {
        return cstockRepository.findByCompanyRefIdAndProductRefId(companyRefId, productRefId)
                .stream()
                .findFirst()
                .map(s -> toAmount(s.getCstock()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public List<InventoryTransactionDto> getLedger(Integer companyRefId, Integer productRefId) {
        return transactionRepository
                .findByCompanyRefIdAndProductRefIdOrderByCreatedDateDesc(companyRefId, productRefId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TruckUsageDto> getTruckUsage(Integer companyRefId, Integer productRefId) {
        List<Object[]> rows = transactionRepository.findTruckUsage(
                companyRefId, productRefId, TransactionType.OUT);

        BigDecimal total = rows.stream()
                .map(r -> toBigDecimal(r[2]))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(r -> {
            BigDecimal qty = toBigDecimal(r[2]);
            BigDecimal share = total.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : qty.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
            return TruckUsageDto.builder()
                    .truckRefId((Integer) r[0])
                    .truckName((String) r[1])
                    .totalIssued(qty)
                    .timesIssued(((Number) r[3]).longValue())
                    .sharePercent(share)
                    .lastIssuedDate((LocalDateTime) r[4])
                    .build();
        }).collect(Collectors.toList());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal
                ? (BigDecimal) value
                : BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private void validateProductExists(Integer productRefId) {
        if (!productMasterRepository.existsById(productRefId)) {
            throw new EntityNotFoundException("ProductMaster not found with ID: " + productRefId);
        }
    }

    private BigDecimal toAmount(Double value) {
        return BigDecimal.valueOf(value != null ? value : 0.0);
    }

    private InventoryTransactionDto toDto(InventoryTransaction e) {
        return InventoryTransactionDto.builder()
                .id(e.getId())
                .companyRefId(e.getCompanyRefId())
                .productRefId(e.getProductRefId())
                .productCode(e.getProductMaster() != null ? e.getProductMaster().getProdCode() : null)
                .productName(e.getProductMaster() != null ? e.getProductMaster().getPname() : null)
                .transactionType(e.getTransactionType().name())
                .quantity(e.getQuantity())
                .balanceAfter(e.getBalanceAfter())
                .unitCost(e.getUnitCost())
                .totalValue(e.getTotalValue())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                .truckRefId(e.getTruckRefId())
                .truckName(e.getTruck() != null ? e.getTruck().getTruckName() : null)
                .assetSerialNo(e.getAssetSerialNo())
                .remarks(e.getRemarks())
                .createdBy(e.getCreatedBy())
                .createdDate(e.getCreatedDate())
                .build();
    }
}

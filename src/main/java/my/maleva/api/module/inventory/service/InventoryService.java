package my.maleva.api.module.inventory.service;

import my.maleva.api.module.inventory.dto.InventoryTransactionDto;
import my.maleva.api.module.inventory.dto.StockInRequestDto;
import my.maleva.api.module.inventory.dto.StockOutRequestDto;
import my.maleva.api.module.inventory.dto.TruckUsageDto;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    /**
     * Record a stock receipt (opening balance, purchase, return, adjustment-in, ...).
     */
    InventoryTransactionDto stockIn(StockInRequestDto request);

    /**
     * Record a stock issue (job order consumption, adjustment-out, ...).
     * Throws InsufficientStockException if the requested quantity exceeds the current balance.
     */
    InventoryTransactionDto stockOut(StockOutRequestDto request);

    /**
     * Current on-hand balance for one product in one company.
     */
    BigDecimal getCurrentStock(Integer companyRefId, Integer productRefId);

    /**
     * Full movement history for one product in one company, newest first.
     */
    List<InventoryTransactionDto> getLedger(Integer companyRefId, Integer productRefId);

    /**
     * Issues of one item broken down by truck - how much each truck consumed,
     * how often, its share of the total and when it last drew stock.
     */
    List<TruckUsageDto> getTruckUsage(Integer companyRefId, Integer productRefId);
}

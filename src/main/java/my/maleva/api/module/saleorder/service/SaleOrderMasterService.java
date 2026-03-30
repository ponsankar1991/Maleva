package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.invoice.dto.SaleF5View;

/**
 * SaleOrderMasterService - Business logic for SaleOrderMaster
 * Incorporates SP_SaleOrderMaster stored procedure logic
 */
public interface SaleOrderMasterService {

    SaleOrderMasterDto save(SaleOrderDTO dto);
    SaleOrderMasterDto update(Integer id, SaleOrderMasterDto dto);
    boolean delete(Integer id);

    /**
     * SelectSaleOrder - Complex filtered search equivalent to .NET SelectSaleOrder method
     * Returns combined SaleMaster and SaleDetails data with dynamic filtering
     */
    SaleF5View selectSaleOrder(SaleOrderFilterDTO filter);
}


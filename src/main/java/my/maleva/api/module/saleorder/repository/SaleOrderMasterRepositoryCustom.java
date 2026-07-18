package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest;

import java.util.List;

public interface SaleOrderMasterRepositoryCustom {
    List<SaleOrderInvoiceCheckDto> checkSaleOrderInvoice(SaleOrderInvoiceCheckRequest request);
}

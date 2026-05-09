package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderEditDto;
import my.maleva.api.module.saleorder.dto.SaleOrderFilterDTO;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderQuickUpdateDto;
import my.maleva.api.module.saleorder.dto.SaleOrderStatusUpdateDto;
import my.maleva.api.module.invoice.dto.SaleF5View;
import my.maleva.api.module.saleorder.dto.UpdateJobStatusDto;


public interface SaleOrderMasterService {


    SaleOrderMasterDto save(SaleOrderDTO dto);


    SaleOrderEditDto getById(Integer id);


    SaleOrderEditDto getEditSaleOrder(Integer id, Integer saleOrderNo, Integer companyId);


    SaleOrderMasterDto update(Integer id, SaleOrderDTO dto);


    SaleOrderMasterDto updateMaster(Integer id, SaleOrderMasterDto dto);


    SaleOrderStatusUpdateDto updateStatus(Integer id, Integer companyId, Integer jobStatusId);


    SaleOrderQuickUpdateDto updateQuickFields(Integer id, SaleOrderQuickUpdateDto dto);


    boolean delete(Integer id);


    SaleF5View selectSaleOrder(SaleOrderFilterDTO filter);


    SaleOrderStatusUpdateDto updateJobStatus(Integer id, Integer jobStatusId);
}

package my.maleva.api.service;

import my.maleva.api.dto.SportSaleOrderDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SportSaleOrderService - Business logic for SportSaleOrder
 * Incorporates SP_SoptSaleorder stored procedure logic
 */
public interface SportSaleOrderService {

    List<SportSaleOrderDto> getByCompanyRefId(Integer companyRefId);

    List<SportSaleOrderDto> getActiveByCompanyRefId(Integer companyRefId);

    List<SportSaleOrderDto> getByCustomerRefId(Integer customerRefId);

    List<SportSaleOrderDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId);

    List<SportSaleOrderDto> getByJobMasterRefId(Integer jobMasterRefId);

    List<SportSaleOrderDto> getByEmployeeRefId(Integer employeeRefId);

    List<SportSaleOrderDto> getByStatus(Integer jStatus);

    List<SportSaleOrderDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Optional<SportSaleOrderDto> getById(Integer id);

    Optional<SportSaleOrderDto> getByAwbNo(String awbNo);

    SportSaleOrderDto create(SportSaleOrderDto dto);

    SportSaleOrderDto update(Integer id, SportSaleOrderDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateSportSaleOrderData(SportSaleOrderDto dto);

    SportSaleOrderDto processSportSaleOrder(SportSaleOrderDto dto);
}


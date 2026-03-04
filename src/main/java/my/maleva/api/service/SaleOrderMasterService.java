package my.maleva.api.service;

import my.maleva.api.dto.SaleOrderDTO;
import my.maleva.api.dto.SaleOrderMasterDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderMasterService - Business logic for SaleOrderMaster
 * Incorporates SP_SaleOrderMaster stored procedure logic
 */
public interface SaleOrderMasterService {

    List<SaleOrderMasterDto> getAllByCompanyId(Integer companyRefId);
    List<SaleOrderMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer active);
    Optional<SaleOrderMasterDto> getById(Integer id);
    SaleOrderMasterDto create(SaleOrderMasterDto dto);
    SaleOrderMasterDto save(SaleOrderDTO dto);
    SaleOrderMasterDto update(Integer id, SaleOrderMasterDto dto);
    boolean delete(Integer id);

    List<SaleOrderMasterDto> getByCustomerRefId(Integer customerRefId);
    List<SaleOrderMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId);
    Optional<SaleOrderMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);
    List<SaleOrderMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);
    List<SaleOrderMasterDto> getByEmployeeId(Integer employeeRefId);
    List<SaleOrderMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId);
    List<SaleOrderMasterDto> getByUserId(Integer userRefId);
    List<SaleOrderMasterDto> getByJobMasterRefId(Integer jobMasterRefId);
    List<SaleOrderMasterDto> getByAgentMasterRefId(Integer agentMasterRefId);
    List<SaleOrderMasterDto> getByDriverRefid(Integer driverRefid);

    long countByCompanyId(Integer companyRefId);
    long countByCompanyIdAndStatus(Integer companyRefId, Integer active);
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    SaleOrderMasterDto activate(Integer id);
    SaleOrderMasterDto deactivate(Integer id);

    void processSaleOrderBatch(List<SaleOrderMasterDto> orderList, Integer companyId);
    SaleOrderMasterDto calculateOrderTotals(SaleOrderMasterDto dto);
    void validateOrderData(SaleOrderMasterDto dto);
}


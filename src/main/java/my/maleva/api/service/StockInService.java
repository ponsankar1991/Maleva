package my.maleva.api.service;

import my.maleva.api.dto.StockInDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StockInService - Business logic for StockIn
 * Incorporates SP_StockIn stored procedure logic
 */
public interface StockInService {

    List<StockInDto> getByCompanyRefId(Integer companyRefId);

    List<StockInDto> getByUserRefId(Integer userRefId);

    List<StockInDto> getByEmployeeRefId(Integer employeeRefId);

    List<StockInDto> getBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    List<StockInDto> getByPortMasterRefId(Integer portMasterRefId);

    List<StockInDto> getByStatus(Integer status);

    List<StockInDto> getByCompanyAndStatus(Integer companyRefId, Integer status);

    List<StockInDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<StockInDto> getByCompanyAndDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<StockInDto> getById(Integer id);

    Optional<StockInDto> getByCNumber(Integer cNumber, Integer companyRefId);

    Optional<StockInDto> getByBarcode(String barcode);

    StockInDto create(StockInDto dto);

    StockInDto update(Integer id, StockInDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countByCompanyAndStatus(Integer companyRefId, Integer status);

    void validateStockInData(StockInDto dto);

    StockInDto processStockIn(StockInDto dto);

    void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}


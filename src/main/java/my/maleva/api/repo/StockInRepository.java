package my.maleva.api.repo;

import my.maleva.api.model.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StockInRepository - Repository for StockIn
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface StockInRepository extends JpaRepository<StockIn, Integer> {

    /**
     * Find all StockIn records by company ID
     */
    List<StockIn> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all StockIn records by user ID
     */
    List<StockIn> findByUserRefId(Integer userRefId);

    /**
     * Find all StockIn records by employee ID
     */
    List<StockIn> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find all StockIn records by sale order master ID
     */
    List<StockIn> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find all StockIn records by port master ID
     */
    List<StockIn> findByPortMasterRefId(Integer portMasterRefId);

    /**
     * Find all StockIn records by status
     */
    List<StockIn> findByStatus(Integer status);

    /**
     * Find StockIn by C Number
     */
    Optional<StockIn> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find StockIn by Barcode
     */
    Optional<StockIn> findByBarcode(String barcode);

    /**
     * Find StockIn records by company and status
     */
    List<StockIn> findByCompanyRefIdAndStatus(Integer companyRefId, Integer status);

    /**
     * Find StockIn records by date range
     */
    List<StockIn> findByStockDateGreaterThanEqualAndStockDateLessThanEqual(
        LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find StockIn records by company and date range
     */
    List<StockIn> findByCompanyRefIdAndStockDateGreaterThanEqualAndStockDateLessThanEqual(
        Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count StockIn records by company ID
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count StockIn records by company and status
     */
    long countByCompanyRefIdAndStatus(Integer companyRefId, Integer status);

    /**
     * Delete all StockIn records by sale order master ID
     */
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}


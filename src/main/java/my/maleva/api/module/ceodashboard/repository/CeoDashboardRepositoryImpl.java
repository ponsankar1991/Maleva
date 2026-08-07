package my.maleva.api.module.ceodashboard.repository;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.ceodashboard.dto.DashboardFilterRequestDto;
import my.maleva.api.module.ceodashboard.dto.TopCustomerResponseDto;
import my.maleva.api.module.ceodashboard.dto.DateRangeResponseDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CeoDashboardRepositoryImpl implements CeoDashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<TopCustomerResponseDto> getTop20SgdCustomers(DashboardFilterRequestDto filter) {
        return executeTop20Query("MY", 5, "SUM(SOM.Amount)", "totalRevenue", filter);
    }

    @Override
    public List<TopCustomerResponseDto> getTop20UsdCustomers(DashboardFilterRequestDto filter) {
        return executeTop20Query("MY", 8, "SUM(SOM.Amount)", "totalRevenue", filter);
    }

    @Override
    public List<TopCustomerResponseDto> getTop20RmCustomers(DashboardFilterRequestDto filter) {
        return executeTop20Query("MY", 4, "SUM(SOM.ActualNetAmount)", "totalRevenue", filter);
    }

    @Override
    public List<TopCustomerResponseDto> getTop20TransportCustomers(DashboardFilterRequestDto filter) {
        return executeTop20Query("TR", null, "SUM(SOM.ActualNetAmount)", "totalRevenue", filter);
    }
    
    @Override
    public List<TopCustomerResponseDto> getTop20OverallByRevenue(DashboardFilterRequestDto filter) {
        return executeTop20Query(null, null, "SUM(SOM.ActualNetAmount)", "totalRevenue", filter);
    }
    
    @Override
    public List<TopCustomerResponseDto> getTop20OverallByJobs(DashboardFilterRequestDto filter) {
        return executeTop20Query(null, null, "SUM(SOM.ActualNetAmount)", "totalJobs", filter);
    }

    @Override
    public DateRangeResponseDto getAvailableDateRange() {
        String sql = "SELECT MIN(SaleDate) as minDate, MAX(SaleDate) as maxDate FROM SaleOrderMaster";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> {
            DateRangeResponseDto dto = new DateRangeResponseDto();
            java.sql.Date minSqlDate = rs.getDate("minDate");
            java.sql.Date maxSqlDate = rs.getDate("maxDate");
            if (minSqlDate != null) dto.setMinDate(minSqlDate.toLocalDate());
            if (maxSqlDate != null) dto.setMaxDate(maxSqlDate.toLocalDate());
            return dto;
        });
    }

    private List<TopCustomerResponseDto> executeTop20Query(String billType, Integer symbolRefId, String revenueColumn, String orderBy, DashboardFilterRequestDto filter) {
        StringBuilder sql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append("SELECT TOP 20 ");
        sql.append("    C.CNumber AS customerCode, ");
        sql.append("    C.CustomerName AS customerName, ");
        sql.append("    COUNT(SOM.Id) AS totalJobs, ");
        sql.append("    ").append(revenueColumn).append(" AS totalRevenue ");
        sql.append("FROM SaleOrderMaster SOM ");
        sql.append("JOIN Customer C ON SOM.CustomerRefId = C.Id ");
        
        sql.append("WHERE 1=1 ");
        
        if (billType != null) {
            sql.append("  AND SOM.BillType = :billType ");
            params.addValue("billType", billType);
        }
        
        if (symbolRefId != null) {
            sql.append("  AND C.SymbolRefid = :symbolRefId ");
            params.addValue("symbolRefId", symbolRefId);
        }

        if (filter != null) {
            if (filter.getFromDate() != null) {
                sql.append("  AND SOM.SaleDate >= :fromDate ");
                params.addValue("fromDate", filter.getFromDate().atStartOfDay());
            }
            if (filter.getToDate() != null) {
                sql.append("  AND SOM.SaleDate <= :toDate ");
                params.addValue("toDate", filter.getToDate().atTime(23, 59, 59));
            }
            if (filter.getCompanyRefIds() != null && !filter.getCompanyRefIds().isEmpty()) {
                sql.append("  AND SOM.CompanyRefId IN (:companyIds) ");
                params.addValue("companyIds", filter.getCompanyRefIds());
            }
            if (filter.getBranchRefIds() != null && !filter.getBranchRefIds().isEmpty()) {
                sql.append("  AND SOM.BranchRefId IN (:branchIds) ");
                params.addValue("branchIds", filter.getBranchRefIds());
            }
            if (filter.getCustomerRefIds() != null && !filter.getCustomerRefIds().isEmpty()) {
                sql.append("  AND SOM.CustomerRefId IN (:customerIds) ");
                params.addValue("customerIds", filter.getCustomerRefIds());
            }
            if (filter.getSalesPersonRefIds() != null && !filter.getSalesPersonRefIds().isEmpty()) {
                sql.append("  AND SOM.SalesPersonRefId IN (:salesPersonIds) ");
                params.addValue("salesPersonIds", filter.getSalesPersonRefIds());
            }
            if (filter.getBusinessType() != null && !filter.getBusinessType().isEmpty()) {
                sql.append("  AND SOM.BusinessType = :businessType ");
                params.addValue("businessType", filter.getBusinessType());
            }
            if (filter.getTradeType() != null && !filter.getTradeType().isEmpty()) {
                sql.append("  AND SOM.TradeType = :tradeType ");
                params.addValue("tradeType", filter.getTradeType());
            }
            if (filter.getJobType() != null && !filter.getJobType().isEmpty()) {
                sql.append("  AND SOM.JobType = :jobType ");
                params.addValue("jobType", filter.getJobType());
            }
        }

        sql.append("GROUP BY C.CNumber, C.CustomerName ");
        sql.append("ORDER BY ").append(orderBy).append(" DESC");

        int[] rank = {1};
        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            TopCustomerResponseDto dto = new TopCustomerResponseDto();
            dto.setRank(rank[0]++);
            dto.setCustomerCode(rs.getString("customerCode"));
            dto.setCustomerName(rs.getString("customerName"));
            dto.setTotalJobs(rs.getInt("totalJobs"));
            
            BigDecimal revenue = rs.getBigDecimal("totalRevenue");
            if (revenue == null) revenue = BigDecimal.ZERO;
            dto.setTotalRevenue(revenue);
            
            if (dto.getTotalJobs() > 0) {
                dto.setAvgRevenuePerShipment(revenue.divide(new BigDecimal(dto.getTotalJobs()), 2, java.math.RoundingMode.HALF_UP));
            } else {
                dto.setAvgRevenuePerShipment(BigDecimal.ZERO);
            }
            
            return dto;
        });
    }
}

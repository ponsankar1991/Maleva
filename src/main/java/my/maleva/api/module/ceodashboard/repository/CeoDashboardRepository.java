package my.maleva.api.module.ceodashboard.repository;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ceodashboard.dto.DashboardFilterRequestDto;
import my.maleva.api.module.ceodashboard.dto.TopCustomerResponseDto;
import my.maleva.api.module.ceodashboard.dto.DateRangeResponseDto;

import java.util.List;

public interface CeoDashboardRepository {
    List<TopCustomerResponseDto> getTop20SgdCustomers(DashboardFilterRequestDto filter);
    List<TopCustomerResponseDto> getTop20UsdCustomers(DashboardFilterRequestDto filter);
    List<TopCustomerResponseDto> getTop20RmCustomers(DashboardFilterRequestDto filter);
    List<TopCustomerResponseDto> getTop20TransportCustomers(DashboardFilterRequestDto filter);
    List<TopCustomerResponseDto> getTop20OverallByRevenue(DashboardFilterRequestDto filter);
    List<TopCustomerResponseDto> getTop20OverallByJobs(DashboardFilterRequestDto filter);
    DateRangeResponseDto getAvailableDateRange();
}

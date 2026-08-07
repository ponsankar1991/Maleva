package my.maleva.api.module.ceodashboard.service.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ceodashboard.dto.DashboardFilterRequestDto;
import my.maleva.api.module.ceodashboard.dto.TopCustomerResponseDto;
import my.maleva.api.module.ceodashboard.dto.DateRangeResponseDto;
import my.maleva.api.module.ceodashboard.repository.CeoDashboardRepository;
import my.maleva.api.module.ceodashboard.service.CeoDashboardService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CeoDashboardServiceImpl implements CeoDashboardService {

    private final CeoDashboardRepository ceoDashboardRepository;

    @Override
    @Cacheable(value = "ceoDashboardSgd", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20SgdCustomers(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20SgdCustomers(filter);
        return ApiResponse.success(data, "Top 20 SGD Ship Spares Customers fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardUsd", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20UsdCustomers(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20UsdCustomers(filter);
        return ApiResponse.success(data, "Top 20 USD Ship Spares Customers fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardRm", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20RmCustomers(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20RmCustomers(filter);
        return ApiResponse.success(data, "Top 20 RM Ship Spares Customers fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardTransport", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20TransportCustomers(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20TransportCustomers(filter);
        return ApiResponse.success(data, "Top 20 Transport Customers fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardOverallRevenue", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20OverallByRevenue(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20OverallByRevenue(filter);
        return ApiResponse.success(data, "Top 20 Overall Customers by Revenue fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardOverallJobs", key = "#filter != null ? #filter.hashCode() : 'all'")
    public ApiResponse<List<TopCustomerResponseDto>> getTop20OverallByJobs(DashboardFilterRequestDto filter) {
        List<TopCustomerResponseDto> data = ceoDashboardRepository.getTop20OverallByJobs(filter);
        return ApiResponse.success(data, "Top 20 Overall Customers by Jobs fetched successfully");
    }

    @Override
    @Cacheable(value = "ceoDashboardDateRange", key = "'all'")
    public ApiResponse<DateRangeResponseDto> getAvailableDateRange() {
        DateRangeResponseDto data = ceoDashboardRepository.getAvailableDateRange();
        return ApiResponse.success(data, "Available Date Range fetched successfully");
    }
}

package my.maleva.api.module.joborder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.joborder.dto.JobOrderFilterDto;
import my.maleva.api.module.joborder.dto.JobOrderLookupDto;
import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import my.maleva.api.module.joborder.service.JobOrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-orders")
@RequiredArgsConstructor
public class JobOrderController {

    private final JobOrderService jobOrderService;

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<JobOrderResponseDto>>> getJobOrders(
            @RequestBody JobOrderFilterDto filterDto) {
        
        List<JobOrderResponseDto> result = jobOrderService.getJobOrders(filterDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOrderResponseDto>> getJobOrderById(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {
        JobOrderResponseDto result = jobOrderService.getJobOrderById(id, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Order retrieved successfully"));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto>>> getJobOrderDetailsByMasterId(
            @PathVariable Integer id) {
        List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto> result = jobOrderService.getJobOrderDetailsByMasterId(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Order Details retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobOrderResponseDto>> createJobOrder(
            @Valid @RequestBody JobOrderRequestDto requestDto) {
        JobOrderResponseDto result = jobOrderService.createJobOrder(requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Order created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOrderResponseDto>> updateJobOrder(
            @PathVariable Integer id,
            @Valid @RequestBody JobOrderRequestDto requestDto) {
        JobOrderResponseDto result = jobOrderService.updateJobOrder(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Order updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobOrder(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {
        jobOrderService.deleteJobOrder(id, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(null, "Job Order deleted successfully"));
    }

    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> getNextJobNumber(@RequestParam Integer companyRefId) {
        String nextNumber = jobOrderService.getNextJobNumber(companyRefId);
        return ResponseEntity.ok(ApiResponse.success(nextNumber, "Next job number retrieved successfully"));
    }

    @GetMapping("/lookups")
    public ResponseEntity<ApiResponse<JobOrderLookupDto>> getLookups() {
        JobOrderLookupDto result = jobOrderService.getLookups();
        return ResponseEntity.ok(ApiResponse.success(result, "Lookups retrieved successfully"));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<java.util.List<JobOrderLookupDto.LookupItem>>> getStatuses() {
        return ResponseEntity.ok(ApiResponse.success(jobOrderService.getStatuses(), "Statuses retrieved successfully"));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<java.util.List<JobOrderLookupDto.LookupItem>>> getJobTypes() {
        return ResponseEntity.ok(ApiResponse.success(jobOrderService.getJobTypes(), "Job Types retrieved successfully"));
    }

    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<java.util.List<JobOrderLookupDto.LookupItem>>> getPriorities() {
        return ResponseEntity.ok(ApiResponse.success(jobOrderService.getPriorities(), "Priorities retrieved successfully"));
    }
}

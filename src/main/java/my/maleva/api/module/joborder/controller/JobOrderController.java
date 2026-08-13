package my.maleva.api.module.joborder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.joborder.dto.JobOrderFilterDto;
import my.maleva.api.module.joborder.dto.JobOrderLookupDto;
import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import my.maleva.api.module.joborder.service.JobOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponse<Page<JobOrderResponseDto>>> getJobOrders(
            @RequestBody JobOrderFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<JobOrderResponseDto> result = jobOrderService.getJobOrders(filterDto, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobOrderResponseDto>> getJobOrderById(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {
        JobOrderResponseDto result = jobOrderService.getJobOrderById(id, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(result, "Job Order retrieved successfully"));
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

    @GetMapping("/lookups")
    public ResponseEntity<ApiResponse<JobOrderLookupDto>> getLookups() {
        JobOrderLookupDto result = jobOrderService.getLookups();
        return ResponseEntity.ok(ApiResponse.success(result, "Lookups retrieved successfully"));
    }
}

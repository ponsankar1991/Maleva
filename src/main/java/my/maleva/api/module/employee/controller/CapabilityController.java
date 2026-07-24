package my.maleva.api.module.employee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.employee.dto.CapabilityDto;
import my.maleva.api.module.employee.service.CapabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/capabilities")
@RequiredArgsConstructor
@Tag(name = "Capabilities", description = "Capabilities API for employee permissions")
public class CapabilityController {

    private final CapabilityService capabilityService;

    @Operation(summary = "Get all active capabilities")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CapabilityDto>>> getActiveCapabilities() {
        List<CapabilityDto> capabilities = capabilityService.getActiveCapabilities();
        return ResponseEntity.ok(ApiResponse.success(capabilities, "Active capabilities retrieved successfully"));
    }
}

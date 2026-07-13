package my.maleva.api.module.leave.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.leave.dto.request.LeaveRequestDto;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;
import my.maleva.api.module.leave.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import my.maleva.api.module.leave.dto.request.LeaveSearchRequestDto;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping("/active")
    @PermitAll
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> getActiveLeaveRequests() {
        List<LeaveRequestResponseDto> activeLeaves = leaveService.getActiveLeaveRequests();
        return ResponseEntity.ok(ApiResponse.success(activeLeaves, "Fetched active leave requests successfully"));
    }

    @PostMapping("/search")
    @PermitAll
    public ResponseEntity<ApiResponse<List<LeaveRequestResponseDto>>> searchLeaveRequests(@RequestBody LeaveSearchRequestDto request) {
        List<LeaveRequestResponseDto> leaves = leaveService.searchLeaveRequests(request);
        return ResponseEntity.ok(ApiResponse.success(leaves, "Searched leave requests successfully"));
    }

    @PostMapping("/save")

    @PermitAll
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> saveLeaveRequest(@RequestBody LeaveRequestDto request) {
        LeaveRequestResponseDto savedRequest = leaveService.saveLeaveRequest(request);
        return ResponseEntity.ok(ApiResponse.success(savedRequest, "Leave request saved successfully"));
    }
}

package my.maleva.api.module.leave.service;

import my.maleva.api.module.leave.dto.request.LeaveRequestDto;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface LeaveService {
    List<LeaveRequestResponseDto> getActiveLeaveRequests();
    List<LeaveRequestResponseDto> searchLeaveRequests(my.maleva.api.module.leave.dto.request.LeaveSearchRequestDto request);
    LeaveRequestResponseDto saveLeaveRequest(LeaveRequestDto leaveRequestDto);
}

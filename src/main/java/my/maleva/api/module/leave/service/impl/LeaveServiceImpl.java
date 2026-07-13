package my.maleva.api.module.leave.service.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.leave.dto.request.LeaveRequestDto;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;
import my.maleva.api.module.leave.entity.LeaveRequestMaster;
import my.maleva.api.module.leave.mapper.LeaveRequestMapper;
import my.maleva.api.module.leave.repository.LeaveRequestRepository;
import my.maleva.api.module.leave.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getActiveLeaveRequests() {
        // Fetch all leave requests where active = 1
        List<LeaveRequestMaster> activeLeaves = leaveRequestRepository.findByActive(1);
        
        // Map to ResponseDto which automatically pulls leaveTypeName and statusName via MapStruct
        return leaveRequestMapper.toResponseDtoList(activeLeaves);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> searchLeaveRequests(my.maleva.api.module.leave.dto.request.LeaveSearchRequestDto request) {
        
        java.time.LocalDateTime searchFromDate = request.getFromDate() != null ? 
                request.getFromDate().with(java.time.LocalTime.MIN) : null;
        java.time.LocalDateTime searchToDate = request.getToDate() != null ? 
                request.getToDate().with(java.time.LocalTime.MAX) : null;

        List<LeaveRequestMaster> activeLeaves = leaveRequestRepository
                .searchByAdvancedFilters(
                        request.getCompanyRefId(),
                        request.getApplicantType(),
                        request.getApplicantRefId(),
                        searchFromDate,
                        searchToDate);
        return leaveRequestMapper.toResponseDtoList(activeLeaves);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto saveLeaveRequest(LeaveRequestDto leaveRequestDto) {
        LeaveRequestMaster entity = leaveRequestMapper.toEntity(leaveRequestDto);
        
        if (entity.getId() == null) {
            entity.setCreatedDate(LocalDateTime.now());
            if (entity.getActive() == null) {
                entity.setActive(1);
            }
        } else {
            entity.setModifiedDate(LocalDateTime.now());
        }
        
        LeaveRequestMaster savedEntity = leaveRequestRepository.save(entity);
        return leaveRequestMapper.toResponseDto(savedEntity);
    }
}

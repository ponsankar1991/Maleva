package my.maleva.api.module.joborder.service;

import my.maleva.api.module.joborder.dto.JobOrderFilterDto;
import my.maleva.api.module.joborder.dto.JobOrderLookupDto;
import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import java.util.List;

public interface JobOrderService {

    List<JobOrderResponseDto> getJobOrders(JobOrderFilterDto filterDto);

    JobOrderResponseDto getJobOrderById(Integer id, Integer companyRefId);

    List<my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto> getJobOrderDetailsByMasterId(Integer masterId);

    JobOrderResponseDto createJobOrder(JobOrderRequestDto requestDto);

    JobOrderResponseDto updateJobOrder(Integer id, JobOrderRequestDto requestDto);

    void deleteJobOrder(Integer id, Integer companyRefId);

    JobOrderLookupDto getLookups();

    List<JobOrderLookupDto.LookupItem> getStatuses();
    List<JobOrderLookupDto.LookupItem> getJobTypes();
    List<JobOrderLookupDto.LookupItem> getPriorities();
}

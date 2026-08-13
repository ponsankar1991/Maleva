package my.maleva.api.module.joborder.service;

import my.maleva.api.module.joborder.dto.JobOrderFilterDto;
import my.maleva.api.module.joborder.dto.JobOrderLookupDto;
import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobOrderService {

    Page<JobOrderResponseDto> getJobOrders(JobOrderFilterDto filterDto, Pageable pageable);

    JobOrderResponseDto getJobOrderById(Integer id, Integer companyRefId);

    JobOrderResponseDto createJobOrder(JobOrderRequestDto requestDto);

    JobOrderResponseDto updateJobOrder(Integer id, JobOrderRequestDto requestDto);

    void deleteJobOrder(Integer id, Integer companyRefId);

    JobOrderLookupDto getLookups();
}

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

    /**
     * Every purchase order raised against one job order, newest first.
     *
     * A repair buys from several vendors, so this normally returns more than
     * one row - and one PO can cover several repair lines, which is why the
     * count of covered lines comes back with it.
     */
    java.util.List<my.maleva.api.module.joborder.dto.JobOrderPurchaseOrderDto>
            getPurchaseOrdersForJob(Integer jobOrderMasterRefId);

    String getNextJobNumber(Integer companyRefId);

    JobOrderLookupDto getLookups();

    List<JobOrderLookupDto.LookupItem> getStatuses();
    List<JobOrderLookupDto.LookupItem> getJobTypes();
    List<JobOrderLookupDto.LookupItem> getPriorities();
}

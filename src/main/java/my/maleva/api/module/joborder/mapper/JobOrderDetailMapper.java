package my.maleva.api.module.joborder.mapper;

import my.maleva.api.module.joborder.dto.JobOrderDetailRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderDetailResponseDto;
import my.maleva.api.module.joborder.entity.JobOrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobOrderDetailMapper {

    @org.mapstruct.Mapping(target = "supplierName", source = "supplier.supplierName")
    JobOrderDetailResponseDto toDto(JobOrderDetail entity);

    @org.mapstruct.Mapping(target = "supplier", ignore = true)
    JobOrderDetail toEntity(JobOrderDetailRequestDto requestDto);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "createdBy", ignore = true)
    @org.mapstruct.Mapping(target = "createdDate", ignore = true)
    @org.mapstruct.Mapping(target = "modifiedBy", ignore = true)
    @org.mapstruct.Mapping(target = "modifiedDate", ignore = true)
    @org.mapstruct.Mapping(target = "jobOrderMaster", ignore = true)
    @org.mapstruct.Mapping(target = "supplier", ignore = true)
    void updateEntity(@MappingTarget JobOrderDetail entity, JobOrderDetailRequestDto requestDto);
}

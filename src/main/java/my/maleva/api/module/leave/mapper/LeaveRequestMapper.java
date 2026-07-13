package my.maleva.api.module.leave.mapper;

import my.maleva.api.module.leave.dto.request.LeaveRequestDto;
import my.maleva.api.module.leave.dto.response.LeaveRequestResponseDto;
import my.maleva.api.module.leave.entity.LeaveRequestMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    LeaveRequestMaster toEntity(LeaveRequestDto dto);

    @Mapping(source = "leaveType.leaveTypeName", target = "leaveTypeName")
    @Mapping(source = "leaveStatus.statusName", target = "statusName")
    LeaveRequestResponseDto toResponseDto(LeaveRequestMaster entity);

    List<LeaveRequestResponseDto> toResponseDtoList(List<LeaveRequestMaster> entities);

    void updateEntity(@MappingTarget LeaveRequestMaster entity, LeaveRequestDto dto);
}

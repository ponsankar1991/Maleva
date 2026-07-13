package my.maleva.api.module.leave.mapper;

import my.maleva.api.module.leave.dto.LeaveStatusDto;
import my.maleva.api.module.leave.entity.LeaveStatusMaster;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveStatusMapper {
    LeaveStatusDto toDto(LeaveStatusMaster entity);
    LeaveStatusMaster toEntity(LeaveStatusDto dto);
    List<LeaveStatusDto> toDtoList(List<LeaveStatusMaster> entities);
}

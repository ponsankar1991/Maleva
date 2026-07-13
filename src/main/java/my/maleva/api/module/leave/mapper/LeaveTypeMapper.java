package my.maleva.api.module.leave.mapper;

import my.maleva.api.module.leave.dto.LeaveTypeDto;
import my.maleva.api.module.leave.entity.LeaveTypeMaster;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveTypeMapper {
    LeaveTypeDto toDto(LeaveTypeMaster entity);
    LeaveTypeMaster toEntity(LeaveTypeDto dto);
    List<LeaveTypeDto> toDtoList(List<LeaveTypeMaster> entities);
}

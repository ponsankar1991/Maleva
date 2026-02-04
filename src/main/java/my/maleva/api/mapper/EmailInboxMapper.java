package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.EmailInbox;
import my.maleva.api.dto.EmailInboxDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmailInboxMapper {

    EmailInboxDto toDto(EmailInbox entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EmailInbox toEntity(EmailInboxDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EmailInboxDto dto, @MappingTarget EmailInbox entity);
}

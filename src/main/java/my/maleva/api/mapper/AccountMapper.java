package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Account;
import my.maleva.api.dto.AccountDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    AccountDto toDto(Account entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account toEntity(AccountDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountDto dto, @MappingTarget Account entity);
}

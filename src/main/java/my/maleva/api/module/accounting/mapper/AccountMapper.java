package my.maleva.api.module.accounting.mapper;

import org.mapstruct.*;
import my.maleva.api.module.accounting.entity.Account;
import my.maleva.api.module.accounting.dto.AccountDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    AccountDto toDto(Account entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account toEntity(AccountDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountDto dto, @MappingTarget Account entity);
}

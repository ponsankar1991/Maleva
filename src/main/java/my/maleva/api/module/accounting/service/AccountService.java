package my.maleva.api.module.accounting.service;

import my.maleva.api.common.service.BaseService;
import my.maleva.api.module.accounting.dto.AccountDto;
import my.maleva.api.module.accounting.mapper.AccountMapper;
import my.maleva.api.module.accounting.entity.Account;
import my.maleva.api.module.accounting.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService extends BaseService<Account, AccountDto, UUID> {

    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        super(accountRepository);
        this.accountMapper = accountMapper;
    }

    @Override
    protected AccountDto toDto(Account entity) {
        return accountMapper.toDto(entity);
    }

    @Override
    protected Account toEntity(AccountDto dto) {
        return accountMapper.toEntity(dto);
    }

    @Override
    protected void updateFromDto(AccountDto dto, Account entity) {
        accountMapper.updateFromDto(dto, entity);
    }
}

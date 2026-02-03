package my.maleva.api.service;

import my.maleva.api.dto.AccountDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.AccountMapper;
import my.maleva.api.model.Account;
import my.maleva.api.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public List<AccountDto> listAll() {
        return accountRepository.findAll().stream().map(accountMapper::toDto).collect(Collectors.toList());
    }

    public AccountDto getById(UUID id) {
        Account acc = accountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
        return accountMapper.toDto(acc);
    }

    @Transactional
    public AccountDto create(AccountDto dto) {
        LocalDateTime now = LocalDateTime.now();
        Account acc = accountMapper.toEntity(dto);
        acc.setCreatedDate(now);
        acc.setModifiedDate(now);
        Account saved = accountRepository.save(acc);
        return accountMapper.toDto(saved);
    }

    @Transactional
    public AccountDto update(UUID id, AccountDto dto) {
        Account acc = accountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
        accountMapper.updateFromDto(dto, acc);
        acc.setModifiedDate(LocalDateTime.now());
        Account saved = accountRepository.save(acc);
        return accountMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Account acc = accountRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
        accountRepository.delete(acc);
    }
}

package my.maleva.api.module.accounting.controller;

import my.maleva.api.common.controller.BaseController;
import my.maleva.api.module.accounting.dto.AccountDto;
import my.maleva.api.module.accounting.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Validated
@PermitAll
public class AccountController extends BaseController<AccountDto, UUID> {

    public AccountController(AccountService accountService) {
        super(accountService, "/api/accounts");
    }
    @Override
    protected UUID getId(AccountDto dto) {
        return dto.getId();
    }
}


package my.maleva.api.module.accounting.controller;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.accounting.dto.ComboListDto;
import my.maleva.api.module.accounting.service.AccountsGroupMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts-group")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AccountsGroupMasterController {

    private final AccountsGroupMasterService service;

    @GetMapping("/GetAccountsGroupMaster")
    public ResponseEntity<List<ComboListDto>> getAccountsGroupMaster(
            @RequestParam Integer companyId,
            @RequestParam String type
    )
    {

        return ResponseEntity.ok(
                service.getAccountsGroupMaster(companyId, type)
        );
    }
}
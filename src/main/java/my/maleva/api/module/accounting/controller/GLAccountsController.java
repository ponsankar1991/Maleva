package my.maleva.api.module.accounting.controller;

import my.maleva.api.common.controller.BaseController;
import my.maleva.api.module.accounting.dto.GLAccountsDto;
import my.maleva.api.module.accounting.service.GLAccountsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gl-accounts")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class GLAccountsController extends BaseController<GLAccountsDto, UUID> {

    public GLAccountsController(GLAccountsService service) {
        super(service, "/api/gl-accounts");
    }

    @Override
    protected UUID getId(GLAccountsDto dto) {
        return dto.getId();
    }
}

package my.maleva.api.module.user.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.user.dto.FormTransactionPasswordDto;
import my.maleva.api.module.user.dto.TransactionPasswordVerifyRequest;
import my.maleva.api.module.user.service.FormTransactionPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/form-transaction-passwords")
@Validated
@PermitAll
public class FormTransactionPasswordController {

    private final FormTransactionPasswordService service;

    public FormTransactionPasswordController(FormTransactionPasswordService service) {
        this.service = service;
    }

    /**
     * Verify one transaction password.
     * POST /api/form-transaction-passwords/verify
     *
     * <p>Replaces legacy {@code POST /Login/EditPassword}. Screens call this
     * before a gated action — the QNE push on the payment voucher view asks
     * for {@code SpclPower} — and get back only whether the attempt matched.
     * A wrong password is a 200 with {@code IsSuccess=false}, not an HTTP
     * error, so a failed attempt reads as an answer rather than a fault.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verify(
            @Valid @RequestBody TransactionPasswordVerifyRequest request) {
        boolean matched = service.verify(
                request.getCompanyId(), request.getTransactionName(), request.getPassword());
        return ResponseEntity.ok(matched
                ? ApiResponse.success(true, "Password accepted")
                : ApiResponse.error("Invalid password", 200));
    }

    @GetMapping
    public List<FormTransactionPasswordDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FormTransactionPasswordDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<FormTransactionPasswordDto> create(@Valid @RequestBody FormTransactionPasswordDto dto) {
        FormTransactionPasswordDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/form-transaction-passwords/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public FormTransactionPasswordDto update(@PathVariable Integer id, @Valid @RequestBody FormTransactionPasswordDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

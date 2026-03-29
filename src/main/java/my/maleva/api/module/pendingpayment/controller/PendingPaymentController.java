package my.maleva.api.module.pendingpayment.controller;

import my.maleva.api.module.pendingpayment.dto.PendingPaymentDto;
import my.maleva.api.module.pendingpayment.service.PendingPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pending-payments")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PendingPaymentController {

    private final PendingPaymentService service;

    public PendingPaymentController(PendingPaymentService service) {
        this.service = service;
    }

    @GetMapping
    public List<PendingPaymentDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PendingPaymentDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PendingPaymentDto> create(@Valid @RequestBody PendingPaymentDto dto) {
        PendingPaymentDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/pending-payments/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PendingPaymentDto update(@PathVariable Integer id, @Valid @RequestBody PendingPaymentDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

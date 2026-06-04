package my.maleva.api.module.communication.controller;

import my.maleva.api.module.communication.dto.EmailInboxDto;
import my.maleva.api.module.communication.service.EmailInboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/email-inboxes")
@Validated
@PermitAll
public class EmailInboxController {

    private final EmailInboxService service;

    public EmailInboxController(EmailInboxService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmailInboxDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public EmailInboxDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<EmailInboxDto> create(@Valid @RequestBody EmailInboxDto dto) {
        EmailInboxDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/email-inboxes/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public EmailInboxDto update(@PathVariable Integer id, @Valid @RequestBody EmailInboxDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}


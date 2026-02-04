package my.maleva.api.controller;

import my.maleva.api.dto.CardMasterDto;
import my.maleva.api.service.CardMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/card-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CardMasterController {

    private final CardMasterService service;

    public CardMasterController(CardMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<CardMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CardMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CardMasterDto> create(@Valid @RequestBody CardMasterDto dto) {
        CardMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/card-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CardMasterDto update(@PathVariable Integer id, @Valid @RequestBody CardMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

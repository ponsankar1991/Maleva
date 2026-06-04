package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.constant.SecurityConstants;
import my.maleva.api.module.fleet.dto.FuelFillingsDto;
import my.maleva.api.module.fleet.service.FuelFillingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/fuel-fillings")
@Validated
@PermitAll
public class FuelFillingsController {

    private final FuelFillingsService service;

    public FuelFillingsController(FuelFillingsService service) {
        this.service = service;
    }

    @GetMapping
    public List<FuelFillingsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FuelFillingsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<FuelFillingsDto> create(@Valid @RequestBody FuelFillingsDto dto) {
        FuelFillingsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/fuel-fillings/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public FuelFillingsDto update(@PathVariable Integer id, @Valid @RequestBody FuelFillingsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

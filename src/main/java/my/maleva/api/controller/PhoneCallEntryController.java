package my.maleva.api.controller;

import my.maleva.api.dto.PhoneCallEntryDto;
import my.maleva.api.service.PhoneCallEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/phone-calls")
@Validated
public class PhoneCallEntryController {

    private final PhoneCallEntryService service;

    public PhoneCallEntryController(PhoneCallEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<PhoneCallEntryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PhoneCallEntryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PhoneCallEntryDto> create(@Valid @RequestBody PhoneCallEntryDto dto) {
        PhoneCallEntryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/phone-calls/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PhoneCallEntryDto update(@PathVariable Integer id, @Valid @RequestBody PhoneCallEntryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

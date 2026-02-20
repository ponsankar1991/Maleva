package my.maleva.api.controller;

import my.maleva.api.dto.ItemMasterJobDetailsDto;
import my.maleva.api.service.ItemMasterJobDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/item-master-job-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterJobDetailsController {

    private final ItemMasterJobDetailsService service;

    public ItemMasterJobDetailsController(ItemMasterJobDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemMasterJobDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ItemMasterJobDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ItemMasterJobDetailsDto> create(@Valid @RequestBody ItemMasterJobDetailsDto dto) {
        ItemMasterJobDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/item-master-job-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ItemMasterJobDetailsDto update(@PathVariable Integer id, @Valid @RequestBody ItemMasterJobDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

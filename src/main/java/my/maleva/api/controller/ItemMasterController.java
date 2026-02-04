package my.maleva.api.controller;

import my.maleva.api.dto.ItemMasterDto;
import my.maleva.api.service.ItemMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/item-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterController {

    private final ItemMasterService service;

    public ItemMasterController(ItemMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ItemMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ItemMasterDto> create(@Valid @RequestBody ItemMasterDto dto) {
        ItemMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/item-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ItemMasterDto update(@PathVariable Integer id, @Valid @RequestBody ItemMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

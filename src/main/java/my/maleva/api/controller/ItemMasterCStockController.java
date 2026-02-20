package my.maleva.api.controller;

import my.maleva.api.dto.ItemMasterCStockDto;
import my.maleva.api.service.ItemMasterCStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/item-master-cstocks")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ItemMasterCStockController {

    private final ItemMasterCStockService service;

    public ItemMasterCStockController(ItemMasterCStockService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemMasterCStockDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ItemMasterCStockDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ItemMasterCStockDto> create(@Valid @RequestBody ItemMasterCStockDto dto) {
        ItemMasterCStockDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/item-master-cstocks/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ItemMasterCStockDto update(@PathVariable Integer id, @Valid @RequestBody ItemMasterCStockDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

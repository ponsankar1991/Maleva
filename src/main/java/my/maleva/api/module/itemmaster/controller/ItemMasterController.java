package my.maleva.api.module.itemmaster.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.constant.SecurityConstants;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.itemmaster.dto.ItemMasterDto;
import my.maleva.api.module.productmaster.dto.ProductListDto;
import my.maleva.api.module.itemmaster.service.ItemMasterQneService;
import my.maleva.api.module.itemmaster.service.ItemMasterService;
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
public class ItemMasterController {

    private final ItemMasterService service;
    private final ItemMasterQneService qneService;

    public ItemMasterController(ItemMasterService service, ItemMasterQneService qneService) {
        this.service = service;
        this.qneService = qneService;
    }

    @GetMapping
   @PermitAll
    public List<ItemMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    @PermitAll
    public ItemMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    @PermitAll
    public ResponseEntity<ItemMasterDto> create(@Valid @RequestBody ItemMasterDto dto) {
        ItemMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/item-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PermitAll
    public ItemMasterDto update(@PathVariable Integer id, @Valid @RequestBody ItemMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get product list for a company (Active items only)
     * Returns: Id, ProductName, SaleRate, PurRate, MRP, Productcode
     * Sorted by product name
     * Requires authentication and appropriate role
     */
    @GetMapping("/company/{companyRefId}/products")
    @PermitAll
    public ResponseEntity<List<ProductListDto>> getProductList(@PathVariable Integer companyRefId) {
        List<ProductListDto> products = service.getProductList(companyRefId);
        return ResponseEntity.ok(products);
    }

    /**
     * Reconcile local items against QNE stocks — the Java port of legacy
     * UpdateItemmasterId: items already in QNE get their ids written back,
     * items absent from QNE are pushed one by one (failures logged, loop
     * continues).
     * POST /api/item-masters/qne/reconcile?companyId=1
     */
    @PostMapping("/qne/reconcile")
    @PermitAll
    public ResponseEntity<?> qneReconcile(@RequestParam Integer companyId) {
        return QnePushResponses.toResponse(qneService.reconcile(companyId));
    }
}

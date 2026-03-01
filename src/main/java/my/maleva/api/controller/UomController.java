package my.maleva.api.controller;

import jakarta.validation.Valid;
import my.maleva.api.dto.UomDto;
import my.maleva.api.service.UomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/uoms")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class UomController {

    private final UomService uomService;

    public UomController(UomService uomService) {
        this.uomService = uomService;
    }

    @GetMapping
    public List<UomDto> list() {
        return uomService.listAll();
    }

    @GetMapping("/{id}")
    public UomDto get(@PathVariable Integer id) {
        return uomService.getById(id);
    }

    @PostMapping
    public ResponseEntity<UomDto> create(@Valid @RequestBody UomDto dto) {
        UomDto saved = uomService.create(dto);
        return ResponseEntity.created(URI.create("/api/uoms/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public UomDto update(@PathVariable Integer id, @Valid @RequestBody UomDto dto) {
        return uomService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        uomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Process UOM with SP_UOM logic
     * POST /api/uoms/process?companyId=1&checkFlag=1
     *
     * @param dto - UomDto with UOM data
     * @param companyId - Company ID (required)
     * @param checkFlag - 0 = no check, 1 = check if exists (optional, default=0)
     * @return Processed UomDto
     */
    @PostMapping("/process")
    public ResponseEntity<UomDto> processUom(
            @Valid @RequestBody UomDto dto,
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "0") Integer checkFlag) {
        UomDto result = uomService.processUom(dto, companyId, checkFlag);
        return ResponseEntity.created(URI.create("/api/uoms/" + result.getId())).body(result);
    }
}

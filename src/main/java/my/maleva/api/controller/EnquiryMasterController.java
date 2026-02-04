package my.maleva.api.controller;

import my.maleva.api.dto.EnquiryMasterDto;
import my.maleva.api.service.EnquiryMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/enquiry-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class EnquiryMasterController {

    private final EnquiryMasterService service;

    public EnquiryMasterController(EnquiryMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<EnquiryMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public EnquiryMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<EnquiryMasterDto> create(@Valid @RequestBody EnquiryMasterDto dto) {
        EnquiryMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/enquiry-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public EnquiryMasterDto update(@PathVariable Integer id, @Valid @RequestBody EnquiryMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

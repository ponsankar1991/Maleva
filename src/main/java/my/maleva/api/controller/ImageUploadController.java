package my.maleva.api.controller;

import my.maleva.api.dto.ImageUploadDto;
import my.maleva.api.service.ImageUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/image-uploads")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ImageUploadController {

    private final ImageUploadService service;

    public ImageUploadController(ImageUploadService service) {
        this.service = service;
    }

    @GetMapping
    public List<ImageUploadDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ImageUploadDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ImageUploadDto> create(@Valid @RequestBody ImageUploadDto dto) {
        ImageUploadDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/image-uploads/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ImageUploadDto update(@PathVariable Integer id, @Valid @RequestBody ImageUploadDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

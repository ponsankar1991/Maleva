package my.maleva.api.common.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.service.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

public abstract class BaseController<D, ID> {

    protected final BaseService<?, D, ID> service;
    protected final String basePath;

    protected BaseController(BaseService<?, D, ID> service, String basePath) {
        this.service = service;
        this.basePath = basePath;
    }

    @GetMapping
    public List<D> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public D get(@PathVariable ID id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<D> create(@Valid @RequestBody D dto) {
        D saved = service.create(dto);
        ID id = getId(saved);
        return ResponseEntity.created(URI.create(basePath + "/" + id)).body(saved);
    }

    @PutMapping("/{id}")
    public D update(@PathVariable ID id, @Valid @RequestBody D dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    protected abstract ID getId(D dto);
}

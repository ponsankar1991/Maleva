package my.maleva.api.common.service;

import my.maleva.api.common.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseService<E, D, ID> {

    protected final JpaRepository<E, ID> repository;

    protected BaseService(JpaRepository<E, ID> repository) {
        this.repository = repository;
    }

    public List<D> listAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public D getById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + id));
        return toDto(entity);
    }

    @Transactional
    public D create(D dto) {
        E entity = toEntity(dto);
        E saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public D update(ID id, D dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + id));
        updateFromDto(dto, entity);
        E saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void delete(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resource not found with id: " + id));
        repository.delete(entity);
    }

    protected abstract D toDto(E entity);
    protected abstract E toEntity(D dto);
    protected abstract void updateFromDto(D dto, E entity);
}

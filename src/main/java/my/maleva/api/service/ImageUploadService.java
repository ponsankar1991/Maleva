package my.maleva.api.service;

import my.maleva.api.dto.ImageUploadDto;
import my.maleva.api.exception.EntityNotFoundException;
import my.maleva.api.mapper.ImageUploadMapper;
import my.maleva.api.model.ImageUpload;
import my.maleva.api.repo.ImageUploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageUploadService {

    private final ImageUploadRepository repository;
    private final ImageUploadMapper mapper;

    public ImageUploadService(ImageUploadRepository repository, ImageUploadMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ImageUploadDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ImageUploadDto getById(Integer id) {
        ImageUpload ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ImageUpload not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ImageUploadDto create(ImageUploadDto dto) {
        LocalDateTime now = LocalDateTime.now();
        ImageUpload ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        ImageUpload saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ImageUploadDto update(Integer id, ImageUploadDto dto) {
        ImageUpload ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ImageUpload not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        ImageUpload saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        ImageUpload ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("ImageUpload not found: " + id));
        repository.delete(ent);
    }
}

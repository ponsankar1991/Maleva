package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.ClassificationDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.master.mapper.MasterClassificationMapper;
import my.maleva.api.module.master.entity.Classification;
import my.maleva.api.module.master.repository.ClassificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationService.class);
    private final ClassificationRepository repository;
    private final MasterClassificationMapper mapper;

    public ClassificationService(ClassificationRepository repository, MasterClassificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ClassificationDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Select all classifications - ASP.NET migration method
     * Retrieves all classifications from the Classification table
     * Matches the SelectClasification() pattern from .NET AccountsGroupMasterController
     *
     * @return List of ClassificationDto objects
     * @throws Exception if database query fails
     */
    public List<ClassificationDto> selectAllClassifications() throws Exception {
        try {
            logger.info("Fetching all classifications from database");
            List<Classification> classifications = repository.findAll();
            logger.debug("Found {} classifications", classifications.size());

            List<ClassificationDto> result = classifications.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

            logger.info("Successfully retrieved {} classifications", result.size());
            return result;
        } catch (Exception ex) {
            // Extract innermost exception (matching ASP.NET pattern)
            Exception realError = ex;
            while (realError.getCause() instanceof Exception) {
                realError = (Exception) realError.getCause();
            }

            logger.error("Error in selectAllClassifications", ex);
            logger.error("Root cause: {}", realError.getMessage());
            throw realError;
        }
    }

    public ClassificationDto getById(Integer id) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public ClassificationDto create(ClassificationDto dto) {
        Classification ent = mapper.toEntity(dto);
        Classification saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ClassificationDto update(Integer id, ClassificationDto dto) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        mapper.updateFromDto(dto, ent);
        Classification saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Classification ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Classification not found: " + id));
        repository.delete(ent);
    }
}

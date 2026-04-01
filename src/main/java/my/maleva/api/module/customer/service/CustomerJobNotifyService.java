package my.maleva.api.module.customer.service;

import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customer.dto.CustomerJobNotifyDto;
import my.maleva.api.module.customer.dto.CustomerJobNotifyUpsertDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.customer.dto.response.CustomerJobNotifyBulkResultDto;
import my.maleva.api.module.customer.dto.response.CustomerJobNotifySelectDto;
import my.maleva.api.module.customer.mapper.CustomerJobNotifyMapper;
import my.maleva.api.module.customer.entity.CustomerJobNotify;
import my.maleva.api.module.customer.repository.CustomerJobNotifyRepository;
import my.maleva.api.module.customer.repository.CustomerQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomerJobNotifyService {

    private final CustomerJobNotifyRepository repository;
    private final CustomerQueryRepository queryRepository;
    private final CustomerJobNotifyMapper mapper;

    public CustomerJobNotifyService(
            CustomerJobNotifyRepository repository,
            CustomerQueryRepository queryRepository,
            CustomerJobNotifyMapper mapper
    ) {
        this.repository = repository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    public List<CustomerJobNotifyDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CustomerJobNotifyDto getById(Integer id) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional(readOnly = true)
    public List<CustomerJobNotifySelectDto> selectCustomerJobNotify(Integer customerMasterRefId, Integer saleOrderRefId) {
        return queryRepository.findCustomerJobNotifications(customerMasterRefId, saleOrderRefId);
    }

    @Transactional
    public CustomerJobNotifyDto create(CustomerJobNotifyDto dto) {
        CustomerJobNotify ent = mapper.toEntity(dto);
        CustomerJobNotify saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public ResponseViewModel upsertBatch(Integer companyId, List<CustomerJobNotifyUpsertDto> requests) {
        validateBatchRequest(companyId, requests);

        Map<Integer, CustomerJobNotify> existingEntities = loadExistingEntities(companyId, requests);
        List<CustomerJobNotify> entitiesToSave = new ArrayList<>(requests.size());

        for (CustomerJobNotifyUpsertDto request : requests) {
            CustomerJobNotify entity = resolveEntityForUpsert(request, companyId, existingEntities);
            entity.setCompanyRefId(companyId);
            entity.setCustomerDetailRefId(request.getCustomerDetailRefId());
            entity.setSaleOrderRefId(request.getSaleOrderRefId());
            entity.setWhatsapp(request.getWhatsapp());
            entity.setEmail(request.getEmail());
            entity.setPhone(request.getPhone() != null ? String.valueOf(request.getPhone()) : null);
            entitiesToSave.add(entity);
        }

        List<CustomerJobNotifyDto> savedDtos = repository.saveAll(entitiesToSave).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        Integer lastSavedId = savedDtos.isEmpty() ? null : savedDtos.get(savedDtos.size() - 1).getId();
        CustomerJobNotifyBulkResultDto result = CustomerJobNotifyBulkResultDto.builder()
                .savedCount(savedDtos.size())
                .lastSavedId(lastSavedId)
                .notifications(savedDtos)
                .build();

        return ResponseViewModel.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customer job notifications saved successfully")
                .data1(result)
                .data2(lastSavedId)
                .build();
    }

    @Transactional
    public CustomerJobNotifyDto update(Integer id, CustomerJobNotifyDto dto) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        mapper.updateFromDto(dto, ent);
        CustomerJobNotify saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        CustomerJobNotify ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CustomerJobNotify not found: " + id));
        repository.delete(ent);
    }

    private void validateBatchRequest(Integer companyId, List<CustomerJobNotifyUpsertDto> requests) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID is required and must be a positive integer");
        }

        if (requests == null || requests.isEmpty()) {
            throw new InvalidRequestException("At least one customer job notification is required");
        }

        List<Integer> duplicateIds = requests.stream()
                .map(CustomerJobNotifyUpsertDto::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.groupingBy(id -> id, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicateIds.isEmpty()) {
            throw new InvalidRequestException("Duplicate notification ids in request: " + duplicateIds);
        }
    }

    private Map<Integer, CustomerJobNotify> loadExistingEntities(Integer companyId, List<CustomerJobNotifyUpsertDto> requests) {
        List<Integer> ids = requests.stream()
                .map(CustomerJobNotifyUpsertDto::getId)
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return repository.findAllById(ids).stream()
                .peek(entity -> {
                    if (!companyId.equals(entity.getCompanyRefId())) {
                        throw new InvalidRequestException("Notification does not belong to companyId: " + entity.getId());
                    }
                })
                .collect(Collectors.toMap(CustomerJobNotify::getId, entity -> entity));
    }

    private CustomerJobNotify resolveEntityForUpsert(
            CustomerJobNotifyUpsertDto request,
            Integer companyId,
            Map<Integer, CustomerJobNotify> existingEntities
    ) {
        Integer id = request.getId();
        if (id == null || id <= 0) {
            return CustomerJobNotify.builder()
                    .companyRefId(companyId)
                    .build();
        }

        CustomerJobNotify existingEntity = existingEntities.get(id);
        if (existingEntity == null) {
            throw new EntityNotFoundException("CustomerJobNotify not found: " + id);
        }

        return existingEntity;
    }
}

package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.dto.SaleDetailsViewModel;
import my.maleva.api.module.invoice.dto.SaleF5View;
import my.maleva.api.module.invoice.dto.SaleMasterViewModel;
import my.maleva.api.module.invoice.mapper.QueryResultMapper;
import my.maleva.api.module.invoice.mapper.SaleF5ViewMapper;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.entity.TaxMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.jobs.repository.JobStatusMasterRepository;
import my.maleva.api.module.master.repository.TaxMasterRepository;
import my.maleva.api.module.saleorder.dto.*;
import my.maleva.api.module.saleorder.entity.SaleOrderDelivery;
import my.maleva.api.module.saleorder.entity.SaleOrderDetails;
import my.maleva.api.module.saleorder.entity.SaleOrderForwarding;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import my.maleva.api.module.saleorder.mapper.SaleOrderDeliveryMapper;
import my.maleva.api.module.saleorder.helper.SaleOrderFilterHelper;
import my.maleva.api.module.saleorder.mapper.SaleOrderDetailsMapper;
import my.maleva.api.module.saleorder.mapper.SaleOrderForwardingMapper;
import my.maleva.api.module.saleorder.mapper.SaleOrderMasterMapper;
import my.maleva.api.module.saleorder.mapper.SaleOrderPickupMapper;
import my.maleva.api.module.saleorder.repository.SaleOrderDeliveryRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderDetailsRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderForwardingRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderPickupRepository;
import my.maleva.api.module.boardingsettlement.service.BoardingEventSyncService;
import my.maleva.api.module.saleorder.service.SaleOrderMasterService;
import my.maleva.api.module.saleorder.specification.SaleOrderSpecification;
import my.maleva.api.module.saleorder.util.SaleOrderApiConstants;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SaleOrderMasterServiceImpl - Refactored business service for sale orders.
 *
 * The current frontend already posts the existing DTOs and consumes the current
 * endpoints, so this implementation keeps that contract stable while moving the
 * logic into smaller, intention-revealing methods.
 */
@Service
@Transactional(readOnly = true)
public class SaleOrderMasterServiceImpl implements SaleOrderMasterService {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderMasterServiceImpl.class);
    private static final DateTimeFormatter QUICK_EDIT_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter QUICK_EDIT_VIEW_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final List<DateTimeFormatter> QUICK_EDIT_ACCEPTED_FORMATTERS = List.of(
            QUICK_EDIT_INPUT_FORMATTER,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    );

    private final SaleOrderMasterRepository repository;
    private final SaleOrderDetailsRepository saleOrderDetailsRepository;
    private final SaleOrderPickupRepository saleOrderPickupRepository;
    private final SaleOrderDeliveryRepository saleOrderDeliveryRepository;
    private final SaleOrderForwardingRepository saleOrderForwardingRepository;
    private final CustomerRepository customerRepository;
    private final ItemMasterRepository itemMasterRepository;
    private final TaxMasterRepository taxMasterRepository;
    private final UomRepository uomRepository;
    private final SaleOrderMasterMapper mapper;
    private final SaleOrderDetailsMapper saleOrderDetailsMapper;
    private final SaleOrderPickupMapper saleOrderPickupMapper;
    private final SaleOrderDeliveryMapper saleOrderDeliveryMapper;
    private final SaleOrderForwardingMapper saleOrderForwardingMapper;
    private final SaleF5ViewMapper saleF5ViewMapper;
    private final QueryResultMapper queryResultMapper;
    private final SaleOrderFilterHelper filterHelper;
    private final SequenceNoMasterRepository sequenceNoMasterRepository;
    private final JobStatusMasterRepository jobStatusMasterRepository;
    private final BoardingEventSyncService boardingEventSyncService;

    public SaleOrderMasterServiceImpl(SaleOrderMasterRepository repository,
                                      SaleOrderDetailsRepository saleOrderDetailsRepository,
                                      SaleOrderPickupRepository saleOrderPickupRepository,
                                      SaleOrderDeliveryRepository saleOrderDeliveryRepository,
                                      SaleOrderForwardingRepository saleOrderForwardingRepository,
                                      CustomerRepository customerRepository,
                                      ItemMasterRepository itemMasterRepository,
                                      TaxMasterRepository taxMasterRepository,
                                      UomRepository uomRepository,
                                      SaleOrderMasterMapper mapper,
                                      SaleOrderDetailsMapper saleOrderDetailsMapper,
                                      SaleOrderPickupMapper saleOrderPickupMapper,
                                      SaleOrderDeliveryMapper saleOrderDeliveryMapper,
                                      SaleOrderForwardingMapper saleOrderForwardingMapper,
                                      SaleF5ViewMapper saleF5ViewMapper,
                                      QueryResultMapper queryResultMapper,
                                      SaleOrderFilterHelper filterHelper,
                                      SequenceNoMasterRepository sequenceNoMasterRepository,
                                      JobStatusMasterRepository jobStatusMasterRepository,
                                      BoardingEventSyncService boardingEventSyncService) {
        this.repository = repository;
        this.saleOrderDetailsRepository = saleOrderDetailsRepository;
        this.saleOrderPickupRepository = saleOrderPickupRepository;
        this.saleOrderDeliveryRepository = saleOrderDeliveryRepository;
        this.saleOrderForwardingRepository = saleOrderForwardingRepository;
        this.customerRepository = customerRepository;
        this.itemMasterRepository = itemMasterRepository;
        this.taxMasterRepository = taxMasterRepository;
        this.uomRepository = uomRepository;
        this.mapper = mapper;
        this.saleOrderDetailsMapper = saleOrderDetailsMapper;
        this.saleOrderPickupMapper = saleOrderPickupMapper;
        this.saleOrderDeliveryMapper = saleOrderDeliveryMapper;
        this.saleOrderForwardingMapper = saleOrderForwardingMapper;
        this.saleF5ViewMapper = saleF5ViewMapper;
        this.queryResultMapper = queryResultMapper;
        this.filterHelper = filterHelper;
        this.sequenceNoMasterRepository = sequenceNoMasterRepository;
        this.jobStatusMasterRepository = jobStatusMasterRepository;
        this.boardingEventSyncService = boardingEventSyncService;
    }

    /**
     * Saves a full sale-order aggregate because the current page sends the
     * master row and all nested rows together.
     */
    @Override
    @Transactional
    public SaleOrderMasterDto save(SaleOrderDTO dto) {
        validateSaleOrderRequest(dto);

        boolean createOperation = isCreateOperation(dto.getId());
        String operation = createOperation
                ? SaleOrderApiConstants.CREATE_OPERATION
                : SaleOrderApiConstants.UPDATE_OPERATION;

        logger.info("Sale order {} request received - company: {}, customer: {}, id: {}",
                operation, dto.getCompanyRefId(), dto.getCustomerRefId(), dto.getId());

        SaleOrderMaster entity = createOperation
                ? buildNewSaleOrder(dto)
                : buildExistingSaleOrder(dto);

        SaleOrderMaster savedEntity = repository.saveAndFlush(entity);
        synchronizeChildRecords(savedEntity.getId(), dto, createOperation);
        boardingEventSyncService.syncEventsForJob(savedEntity);

        logger.info("Sale order {} completed successfully - id: {}", operation, savedEntity.getId());
        return mapper.toDto(savedEntity);
    }

    @Override
    public SaleOrderEditDto getById(Integer id) {
        SaleOrderMaster entity = findActiveSaleOrder(id);
        return buildEditPayload(entity);
    }

    @Override
    @Transactional
    public void updateRemarks(my.maleva.api.module.saleorder.dto.SaleOrderRemarksUpdateDto request) {
        SaleOrderMaster entity = repository.findByIdAndCompanyRefId(request.getId(), request.getCompanyRefId())
                .orElseThrow(() -> new EntityNotFoundException("SaleOrderMaster not found for id: " + request.getId()));
        
        entity.setRemarks(request.getRemarks());
        repository.save(entity);
    }
    
    @Override
    @Transactional
    public void updateRemarksBulk(java.util.List<my.maleva.api.module.saleorder.dto.SaleOrderRemarksUpdateDto> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        for (my.maleva.api.module.saleorder.dto.SaleOrderRemarksUpdateDto request : requests) {
            SaleOrderMaster entity = repository.findByIdAndCompanyRefId(request.getId(), request.getCompanyRefId())
                    .orElseThrow(() -> new EntityNotFoundException("SaleOrderMaster not found for id: " + request.getId()));
            
            entity.setRemarks(request.getRemarks());
            repository.save(entity);
        }
    }

    @Override
    public SaleOrderEditDto getEditSaleOrder(Integer id, Integer saleOrderNo, Integer companyId) {
        validateEditLookupRequest(id, saleOrderNo, companyId);

        Integer resolvedId = resolveSaleOrderId(id, saleOrderNo, companyId);
        SaleOrderMaster entity = findActiveSaleOrder(resolvedId);

        if (!Objects.equals(entity.getCompanyRefId(), companyId)) {
            throw new EntityNotFoundException(String.format(
                    SaleOrderApiConstants.MESSAGE_ORDER_NOT_FOUND,
                    resolvedId
            ));
        }

        return buildEditPayload(entity);
    }

    private SaleOrderEditDto buildEditPayload(SaleOrderMaster entity) {
        Integer saleOrderId = entity.getId();
        Integer companyId = entity.getCompanyRefId();
        SaleOrderMasterDto saleOrderMasterDto = mapper.toDto(entity);
        hydrateEditMasterDto(saleOrderMasterDto, entity, companyId);

        return SaleOrderEditDto.builder()
                .saleOrderMaster(saleOrderMasterDto)
                .saleOrderDetails(buildEditDetails(saleOrderId, companyId))
                .pickupDetails(saleOrderPickupRepository.findBySaleOrderMasterRefId(saleOrderId).stream()
                        .map(saleOrderPickupMapper::toDto)
                        .collect(Collectors.toList()))
                .deliveryDetails(saleOrderDeliveryRepository.findBySaleOrderMasterRefId(saleOrderId).stream()
                        .map(saleOrderDeliveryMapper::toDto)
                        .collect(Collectors.toList()))
                .forwardingDetails(saleOrderForwardingRepository.findBySaleOrderMasterRefId(saleOrderId).stream()
                        .map(saleOrderForwardingMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private void hydrateEditMasterDto(SaleOrderMasterDto saleOrderMasterDto, SaleOrderMaster entity, Integer companyId) {
        saleOrderMasterDto.setCNumberDisplay(resolveDisplayNumber(entity));
        saleOrderMasterDto.setCNumber(resolveResponseCNumber(entity));
        saleOrderMasterDto.setCustomerName(resolveCustomerName(entity.getCustomerRefId(), companyId));
        saleOrderMasterDto.setSPort(entity.getSPort());
        saleOrderMasterDto.setJStatus(entity.getJStatus());
        saleOrderMasterDto.setStatusName(resolveStatusName(entity.getJStatus()));
        saleOrderMasterDto.setOStatus(entity.getOStatus());
        saleOrderMasterDto.setOAgentCompanyRefId(entity.getOAgentCompanyRefId());
        saleOrderMasterDto.setOAgentMasterRefId(entity.getOAgentMasterRefId());
        saleOrderMasterDto.setOVessel(entity.getOVessel());
        saleOrderMasterDto.setOPort(entity.getOPort());
        saleOrderMasterDto.setLBoardingOfficerRefid(entity.getLBoardingOfficerRefid());
        saleOrderMasterDto.setLBoardingOfficer1Refid(entity.getLBoardingOfficer1Refid());
        saleOrderMasterDto.setLBoardingAmount(entity.getLBoardingAmount());
        saleOrderMasterDto.setLBoardingAmount1(entity.getLBoardingAmount1());
        saleOrderMasterDto.setLPortChargesRef(entity.getLPortChargesRef());
        saleOrderMasterDto.setLPortCharges(entity.getLPortCharges());
        saleOrderMasterDto.setOBoardingOfficerRefid(entity.getOBoardingOfficerRefid());
        saleOrderMasterDto.setOBoardingOfficer1Refid(entity.getOBoardingOfficer1Refid());
        saleOrderMasterDto.setOBoardingAmount(entity.getOBoardingAmount());
        saleOrderMasterDto.setOBoardingAmount1(entity.getOBoardingAmount1());
        saleOrderMasterDto.setOPortChargesRef(entity.getOPortChargesRef());
        saleOrderMasterDto.setOPortCharges(entity.getOPortCharges());

        saleOrderMasterDto.setBoardingOfficer2Refid(entity.getBoardingOfficer2Refid());
        saleOrderMasterDto.setBoardingAmount2(entity.getBoardingAmount2());
        saleOrderMasterDto.setLBoardingOfficer2Refid(entity.getLBoardingOfficer2Refid());
        saleOrderMasterDto.setLBoardingAmount2(entity.getLBoardingAmount2());
        saleOrderMasterDto.setOBoardingOfficer2Refid(entity.getOBoardingOfficer2Refid());
        saleOrderMasterDto.setOBoardingAmount2(entity.getOBoardingAmount2());
    }

    private Integer resolveResponseCNumber(SaleOrderMaster entity) {
        if (entity == null) {
            return null;
        }

        if (hasPositiveCNumber(entity.getCNumber())) {
            return entity.getCNumber();
        }

        return extractCNumberFromDisplay(entity.getCNumberDisplay());
    }

    private String resolveCustomerName(Integer customerRefId, Integer companyId) {
        if (customerRefId == null || companyId == null) {
            return null;
        }

        return customerRepository.findByIdAndCompanyRefId(customerRefId, companyId)
                .map(customer -> customer.getCustomerName() != null ? customer.getCustomerName().trim() : null)
                .orElse(null);
    }

    private String resolveStatusName(Integer statusId) {
        if (statusId == null || statusId <= 0) {
            return null;
        }

        return jobStatusMasterRepository.findById(statusId)
                .map(status -> status.getName() != null ? status.getName().trim() : null)
                .orElse(null);
    }

    private void validateEditLookupRequest(Integer id, Integer saleOrderNo, Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_ID_REQUIRED);
        }

        boolean hasId = id != null && id > 0;
        boolean hasSaleOrderNo = saleOrderNo != null && saleOrderNo > 0;
        if (!hasId && !hasSaleOrderNo) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_EDIT_LOOKUP_REQUIRED);
        }
    }

    private Integer resolveSaleOrderId(Integer id, Integer saleOrderNo, Integer companyId) {
        if (saleOrderNo != null && saleOrderNo > 0) {
            return repository.findByCompanyRefIdAndCNumberAndActive(
                            companyId,
                            saleOrderNo,
                            SaleOrderApiConstants.ACTIVE_STATUS
                    )
                    .map(SaleOrderMaster::getId)
                    .orElseThrow(() -> new EntityNotFoundException(SaleOrderApiConstants.MESSAGE_SALE_ORDER_NO_NOT_FOUND));
        }
        return id;
    }

    private List<SaleOrderEditDetailsDto> buildEditDetails(Integer saleOrderId, Integer companyId) {
        List<SaleOrderDetails> detailEntities = saleOrderDetailsRepository.findBySaleOrderMasterRefId(saleOrderId);
        if (detailEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, ItemMaster> itemMasterMap = loadItemMasterMap(detailEntities);
        Map<Integer, TaxMaster> taxMasterMap = loadTaxMasterMap(detailEntities, companyId);
        Map<Integer, Uom> uomMap = loadUomMap(itemMasterMap.values(), companyId);

        return detailEntities.stream()
                .map(detail -> toEditDetailDto(detail, itemMasterMap, taxMasterMap, uomMap))
                .collect(Collectors.toList());
    }

    private Map<Integer, ItemMaster> loadItemMasterMap(List<SaleOrderDetails> detailEntities) {
        List<Integer> itemIds = detailEntities.stream()
                .map(SaleOrderDetails::getItemMasterRefId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, ItemMaster> itemMasterMap = new HashMap<>();
        itemMasterRepository.findAllById(itemIds)
                .forEach(item -> itemMasterMap.put(item.getId(), item));
        return itemMasterMap;
    }

    private Map<Integer, TaxMaster> loadTaxMasterMap(List<SaleOrderDetails> detailEntities, Integer companyId) {
        List<Integer> taxIds = detailEntities.stream()
                .map(SaleOrderDetails::getTaxRefId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, TaxMaster> taxMasterMap = new HashMap<>();
        taxMasterRepository.findAllById(taxIds).stream()
                .filter(tax -> Objects.equals(tax.getCompanyRefId(), companyId))
                .forEach(tax -> taxMasterMap.put(tax.getId(), tax));
        return taxMasterMap;
    }

    private Map<Integer, Uom> loadUomMap(java.util.Collection<ItemMaster> itemMasters, Integer companyId) {
        List<Integer> uomIds = itemMasters.stream()
                .map(ItemMaster::getUomCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Uom> uomMap = new HashMap<>();
        uomRepository.findAllById(uomIds).stream()
                .filter(uom -> Objects.equals(uom.getCompanyRefId(), companyId))
                .forEach(uom -> uomMap.put(uom.getId(), uom));
        return uomMap;
    }

    private SaleOrderEditDetailsDto toEditDetailDto(SaleOrderDetails detail,
                                                    Map<Integer, ItemMaster> itemMasterMap,
                                                    Map<Integer, TaxMaster> taxMasterMap,
                                                    Map<Integer, Uom> uomMap) {
        SaleOrderDetailsDto baseDto = saleOrderDetailsMapper.toDto(detail);
        SaleOrderEditDetailsDto editDto = new SaleOrderEditDetailsDto();

        editDto.setId(baseDto.getId());
        editDto.setSaleOrderMasterRefId(baseDto.getSaleOrderMasterRefId());
        editDto.setItemMasterRefId(baseDto.getItemMasterRefId());
        editDto.setMrp(baseDto.getMrp());
        editDto.setPurchaseRate(baseDto.getPurchaseRate());
        editDto.setItemQty(baseDto.getItemQty());
        editDto.setDiscPer(baseDto.getDiscPer());
        editDto.setDiscAmount(baseDto.getDiscAmount());
        editDto.setLandingCost(baseDto.getLandingCost());
        editDto.setTaxPercent(baseDto.getTaxPercent());
        editDto.setTaxAmount(baseDto.getTaxAmount());
        editDto.setSalesRate(baseDto.getSalesRate());
        editDto.setNetSalesRate(baseDto.getNetSalesRate());
        editDto.setAmount(baseDto.getAmount());
        editDto.setCreatedDate(baseDto.getCreatedDate());
        editDto.setModifiedDate(baseDto.getModifiedDate());
        editDto.setCurrencyValue(baseDto.getCurrencyValue());
        editDto.setActualAmount(baseDto.getActualAmount());
        editDto.setSdRemarks(baseDto.getSdRemarks());
        editDto.setTaxRefId(baseDto.getTaxRefId());

        ItemMaster itemMaster = itemMasterMap.get(baseDto.getItemMasterRefId());
        if (itemMaster != null) {
            editDto.setProductCode(itemMaster.getProdCode());
            editDto.setProductName(itemMaster.getPName());

            Uom uom = uomMap.get(itemMaster.getUomCode());
            if (uom != null) {
                editDto.setUom(uom.getDescription());
            }
        }

        TaxMaster taxMaster = taxMasterMap.get(baseDto.getTaxRefId());
        if (taxMaster != null) {
            editDto.setTaxCode(taxMaster.getCode());
        }

        return editDto;
    }

    /**
     * Updates the full sale-order aggregate because the edit page submits the
     * master row together with nested child collections.
     */
    @Override
    @Transactional
    public SaleOrderMasterDto update(Integer id, SaleOrderDTO dto) {
        if (dto == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_UPDATE_FAILED);
        }

        if (dto.getId() != null && dto.getId() > 0 && !Objects.equals(dto.getId(), id)) {
            logger.warn("Sale order update request body id {} did not match path id {}. Using path id.",
                    dto.getId(), id);
        }

        dto.setId(id);
        return save(dto);
    }

    /**
     * Updates only the master row because some smaller workflows do not submit
     * nested child records during edit operations.
     */
    @Override
    @Transactional
    public SaleOrderMasterDto updateMaster(Integer id, SaleOrderMasterDto dto) {
        SaleOrderMaster entity = findActiveSaleOrder(id);
        Integer originalStatusId = entity.getJStatus();
        validateMasterUpdateRequest(dto, entity);
        calculateOrderTotals(dto);

        mapper.updateEntityFromDto(dto, entity);

        // Apply pickup/delivery dates from DTO directly. When DTO fields are null this will
        // clear the stored values in the database. This matches the client's request to
        // set the column to NULL when the DTO contains null for these fields.
        entity.setPickupDate(dto.getPickupDate());
        entity.setDeliveryDate(dto.getDeliveryDate());

        Integer targetStatusId = entity.getJStatus();
        if (!Objects.equals(originalStatusId, targetStatusId)) {
            String targetStatusName = resolveStatusName(targetStatusId);
            checkPendingPortCharges(entity, targetStatusId, targetStatusName);
        }

        entity.setId(id);
        Integer resolvedCNumber = resolveUpdateCNumber(dto, entity);
        if (hasPositiveCNumber(resolvedCNumber)) {
            entity.setCNumber(resolvedCNumber);
        }
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        sanitizeEntity(entity);
        initializeNumericDefaults(entity);
        validateCriticalIdentifiers(entity);

        SaleOrderMaster updatedEntity = repository.save(entity);
        boardingEventSyncService.syncEventsForJob(updatedEntity);
        
        logger.info("Sale order master updated successfully - id: {}", id);
        return mapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    public SaleOrderStatusUpdateDto updateStatus(Integer id, Integer companyId, Integer jobStatusId) {
        validateStatusUpdateRequest(id, companyId, jobStatusId);

        SaleOrderMaster entity = findActiveSaleOrder(id);
        if (!Objects.equals(entity.getCompanyRefId(), companyId)) {
            throw new EntityNotFoundException(String.format(
                    SaleOrderApiConstants.MESSAGE_ORDER_NOT_FOUND,
                    id
            ));
        }

        JobStatusMaster statusEntity = jobStatusMasterRepository.findById(jobStatusId)
                .filter(status -> Objects.equals(status.getCompanyRefId(), companyId))
                .filter(status -> !Objects.equals(status.getActive(), SaleOrderApiConstants.INACTIVE_STATUS))
                .orElseThrow(() -> new InvalidRequestException(SaleOrderApiConstants.MESSAGE_JOB_STATUS_INVALID));

        checkPendingPortCharges(entity, statusEntity.getId(), statusEntity.getName());

        entity.setJStatus(statusEntity.getId());
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        SaleOrderMaster updatedEntity = repository.save(entity);

        logger.info("Sale order status updated successfully - id: {}, jStatus: {}",
                updatedEntity.getId(), updatedEntity.getJStatus());

        return SaleOrderStatusUpdateDto.builder()
                .id(updatedEntity.getId())
                .companyRefId(updatedEntity.getCompanyRefId())
                .jStatus(updatedEntity.getJStatus())
                .statusName(normalizeOptionalValue(statusEntity.getName()))
                .cNumberDisplay(resolveDisplayNumber(updatedEntity))
                .build();
    }

    @Override
    @Transactional
    public SaleOrderQuickUpdateDto updateQuickFields(Integer id, SaleOrderQuickUpdateDto dto) {
        validateQuickUpdateRequest(id, dto);

        SaleOrderMaster entity = findActiveSaleOrder(id);
        if (!Objects.equals(entity.getCompanyRefId(), dto.getCompanyRefId())) {
            throw new EntityNotFoundException(String.format(
                    SaleOrderApiConstants.MESSAGE_ORDER_NOT_FOUND,
                    id
            ));
        }

        if (dto.getJStatus() != null && dto.getJStatus() > 0) {
            JobStatusMaster statusEntity = jobStatusMasterRepository.findById(dto.getJStatus())
                    .filter(status -> Objects.equals(status.getCompanyRefId(), dto.getCompanyRefId()))
                    .filter(status -> !Objects.equals(status.getActive(), SaleOrderApiConstants.INACTIVE_STATUS))
                    .orElseThrow(() -> new InvalidRequestException(SaleOrderApiConstants.MESSAGE_JOB_STATUS_INVALID));
            checkPendingPortCharges(entity, statusEntity.getId(), statusEntity.getName());
            entity.setJStatus(statusEntity.getId());
        }

        entity.setEta(parseQuickUpdateDateTime(dto.getEta()));
        entity.setEtb(parseQuickUpdateDateTime(dto.getEtb()));
        entity.setOeta(parseQuickUpdateDateTime(dto.getOeta()));
        entity.setOetb(parseQuickUpdateDateTime(dto.getOetb()));
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        SaleOrderMaster updatedEntity = repository.save(entity);
        logger.info("Sale order quick update completed successfully - id: {}", updatedEntity.getId());

        return SaleOrderQuickUpdateDto.builder()
                .id(updatedEntity.getId())
                .companyRefId(updatedEntity.getCompanyRefId())
                .jStatus(updatedEntity.getJStatus())
                .statusName(resolveStatusName(updatedEntity.getJStatus()))
                .cNumberDisplay(resolveDisplayNumber(updatedEntity))
                .eta(formatQuickEditInputDate(updatedEntity.getEta()))
                .etb(formatQuickEditInputDate(updatedEntity.getEtb()))
                .oeta(formatQuickEditInputDate(updatedEntity.getOeta()))
                .oetb(formatQuickEditInputDate(updatedEntity.getOetb()))
                .seta(formatQuickEditViewDate(updatedEntity.getEta()))
                .setb(formatQuickEditViewDate(updatedEntity.getEtb()))
                .soeta(formatQuickEditViewDate(updatedEntity.getOeta()))
                .soetb(formatQuickEditViewDate(updatedEntity.getOetb()))
                .build();
    }

    /**
     * Performs a logical delete because the rest of the module already filters
     * active sale orders by the Active flag.
     */
    @Override
    @Transactional
    public boolean delete(Integer id) {
        SaleOrderMaster entity = findActiveSaleOrder(id);
        entity.setActive(SaleOrderApiConstants.INACTIVE_STATUS);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        repository.save(entity);
        logger.info("Sale order deleted successfully - id: {}", id);
        return true;
    }

    /**
     * Update Job Status for Sale Order
     *
     * Equivalent C# Method: public ResponseViewModel UpdateJobStatus(Int32 Id, Int32 JobStatusId)
     *
     * SQL equivalent: UPDATE SaleOrderMaster SET JStatus = :jobStatusId, Modified_Date = GETDATE() WHERE Id = :id
     *
     * Business Logic:
     * 1. Validate ID is provided and positive
     * 2. Find SaleOrderMaster record by ID
     * 3. Update JStatus field with new jobStatusId
     * 4. Set Modified_Date to current timestamp
     * 5. Save to database and return updated status
     *
     * @param id Sale Order ID (primary key)
     * @param jobStatusId New job status ID to set on JStatus field
     * @return SaleOrderStatusUpdateDto with updated status and audit info
     * @throws InvalidRequestException if id or jobStatusId is invalid
     * @throws EntityNotFoundException if record not found
     */
    @Override
    @Transactional
    public SaleOrderStatusUpdateDto updateJobStatus(Integer id, Integer jobStatusId) {
        logger.info("Updating Job Status - id: {}, jobStatusId: {}", id, jobStatusId);

        // Validate input parameters
        if (id == null || id <= 0) {
            throw new InvalidRequestException("Sale Order ID is required and must be greater than 0");
        }

        if (jobStatusId == null || jobStatusId <= 0) {
            throw new InvalidRequestException("Job Status ID is required and must be greater than 0");
        }

        // Find the sale order (must be active)
        SaleOrderMaster entity = findActiveSaleOrder(id);

        // Validate and get job status
        JobStatusMaster statusEntity = jobStatusMasterRepository.findById(jobStatusId)
                .filter(status -> !Objects.equals(status.getActive(), SaleOrderApiConstants.INACTIVE_STATUS))
                .orElseThrow(() -> new InvalidRequestException("Invalid Job Status ID"));

        checkPendingPortCharges(entity, statusEntity.getId(), statusEntity.getName());

        // Update JStatus and modified date
        entity.setJStatus(statusEntity.getId());
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        // Save the updated entity
        SaleOrderMaster updatedEntity = repository.save(entity);

        logger.info("Job Status updated successfully - Sale Order ID: {}, New JStatus: {}, Modified Date: {}",
                updatedEntity.getId(), updatedEntity.getJStatus(), updatedEntity.getModifiedDate());

        // Return response DTO
        return SaleOrderStatusUpdateDto.builder()
                .id(updatedEntity.getId())
                .companyRefId(updatedEntity.getCompanyRefId())
                .jStatus(updatedEntity.getJStatus())
                .statusName(normalizeOptionalValue(statusEntity.getName()))
                .cNumberDisplay(resolveDisplayNumber(updatedEntity))
                .modifiedDate(updatedEntity.getModifiedDate())
                .message("Update JobStatus Successfully!")
                .build();
    }

    /**
     * Executes the existing SelectSaleOrder contract because the current page
     * already depends on this response shape.
     */
    @Override
    public SaleF5View selectSaleOrder(SaleOrderFilterDTO filter) {
        filterHelper.validateFilter(filter);
        filterHelper.logFilterDetails(filter);

        logger.info("SelectSaleOrder started - company: {}", filter.getComid());
        long startTime = System.currentTimeMillis();

        Specification<SaleOrderMaster> specification = buildFilterSpecification(filter);
        List<Integer> filteredOrderIds = getFilteredOrderIds(specification);

        if (filteredOrderIds.isEmpty()) {
            logger.info("SelectSaleOrder completed with no matching records - company: {}", filter.getComid());
            return buildEmptySaleF5ViewResponse();
        }

        List<SaleMasterViewModel> saleMasterList = fetchAndMapSaleMasterData(filter.getComid(), filteredOrderIds);
        List<SaleDetailsViewModel> saleDetailsList = fetchAndMapSaleDetailsData(filter.getComid(), filteredOrderIds);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("SelectSaleOrder completed in {} ms - company: {}, records: {}",
                duration, filter.getComid(), filteredOrderIds.size());

        return buildSaleF5ViewResponse(saleMasterList, saleDetailsList);
    }

    private boolean isCreateOperation(Integer id) {
        return id == null || id == 0;
    }

    private void validateSaleOrderRequest(SaleOrderDTO dto) {
        if (dto == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_SAVE_FAILED);
        }
        if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_REF_REQUIRED);
        }
        if (dto.getCustomerRefId() == null || dto.getCustomerRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CUSTOMER_REF_REQUIRED);
        }
        if (!isCreateOperation(dto.getId()) && !hasPositiveCNumber(resolveUpdateCNumber(dto))) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CNUMBER_REQUIRED);
        }
    }

    private void validateMasterUpdateRequest(SaleOrderMasterDto dto, SaleOrderMaster existingEntity) {
        if (dto == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_UPDATE_FAILED);
        }
        if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_REF_REQUIRED);
        }
        if (dto.getCustomerRefId() == null || dto.getCustomerRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CUSTOMER_REF_REQUIRED);
        }
        if (dto.getSaleDate() == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_SALE_DATE_REQUIRED);
        }
        if (!hasPositiveCNumber(resolveUpdateCNumber(dto, existingEntity))) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CNUMBER_REQUIRED);
        }
    }

    private void validateStatusUpdateRequest(Integer id, Integer companyId, Integer jobStatusId) {
        if (id == null || id <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_UPDATE_FAILED);
        }
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_ID_REQUIRED);
        }
        if (jobStatusId == null || jobStatusId <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_JOB_STATUS_REQUIRED);
        }
    }

    private void validateQuickUpdateRequest(Integer id, SaleOrderQuickUpdateDto dto) {
        if (id == null || id <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_UPDATE_FAILED);
        }
        if (dto == null) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_UPDATE_FAILED);
        }
        if (dto.getCompanyRefId() == null || dto.getCompanyRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_ID_REQUIRED);
        }
        if (dto.getJStatus() != null && dto.getJStatus() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_JOB_STATUS_REQUIRED);
        }
    }

    private SaleOrderMaster buildNewSaleOrder(SaleOrderDTO dto) {
        SaleOrderMaster entity = mapper.toEntity(dto);
        entity.setId(null);
        entity.setActive(SaleOrderApiConstants.ACTIVE_STATUS);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        entity.setCreatedBy(resolveAuditUser(entity.getCreatedBy()));
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        assignSequenceValues(entity, dto.getCNumberDisplay());
        sanitizeEntity(entity);
        initializeNumericDefaults(entity);
        validateCriticalIdentifiers(entity);

        return entity;
    }

    private SaleOrderMaster buildExistingSaleOrder(SaleOrderDTO dto) {
        SaleOrderMaster entity = findActiveSaleOrder(dto.getId());
        Integer originalStatusId = entity.getJStatus();
        mapper.updateEntityFromDto(dto, entity);

        Integer targetStatusId = entity.getJStatus();
        if (!Objects.equals(originalStatusId, targetStatusId)) {
            String targetStatusName = resolveStatusName(targetStatusId);
            checkPendingPortCharges(entity, targetStatusId, targetStatusName);
        }

        // Manually allow clearing of dates if the frontend sends null or empty strings
        if (dto.getEta() == null || dto.getEta().trim().isEmpty()) entity.setEta(null);
        if (dto.getEtb() == null || dto.getEtb().trim().isEmpty()) entity.setEtb(null);
        if (dto.getEtd() == null || dto.getEtd().trim().isEmpty()) entity.setEtd(null);
        if (dto.getOeta() == null || dto.getOeta().trim().isEmpty()) entity.setOeta(null);
        if (dto.getOetb() == null || dto.getOetb().trim().isEmpty()) entity.setOetb(null);
        if (dto.getOetd() == null || dto.getOetd().trim().isEmpty()) entity.setOetd(null);
        // Allow clearing or updating of pickup/delivery dates (DTO carries strings)
        if (dto.getPickupDate() == null || dto.getPickupDate().trim().isEmpty()) {
            entity.setPickupDate(null);
        } else {
            entity.setPickupDate(parseQuickUpdateDateTime(dto.getPickupDate()));
        }

        if (dto.getDeliveryDate() == null || dto.getDeliveryDate().trim().isEmpty()) {
            entity.setDeliveryDate(null);
        } else {
            entity.setDeliveryDate(parseQuickUpdateDateTime(dto.getDeliveryDate()));
        }

        Integer resolvedCNumber = resolveUpdateCNumber(dto);
        if (hasPositiveCNumber(resolvedCNumber)) {
            entity.setCNumber(resolvedCNumber);
        }
        if (dto.getCNumberDisplay() != null && !dto.getCNumberDisplay().trim().isEmpty()) {
            entity.setCNumberDisplay(dto.getCNumberDisplay().trim());
        } else if (isMissingDisplayNumber(entity.getCNumberDisplay())
                && entity.getCNumber() != null
                && entity.getCNumber() > 0) {
            entity.setCNumberDisplay(buildDisplayNumber(entity.getBillType(), entity.getCNumber()));
        }

        entity.setActive(SaleOrderApiConstants.ACTIVE_STATUS);
        entity.setModifiedDate(LocalDateTime.now());
        entity.setModifiedBy(resolveAuditUser(entity.getModifiedBy()));

        sanitizeEntity(entity);
        initializeNumericDefaults(entity);
        validateCriticalIdentifiers(entity);

        return entity;
    }

    private Integer resolveUpdateCNumber(SaleOrderDTO dto) {
        if (dto == null) {
            return null;
        }

        if (hasPositiveCNumber(dto.getCNumber())) {
            return dto.getCNumber();
        }

        Integer parsedFromDisplay = extractCNumberFromDisplay(dto.getCNumberDisplay());
        if (hasPositiveCNumber(parsedFromDisplay)) {
            return parsedFromDisplay;
        }

        if (!isCreateOperation(dto.getId())) {
            return repository.findByIdAndActive(dto.getId(), SaleOrderApiConstants.ACTIVE_STATUS)
                    .map(SaleOrderMaster::getCNumber)
                    .filter(this::hasPositiveCNumber)
                    .orElse(null);
        }

        return null;
    }

    private Integer resolveUpdateCNumber(SaleOrderMasterDto dto, SaleOrderMaster existingEntity) {
        if (dto == null) {
            return null;
        }

        if (hasPositiveCNumber(dto.getCNumber())) {
            return dto.getCNumber();
        }

        Integer parsedFromDisplay = extractCNumberFromDisplay(dto.getCNumberDisplay());
        if (hasPositiveCNumber(parsedFromDisplay)) {
            return parsedFromDisplay;
        }

        if (existingEntity != null && hasPositiveCNumber(existingEntity.getCNumber())) {
            return existingEntity.getCNumber();
        }

        return null;
    }

    private String resolveDisplayNumber(SaleOrderMaster entity) {
        if (entity == null) {
            return null;
        }

        String existingDisplayNumber = normalizeOptionalValue(entity.getCNumberDisplay());
        if (!isMissingDisplayNumber(existingDisplayNumber)) {
            return existingDisplayNumber;
        }

        if (entity.getCNumber() != null && entity.getCNumber() > 0) {
            return buildDisplayNumber(entity.getBillType(), entity.getCNumber());
        }

        return existingDisplayNumber;
    }

    private LocalDateTime parseQuickUpdateDateTime(String value) {
        String normalizedValue = normalizeOptionalValue(value);
        if (normalizedValue == null) {
            return null;
        }

        for (DateTimeFormatter formatter : QUICK_EDIT_ACCEPTED_FORMATTERS) {
            try {
                return LocalDateTime.parse(normalizedValue, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted format.
            }
        }

        try {
            return LocalDateTime.parse(normalizedValue);
        } catch (DateTimeParseException exception) {
            throw new InvalidRequestException("Invalid date/time value: " + normalizedValue, exception);
        }
    }

    private String formatQuickEditInputDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(QUICK_EDIT_INPUT_FORMATTER);
    }

    private String formatQuickEditViewDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(QUICK_EDIT_VIEW_FORMATTER);
    }

    private boolean isMissingDisplayNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String normalized = value.trim();
        return "0".equals(normalized) || "0.0".equals(normalized) || "0.00".equals(normalized);
    }

    private boolean hasPositiveCNumber(Integer value) {
        return value != null && value > 0;
    }

    private Integer extractCNumberFromDisplay(String cNumberDisplay) {
        if (cNumberDisplay == null || cNumberDisplay.trim().isEmpty()) {
            return null;
        }

        String digitsOnly = cNumberDisplay.replaceAll("\\D+", "");
        if (digitsOnly.isEmpty()) {
            return null;
        }

        try {
            int parsed = Integer.parseInt(digitsOnly);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            logger.warn("Unable to parse cNumber from display value: {}", cNumberDisplay);
            return null;
        }
    }

    private void assignSequenceValues(SaleOrderMaster entity, String cNumberDisplayFromRequest) {
        if (entity.getCNumber() == null || entity.getCNumber() <= 0) {
            Integer generatedCNumber = generateCNumber(entity.getCompanyRefId(), entity.getBillType());
            entity.setCNumber(generatedCNumber);
            entity.setCNumberDisplay(buildDisplayNumber(entity.getBillType(), generatedCNumber));
            return;
        }

        if (cNumberDisplayFromRequest != null && !cNumberDisplayFromRequest.trim().isEmpty()) {
            entity.setCNumberDisplay(cNumberDisplayFromRequest.trim());
            return;
        }

        entity.setCNumberDisplay(buildDisplayNumber(entity.getBillType(), entity.getCNumber()));
    }

    private Integer generateCNumber(Integer companyRefId, String billType) {
        try {
            String sequenceName = buildSequenceName(billType);
            Optional<SequenceNoMaster> existingSequence = sequenceNoMasterRepository
                    .findByCompanyRefIdAndSequenceName(companyRefId, sequenceName);

            int currentSequence = existingSequence
                    .map(SequenceNoMaster::getSequenceNo)
                    .orElseGet(() -> Optional.ofNullable(
                            sequenceNoMasterRepository.findMaxSequenceNoByCompanyAndSequenceName(companyRefId, sequenceName)
                    ).orElse(0));

            int nextSequence = currentSequence + 1;
            SequenceNoMaster sequenceEntity = existingSequence.orElseGet(() -> createSequenceEntity(companyRefId, sequenceName));
            sequenceEntity.setSequenceNo(nextSequence);
            sequenceEntity.setSequenceDate(LocalDateTime.now());

            sequenceNoMasterRepository.save(sequenceEntity);
            logger.debug("Generated sale order sequence - company: {}, sequenceName: {}, value: {}",
                    companyRefId, sequenceName, nextSequence);

            return nextSequence;
        } catch (Exception exception) {
            logger.error("Unable to generate sale order sequence for company: {}", companyRefId, exception);
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_SEQUENCE_GENERATION_FAILED, exception);
        }
    }

    private SequenceNoMaster createSequenceEntity(Integer companyRefId, String sequenceName) {
        return SequenceNoMaster.builder()
                .companyRefId(companyRefId)
                .sequenceName(sequenceName)
                .sequenceNo(0)
                .sequenceDate(LocalDateTime.now())
                .build();
    }

    private String buildSequenceName(String billType) {
        return SaleOrderApiConstants.SEQUENCE_NAME_PREFIX + normalizeRequiredValue(
                billType,
                SaleOrderApiConstants.DEFAULT_BILL_TYPE
        );
    }

    private String buildDisplayNumber(String billType, Integer cNumber) {
        return normalizeRequiredValue(billType, SaleOrderApiConstants.DEFAULT_BILL_TYPE)
                + String.format(SaleOrderApiConstants.ORDER_NUMBER_PATTERN, cNumber);
    }

    private void synchronizeChildRecords(Integer masterId, SaleOrderDTO dto, boolean createOperation) {
        synchronizeSaleDetails(masterId, dto.getSaleOrderDetails(), createOperation);
        synchronizePickupDetails(masterId, dto.getPickupDetails(), createOperation);
        synchronizeDeliveryDetails(masterId, dto.getDeliveryDetails(), createOperation);
        synchronizeForwardingDetails(masterId, dto.getForwardingDetails(), createOperation);
    }

    private void synchronizeSaleDetails(Integer masterId,
                                        List<SaleOrderDetailsDto> detailDtos,
                                        boolean createOperation) {
        if (detailDtos == null) {
            return;
        }

        if (!createOperation) {
            saleOrderDetailsRepository.deleteAllBySaleOrderMasterRefId(masterId);
        }

        saveSaleDetails(masterId, detailDtos);
    }

    private void synchronizePickupDetails(Integer masterId,
                                          List<PickupDetailDTO> pickupDtos,
                                          boolean createOperation) {
        if (pickupDtos == null) {
            return;
        }

        if (!createOperation) {
            saleOrderPickupRepository.deleteAllBySaleOrderMasterRefId(masterId);
        }

        savePickupDetails(masterId, pickupDtos);
    }

    private void synchronizeDeliveryDetails(Integer masterId,
                                            List<DeliveryDetailDTO> deliveryDtos,
                                            boolean createOperation) {
        if (deliveryDtos == null) {
            return;
        }

        if (!createOperation) {
            saleOrderDeliveryRepository.deleteAllBySaleOrderMasterRefId(masterId);
        }

        saveDeliveryDetails(masterId, deliveryDtos);
    }

    private void synchronizeForwardingDetails(Integer masterId,
                                              List<ForwardingDetailDTO> forwardingDtos,
                                              boolean createOperation) {
        if (forwardingDtos == null) {
            return;
        }

        if (!createOperation) {
            saleOrderForwardingRepository.deleteAllBySaleOrderMasterRefId(masterId);
        }

        saveForwardingDetails(masterId, forwardingDtos);
    }

    private void saveSaleDetails(Integer masterId, List<SaleOrderDetailsDto> detailDtos) {
        if (detailDtos == null || detailDtos.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SaleOrderDetails> saleOrderDetails = saleOrderDetailsMapper.toEntityList(detailDtos);
        for (SaleOrderDetails detail : saleOrderDetails) {
            detail.setId(null);
            detail.setSaleOrderMasterRefId(masterId);
            detail.setCreatedDate(now);
            detail.setModifiedDate(now);
        }

        saleOrderDetailsRepository.saveAll(saleOrderDetails);
    }

    private void savePickupDetails(Integer masterId, List<PickupDetailDTO> pickupDtos) {
        if (pickupDtos == null || pickupDtos.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SaleOrderPickup> pickupEntities = Optional.ofNullable(mapper.toSaleOrderPickupentity(pickupDtos))
                .orElse(Collections.emptyList());

        for (SaleOrderPickup pickup : pickupEntities) {
            pickup.setId(null);
            pickup.setSaleOrderMasterRefId(masterId);
            pickup.setCreatedDate(now);
        }

        saleOrderPickupRepository.saveAll(pickupEntities);
    }

    private void saveDeliveryDetails(Integer masterId, List<DeliveryDetailDTO> deliveryDtos) {
        if (deliveryDtos == null || deliveryDtos.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SaleOrderDelivery> deliveryEntities = Optional.ofNullable(mapper.toSaleOrderDeliveryentity(deliveryDtos))
                .orElse(Collections.emptyList());

        for (SaleOrderDelivery delivery : deliveryEntities) {
            delivery.setId(null);
            delivery.setSaleOrderMasterRefId(masterId);
            delivery.setCreatedDate(now);
        }

        saleOrderDeliveryRepository.saveAll(deliveryEntities);
    }

    private void saveForwardingDetails(Integer masterId, List<ForwardingDetailDTO> forwardingDtos) {
        if (forwardingDtos == null || forwardingDtos.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SaleOrderForwarding> forwardingEntities = forwardingDtos.stream()
                .map(this::toForwardingEntity)
                .collect(Collectors.toList());

        for (SaleOrderForwarding forwarding : forwardingEntities) {
            forwarding.setId(null);
            forwarding.setSaleOrderMasterRefId(masterId);
            forwarding.setCreatedDate(now);
            forwarding.setModifiedDate(now);
        }

        saleOrderForwardingRepository.saveAll(forwardingEntities);
    }

    private SaleOrderForwarding toForwardingEntity(ForwardingDetailDTO dto) {
        SaleOrderForwarding entity = saleOrderForwardingMapper.toEntity(dto);
        try {
            if (dto.getSealByRefId() != null && !dto.getSealByRefId().trim().isEmpty()) {
                entity.setSealByRefId(Integer.parseInt(dto.getSealByRefId().trim()));
            }
            if (dto.getBreakSealByRefId() != null && !dto.getBreakSealByRefId().trim().isEmpty()) {
                entity.setBreakSealByRefId(Integer.parseInt(dto.getBreakSealByRefId().trim()));
            }
        } catch (NumberFormatException e) {
            logger.error("Could not parse integer from string: " + e.getMessage());
        }
        return entity;
    }

    private SaleOrderMaster findActiveSaleOrder(Integer id) {
        return repository.findByIdAndActive(id, SaleOrderApiConstants.ACTIVE_STATUS)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        SaleOrderApiConstants.MESSAGE_ORDER_NOT_FOUND, id
                )));
    }

    private void sanitizeEntity(SaleOrderMaster entity) {
        entity.setBillType(normalizeRequiredValue(entity.getBillType(), SaleOrderApiConstants.DEFAULT_BILL_TYPE));
        entity.setSaleType(normalizeRequiredValue(entity.getSaleType(), SaleOrderApiConstants.DEFAULT_SALE_TYPE));

        entity.setRemarks(normalizeOptionalValue(entity.getRemarks()));
        entity.setRemarks1(normalizeOptionalValue(entity.getRemarks1()));
        entity.setOrigin(normalizeOptionalValue(entity.getOrigin()));
        entity.setDestination(normalizeOptionalValue(entity.getDestination()));
        entity.setPickupAddress(normalizeOptionalValue(entity.getPickupAddress()));
        entity.setDeliveryAddress(normalizeOptionalValue(entity.getDeliveryAddress()));
        entity.setOffvesselname(normalizeOptionalValue(entity.getOffvesselname()));
        entity.setLoadingvesselname(normalizeOptionalValue(entity.getLoadingvesselname()));
    }

    private String normalizeRequiredValue(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String normalizeOptionalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String resolveAuditUser(String currentValue) {
        return (currentValue == null || currentValue.trim().isEmpty())
                ? SaleOrderApiConstants.DEFAULT_AUDIT_USER
                : currentValue.trim();
    }

    private void initializeNumericDefaults(SaleOrderMaster entity) {
        entity.setJobMasterRefId(defaultInteger(entity.getJobMasterRefId()));
        entity.setCoinage(defaultDouble(entity.getCoinage()));
        entity.setGrossAmount(defaultDouble(entity.getGrossAmount()));
        entity.setTaxAmount(defaultDouble(entity.getTaxAmount()));
        entity.setDiscountAmount(defaultDouble(entity.getDiscountAmount()));
        entity.setPlusAmount(defaultDouble(entity.getPlusAmount()));
        entity.setMinusAmount(defaultDouble(entity.getMinusAmount()));
        entity.setAmount(defaultDouble(entity.getAmount()));
        entity.setBoardingAmount(defaultDouble(entity.getBoardingAmount()));
        entity.setBoardingAmount1(defaultDouble(entity.getBoardingAmount1()));
        entity.setPortCharges(defaultDouble(entity.getPortCharges()));
        entity.setSealAmount(defaultDouble(entity.getSealAmount()));
        entity.setBreakSealAmount(defaultDouble(entity.getBreakSealAmount()));
        entity.setSealAmount2(defaultDouble(entity.getSealAmount2()));
        entity.setBreakSealAmount2(defaultDouble(entity.getBreakSealAmount2()));
        entity.setSealAmount3(defaultDouble(entity.getSealAmount3()));
        entity.setBreakSealAmount3(defaultDouble(entity.getBreakSealAmount3()));
        entity.setCurrencyValue(defaultDouble(entity.getCurrencyValue()));
        entity.setActualNetAmount(defaultDouble(entity.getActualNetAmount()));
        entity.setLBoardingAmount(defaultDouble(entity.getLBoardingAmount()));
        entity.setLBoardingAmount1(defaultDouble(entity.getLBoardingAmount1()));
        entity.setLPortCharges(defaultDouble(entity.getLPortCharges()));
        entity.setOBoardingAmount(defaultDouble(entity.getOBoardingAmount()));
        entity.setOBoardingAmount1(defaultDouble(entity.getOBoardingAmount1()));
        entity.setOPortCharges(defaultDouble(entity.getOPortCharges()));

        if (entity.getActive() == null) {
            entity.setActive(SaleOrderApiConstants.ACTIVE_STATUS);
        }
    }

    private Double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private Integer defaultInteger(Integer value) {
        return value != null ? value : 0;
    }

    private void validateCriticalIdentifiers(SaleOrderMaster entity) {
        if (entity.getCompanyRefId() == null || entity.getCompanyRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_COMPANY_REF_REQUIRED);
        }
        if (entity.getCustomerRefId() == null || entity.getCustomerRefId() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CUSTOMER_REF_REQUIRED);
        }
        if (entity.getCNumber() == null || entity.getCNumber() <= 0) {
            throw new InvalidRequestException(SaleOrderApiConstants.MESSAGE_CNUMBER_REQUIRED);
        }
    }

    private SaleOrderMasterDto calculateOrderTotals(SaleOrderMasterDto dto) {
        dto.setCoinage(defaultDouble(dto.getCoinage()));
        dto.setGrossAmount(defaultDouble(dto.getGrossAmount()));
        dto.setTaxAmount(defaultDouble(dto.getTaxAmount()));
        dto.setDiscountAmount(defaultDouble(dto.getDiscountAmount()));
        dto.setPlusAmount(defaultDouble(dto.getPlusAmount()));
        dto.setMinusAmount(defaultDouble(dto.getMinusAmount()));

        double totalAmount = dto.getGrossAmount()
                + dto.getTaxAmount()
                - dto.getDiscountAmount()
                + dto.getPlusAmount()
                - dto.getMinusAmount()
                + dto.getCoinage();

        dto.setAmount(totalAmount);
        return dto;
    }

    private Specification<SaleOrderMaster> buildFilterSpecification(SaleOrderFilterDTO filter) {
        return SaleOrderSpecification.buildFilter(
                filter.getComid(),
                filter.getId(),
                filter.getJId(),
                filter.getEmployeeid(),
                filter.getDashboardStatus(),
                filter.getStatusList(),
                filter.getStatusid(),
                filter.getCompletestatusnotshow(),
                filter.getRemarks(),
                filter.getOffvesselname(),
                filter.getLoadingvesselname(),
                filter.getSearch(),
                filter.getInvoice(),
                filter.getEta(),
                filter.getEtaType(),
                filter.getFromdate(),
                filter.getTodate(),
                filter.getPickup(),
                filter.getInvoicecheck()
        );
    }

    private List<Integer> getFilteredOrderIds(Specification<SaleOrderMaster> specification) {
        return repository.findAll(specification)
                .stream()
                .map(SaleOrderMaster::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<SaleMasterViewModel> fetchAndMapSaleMasterData(Integer companyId, List<Integer> filteredOrderIds) {
        List<Object[]> rawData = repository.findSaleMasterRawDataWithJoinsByOrderIds(companyId, filteredOrderIds);
        List<SaleMasterViewModel> mappedData = queryResultMapper.mapSaleMasterRows(rawData);

        return mappedData.stream()
                .sorted(
                        Comparator.comparing(
                                (SaleMasterViewModel item) -> item.getDeta() != null && !item.getDeta().isEmpty()
                                        ? item.getDeta()
                                        : "01/01/1900"
                        ).thenComparing(item -> item.getBillDate() != null ? item.getBillDate() : "")
                )
                .collect(Collectors.toList());
    }

    private List<SaleDetailsViewModel> fetchAndMapSaleDetailsData(Integer companyId, List<Integer> filteredOrderIds) {
        List<Object[]> rawData = repository.findSaleDetailsRawDataWithJoinsByOrderIds(companyId, filteredOrderIds);
        return queryResultMapper.mapSaleDetailsRows(rawData);
    }

    private SaleF5View buildSaleF5ViewResponse(List<SaleMasterViewModel> saleMasterList,
                                               List<SaleDetailsViewModel> saleDetailsList) {
        return saleF5ViewMapper.createSaleF5View(saleMasterList, saleDetailsList);
    }

    private SaleF5View buildEmptySaleF5ViewResponse() {
        return saleF5ViewMapper.createSaleF5View(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Get customer job numbers for a given company and customer
     *
     * Equivalent to ASP.NET GetCustJobNo endpoint
     *
     * Business Logic:
     * 1. Filter by company (multi-tenancy) - required
     * 2. Filter by customer (if customerId != 0) - optional, 0 means all customers
     * 3. Exclude soft-deleted records (Active != 2)
     * 4. Filter by invoice number:
     *    - If invoiceNo = 0: returns jobs NOT YET INVOICED
     *    - If invoiceNo > 0: returns jobs for that specific invoice
     *
     * @param companyId Company ID (tenant identifier)
     * @param customerId Customer ID (0 means all customers)
     * @param invoiceNo Invoice number (0 means not yet invoiced)
     * @return List of job records with Id and billNoDisplay (CNumberDisplay)
     */
    @Override
    public List<JobNumberDto> getCustJobNumbers(Integer companyId, Integer customerId, Integer invoiceNo) {
        try {
            logger.debug("Fetching customer job numbers - companyId: {}, customerId: {}, invoiceNo: {}",
                    companyId, customerId, invoiceNo);

            // Query repository with multi-tenancy and conditional filters
            List<JobNumberDto> jobs = repository.findCustJobNumbers(companyId, customerId, invoiceNo);

            logger.info("Successfully retrieved {} job numbers for companyId: {}",
                    jobs.size(), companyId);

            return jobs;

        } catch (Exception ex) {
            // Extract innermost exception (equivalent to ASP.NET pattern)
            Exception innermost = extractInnerException(ex);

            logger.error("Error retrieving customer job numbers for companyId: {}, customerId: {}",
                    companyId, customerId, innermost);

            // Re-throw to be handled by controller and global exception handler
            throw new InvalidRequestException("Error retrieving job numbers: " + innermost.getMessage(), innermost);
        }
    }

    /**
     * Extract innermost exception from exception chain
     * Equivalent to ASP.NET:
     * while (realerror.InnerException != null)
     *     realerror = realerror.InnerException;
     */
    private Exception extractInnerException(Exception ex) {
        Exception innermost = ex;
        while (innermost.getCause() != null && innermost.getCause() instanceof Exception) {
            innermost = (Exception) innermost.getCause();
        }
        return innermost;
    }
    @Override
    public List<my.maleva.api.module.saleorder.dto.SaleOrderDTO> editMultiSaleOrder(my.maleva.api.module.invoice.dto.MultiInvoiceDto dto) {
        if (dto == null || dto.getId() == null || dto.getId().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<SaleOrderMaster> masters = repository.findAllById(dto.getId());
        List<my.maleva.api.module.saleorder.dto.SaleOrderDTO> results = new ArrayList<>();
        boolean isMulti = dto.getId().size() > 1;
        
        for (SaleOrderMaster master : masters) {
            if (!Objects.equals(master.getCompanyRefId(), dto.getComid())) continue;
            
            SaleOrderMasterDto mapped = mapper.toDto(master);
            hydrateEditMasterDto(mapped, master, dto.getComid());
            
            my.maleva.api.module.saleorder.dto.SaleOrderDTO saleOrderDTO = mapper.toSaleOrderDto(mapped);
            
            List<SaleOrderEditDetailsDto> details = buildEditDetails(master.getId(), dto.getComid());
            List<SaleOrderDetailsDto> finalDetails = new ArrayList<>();
            
            for (SaleOrderEditDetailsDto detail : details) {
                String sdRemarks = detail.getSdRemarks() != null && !detail.getSdRemarks().trim().isEmpty() 
                        ? detail.getSdRemarks() 
                        : detail.getProductName();
                        
                if (isMulti) {
                    detail.setSdRemarks(sdRemarks + " - " + mapped.getCNumberDisplay());
                } else {
                    detail.setSdRemarks(sdRemarks);
                }
                
                detail.setSaleJobNo(mapped.getCNumberDisplay());
                finalDetails.add(detail);
            }
            
            saleOrderDTO.setSaleOrderDetails(finalDetails);
            
            saleOrderDTO.setPickupDetails(saleOrderPickupRepository.findBySaleOrderMasterRefId(master.getId()).stream()
                          .map(mapper::toPickupDetailDTO)
                          .collect(Collectors.toList()));
            saleOrderDTO.setDeliveryDetails(saleOrderDeliveryRepository.findBySaleOrderMasterRefId(master.getId()).stream()
                          .map(mapper::toDeliveryDetailDTO)
                          .collect(Collectors.toList()));
            saleOrderDTO.setForwardingDetails(saleOrderForwardingRepository.findBySaleOrderMasterRefId(master.getId()).stream()
                          .map(mapper::toForwardingDetailDTO)
                          .collect(Collectors.toList()));
            
            results.add(saleOrderDTO);
        }
        
        if (results.isEmpty()) {
            throw new EntityNotFoundException("Invaild Saleorder No !!!.");
        }
        
        return results;
    }



    @Override
    @Transactional(readOnly = true)
    public Integer selectPortChargeCount(int companyId, int jobId) {

        // Prefer primitives and Integer.valueOf to avoid deprecated constructor usage
        Integer response = Integer.valueOf(0);

        try {
            /*
             * Delegate the entire query to the repository instance. The repository
             * provides a native query that performs a COUNT at the database level.
             */
            int count = repository.countPendingPortCharges(companyId, jobId);
            response = Integer.valueOf(count);

        } catch (Exception ex) {
            String errorMessage = (ex.getCause() != null)
                    ? ex.getCause().getMessage()
                    : ex.getMessage();

            logger.error("[PortCharge] selectPortChargeCount failed | companyId={} jobId={} | error={}",
                    companyId, jobId, errorMessage, ex);

            response = Integer.valueOf(0);
        }

        return response;
    }

    private void checkPendingPortCharges(SaleOrderMaster entity, Integer targetStatusId, String statusName) {
        if (entity == null || entity.getId() == null || entity.getId() <= 0) {
            return;
        }
        String statusNameClean = statusName != null ? statusName.trim().toUpperCase() : "";
        if (targetStatusId == 15 || "WAITING FOR BILLING".equals(statusNameClean) ||
            targetStatusId == 20 || "WAITING FOR POD".equals(statusNameClean)) {
            Integer count = selectPortChargeCount(entity.getCompanyRefId(), entity.getId());
            if (count != null && count > 0) {
                throw new InvalidRequestException("Please complete all purchase orders before changing status!");
            }
        }
    }

    /**
     * AUDIT LOGGING: Track if address fields are deleted or changed during updates
     * This helps identify when pickup/delivery addresses are unexpectedly lost
     * Logs appear in application.log with "ADDRESS_AUDIT" prefix for easy searching
     * 
     * @param jobNumber Sale order job number (CNumberDisplay) for identification
     * @param originalPickupAddress Original pickup address before update
     * @param originalDeliveryAddress Original delivery address before update
     * @param originalOrigin Original origin before update
     * @param originalDestination Original destination before update
     * @param updatedEntity The entity after updates applied
     */
    private void auditAddressChanges(String jobNumber, String originalPickupAddress, 
                                     String originalDeliveryAddress, String originalOrigin, 
                                     String originalDestination, SaleOrderMaster updatedEntity) {
        try {
            boolean pickupAddressDeleted = (originalPickupAddress != null && !originalPickupAddress.trim().isEmpty())
                    && (updatedEntity.getPickupAddress() == null || updatedEntity.getPickupAddress().trim().isEmpty());
            
            boolean deliveryAddressDeleted = (originalDeliveryAddress != null && !originalDeliveryAddress.trim().isEmpty())
                    && (updatedEntity.getDeliveryAddress() == null || updatedEntity.getDeliveryAddress().trim().isEmpty());
            
            boolean originDeleted = (originalOrigin != null && !originalOrigin.trim().isEmpty())
                    && (updatedEntity.getOrigin() == null || updatedEntity.getOrigin().trim().isEmpty());
            
            boolean destinationDeleted = (originalDestination != null && !originalDestination.trim().isEmpty())
                    && (updatedEntity.getDestination() == null || updatedEntity.getDestination().trim().isEmpty());
            
            if (pickupAddressDeleted) {
                logger.warn("ADDRESS_AUDIT: Pickup Address DELETED - Job: {}, Original: {}, ID: {}",
                        jobNumber, originalPickupAddress, updatedEntity.getId());
            }
            
            if (deliveryAddressDeleted) {
                logger.warn("ADDRESS_AUDIT: Delivery Address DELETED - Job: {}, Original: {}, ID: {}",
                        jobNumber, originalDeliveryAddress, updatedEntity.getId());
            }
            
            if (originDeleted) {
                logger.warn("ADDRESS_AUDIT: Origin DELETED - Job: {}, Original: {}, ID: {}",
                        jobNumber, originalOrigin, updatedEntity.getId());
            }
            
            if (destinationDeleted) {
                logger.warn("ADDRESS_AUDIT: Destination DELETED - Job: {}, Original: {}, ID: {}",
                        jobNumber, originalDestination, updatedEntity.getId());
            }
            
            // Log when addresses are modified (not just deleted)
            if (originalPickupAddress != null && !originalPickupAddress.equals(updatedEntity.getPickupAddress())) {
                if (!pickupAddressDeleted) {
                    logger.info("ADDRESS_AUDIT: Pickup Address CHANGED - Job: {}, From: {} | To: {}, ID: {}",
                            jobNumber, originalPickupAddress, updatedEntity.getPickupAddress(), updatedEntity.getId());
                }
            }
            
            if (originalDeliveryAddress != null && !originalDeliveryAddress.equals(updatedEntity.getDeliveryAddress())) {
                if (!deliveryAddressDeleted) {
                    logger.info("ADDRESS_AUDIT: Delivery Address CHANGED - Job: {}, From: {} | To: {}, ID: {}",
                            jobNumber, originalDeliveryAddress, updatedEntity.getDeliveryAddress(), updatedEntity.getId());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in address audit logging for job: {}", jobNumber, e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> getSaleJobView(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return java.util.Collections.emptyList();
        }

        Integer remarks = filter.getRemarks() != null ? filter.getRemarks() : 0;
        Integer comId = filter.getComid() != null ? filter.getComid() : 0;
        Integer empId = filter.getEmployeeid() != null ? filter.getEmployeeid() : 0;
        
        String fromDate = filter.getFromdate() != null ? filter.getFromdate().toString() : "1900-01-01";
        String toDate = filter.getTodate() != null ? filter.getTodate().toString() : "2099-12-31";

        List<my.maleva.api.module.saleorder.repository.SaleJobViewProjection> results;

        switch (remarks) {
            case 1:
                results = repository.getJobViewByCurrency(comId, empId, fromDate, toDate);
                break;
            case 2:
                results = repository.getJobViewByEmployee(comId, empId, fromDate, toDate);
                break;
            case 3:
                results = repository.getJobViewByJobType(comId, empId, fromDate, toDate);
                break;
            case 4:
                results = repository.getJobViewByJobStatus(comId, empId, fromDate, toDate);
                break;
            default:
                return java.util.Collections.emptyList();
        }

        return results.stream().map(proj -> 
            my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto.builder()
                .currencyName(proj.getCurrencyName())
                .countryName(proj.getCountryName())
                .jobCount(proj.getJobCount())
                .employeeName(proj.getEmployeeName())
                .employeeCount(proj.getEmployeeCount())
                .jobType(proj.getJobType())
                .typeCount(proj.getTypeCount())
                .jobStatus(proj.getJobStatus())
                .statusCount(proj.getStatusCount())
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> getSaleCurrencyView(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return java.util.Collections.emptyList();
        }

        Integer comId = filter.getComid() != null ? filter.getComid() : 0;
        Integer empId = filter.getEmployeeid() != null ? filter.getEmployeeid() : 0;
        Integer tId = filter.getTId() != null ? filter.getTId() : 0;
        
        String fromDate = filter.getFromdate() != null ? filter.getFromdate().toString() : "1900-01-01";

        List<my.maleva.api.module.saleorder.repository.SaleJobViewProjection> results = repository.getSaleCurrencyView(comId, empId, tId, fromDate);

        return results.stream().map(proj -> 
            my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto.builder()
                .customerName(proj.getCustomerName())
                .currencyName(proj.getCurrencyName())
                .month1(proj.getMonth1() != null ? String.valueOf(proj.getMonth1()) : "0")
                .month2(proj.getMonth2() != null ? String.valueOf(proj.getMonth2()) : "0")
                .month3(proj.getMonth3() != null ? String.valueOf(proj.getMonth3()) : "0")
                .currentMonth(proj.getCurrentMonth() != null ? String.valueOf(proj.getCurrentMonth()) : "0")
                .amount(proj.getAmount() != null ? proj.getAmount().intValue() : 0)
                .companyRefId(proj.getCompanyRefId() != null ? proj.getCompanyRefId() : 0)
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }
    @Override
    public List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> getSaleEmployeeView(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return java.util.Collections.emptyList();
        }

        Integer comId = filter.getComid() != null ? filter.getComid() : 0;
        Integer empId = filter.getEmployeeid() != null ? filter.getEmployeeid() : 0;
        Integer tId = filter.getTId() != null ? filter.getTId() : 0;

        List<my.maleva.api.module.saleorder.repository.SaleJobViewProjection> results = repository.getSaleEmployeeView(comId, empId, tId);

        return results.stream().map(proj -> 
            my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto.builder()
                .customerName(proj.getCustomerName())
                .countryName(proj.getCountryName())
                .month1(proj.getMonth1() != null ? String.valueOf(proj.getMonth1()) : "0")
                .month2(proj.getMonth2() != null ? String.valueOf(proj.getMonth2()) : "0")
                .month3(proj.getMonth3() != null ? String.valueOf(proj.getMonth3()) : "0")
                .currentMonth(proj.getCurrentMonth() != null ? String.valueOf(proj.getCurrentMonth()) : "0")
                .amount(proj.getAmount() != null ? proj.getAmount().intValue() : 0)
                .companyRefId(proj.getCompanyRefId() != null ? proj.getCompanyRefId() : 0)
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getSalePortView(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return java.util.Collections.emptyList();
        }

        if (filter.getRemarks() == null || filter.getRemarks() != 5) {
            throw new InvalidRequestException("Invalid Remarks value");
        }

        Integer comId = filter.getComid() != null ? filter.getComid() : 0;
        Integer empId = filter.getEmployeeid() != null ? filter.getEmployeeid() : 0;
        String portName = filter.getPortName();

        java.time.LocalDate toDate = filter.getTodate() != null ? filter.getTodate() : java.time.LocalDate.now();
        java.time.LocalDate fromDate = toDate.withDayOfMonth(1).minusMonths(3);

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String fromDateStr = fromDate.format(dtf);
        String toDateStr = toDate.format(dtf);

        List<my.maleva.api.module.saleorder.repository.SalePortViewProjection> results = repository.getSalePortViewRaw(
                comId, empId, portName, fromDateStr, toDateStr);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.ENGLISH);
        List<String> monthList = results.stream()
                .map(my.maleva.api.module.saleorder.repository.SalePortViewProjection::getSaleMonth)
                .distinct()
                .sorted((m1, m2) -> {
                    try {
                        return java.time.YearMonth.parse(m1, formatter).compareTo(java.time.YearMonth.parse(m2, formatter));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        Map<String, List<my.maleva.api.module.saleorder.repository.SalePortViewProjection>> grouped = results.stream()
                .collect(Collectors.groupingBy(my.maleva.api.module.saleorder.repository.SalePortViewProjection::getPortName));

        List<Map<String, Object>> pivotedList = new ArrayList<>();
        for (Map.Entry<String, List<my.maleva.api.module.saleorder.repository.SalePortViewProjection>> entry : grouped.entrySet()) {
            Map<String, Object> dict = new java.util.LinkedHashMap<>();
            dict.put("PortName", entry.getKey());
            for (String month : monthList) {
                my.maleva.api.module.saleorder.repository.SalePortViewProjection data = entry.getValue().stream()
                        .filter(x -> month.equals(x.getSaleMonth()))
                        .findFirst()
                        .orElse(null);
                dict.put(month + "_Count", data != null ? data.getJobCount() : 0);
                dict.put(month + "_Amount", data != null ? data.getTotalAmount() : 0.0);
            }
            pivotedList.add(dict);
        }

        return pivotedList;
    }
    @Override
    public List<my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto> getSaleCustomerView(SaleOrderFilterDTO filter) {
        if (filter == null) {
            return java.util.Collections.emptyList();
        }

        Integer comId = filter.getComid() != null ? filter.getComid() : 0;
        Integer empId = filter.getEmployeeid() != null ? filter.getEmployeeid() : 0;
        Integer tId = filter.getTId() != null ? filter.getTId() : 0;

        List<my.maleva.api.module.saleorder.repository.SaleJobViewProjection> results = repository.getSaleCustomerView(comId, empId, tId);

        return results.stream().map(proj -> 
            my.maleva.api.module.saleorder.dto.SaleJobViewAggregateDto.builder()
                .customerName(proj.getCustomerName())
                .month1(proj.getMonth1() != null ? String.valueOf(proj.getMonth1()) : "0")
                .month2(proj.getMonth2() != null ? String.valueOf(proj.getMonth2()) : "0")
                .month3(proj.getMonth3() != null ? String.valueOf(proj.getMonth3()) : "0")
                .currentMonth(proj.getCurrentMonth() != null ? String.valueOf(proj.getCurrentMonth()) : "0")
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }


    @Override
    public List<VesselScheduleResponseDto> getVesselSchedules(Integer companyId, String fromDate, String toDate) {
        logger.info("Fetching Vessel Schedules for company: {} from {} to {}", companyId, fromDate, toDate);

        // 1. Business Logic / Validation
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID is required and must be valid.");
        }

        // 2. Fetch from database
        List<VesselScheduleDto> schedules = repository.getVesselSchedules(companyId, fromDate, toDate);

        logger.info("Successfully fetched {} vessel schedules", schedules.size());
        
        // 3. Map interface projection to concrete DTO for robust JSON serialization
        return schedules.stream().map(s -> VesselScheduleResponseDto.builder()
                .etaDate(s.getEtaDate() != null ? s.getEtaDate().toString() : null)
                .vesselName(s.getVesselName())
                .vesselType(s.getVesselType())
                .jobNumbers(s.getJobNumbers())
                .totalJob(s.getTotalJob())
                .officer1Name(s.getOfficer1Name())
                .officer1Amount(s.getOfficer1Amount())
                .officer2Name(s.getOfficer2Name())
                .officer2Amount(s.getOfficer2Amount())
                .officer3Name(s.getOfficer3Name())
                .officer3Amount(s.getOfficer3Amount())
                .officer4Name(s.getOfficer4Name())
                .officer4Amount(s.getOfficer4Amount())
                .officer5Name(s.getOfficer5Name())
                .officer5Amount(s.getOfficer5Amount())
                .totalAmount(s.getTotalAmount())
                .build()).collect(java.util.stream.Collectors.toList());
    }
    @Override
    @Transactional
    public SaleOrderMasterDto updateDoDescription(SaleOrderDoDescriptionUpdateDto dto) {
        // Validate DTO is not null
        if (dto == null) {
            logger.error("DODescription update request received with null DTO");
            throw new InvalidRequestException("Request body cannot be null");
        }

        Integer id = dto.getId();
        String newDescription = dto.getDoDescription();

        // CRITICAL: Validate ID is present and positive - NO UPDATE without valid ID
        if (id == null) {
            logger.error("DODescription update attempted WITHOUT ID - REJECTED");
            throw new InvalidRequestException("SaleOrder ID is mandatory and cannot be null");
        }

        if (id <= 0) {
            logger.error("DODescription update attempted with INVALID ID: {} - REJECTED", id);
            throw new InvalidRequestException("SaleOrder ID must be a positive number (greater than 0), provided: " + id);
        }

        // Validate description is not empty
        if (newDescription == null || newDescription.trim().isEmpty()) {
            logger.error("DODescription update attempted with EMPTY/NULL description for ID: {} - REJECTED", id);
            throw new InvalidRequestException("DODescription cannot be empty or blank");
        }

        logger.debug("Initiating DODescription update - SaleOrder ID: {} with description length: {}", id, newDescription.length());

        // Verify entity exists in database - throw 404 if not found
        SaleOrderMaster entity = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("SaleOrderMaster NOT FOUND in database - ID: {}", id);
                    return new EntityNotFoundException("SaleOrderMaster not found with id: " + id);
                });

        logger.debug("SaleOrderMaster found - ID: {}, Current Status: READY FOR UPDATE", id);

        // Capture previous value for audit logging
        String previousDescription = entity.getDoDescription();

        // Check if value has actually changed to avoid unnecessary updates
        if (Objects.equals(previousDescription, newDescription)) {
            logger.info("NO CHANGE DETECTED - DODescription update skipped - SaleOrder ID: {}. Current value: '{}'", id, previousDescription);
            return mapper.toDto(entity);
        }

        // Update the entity property with new value
        entity.setDoDescription(newDescription);

        // Persist the change (proper JPA methodology)
        SaleOrderMaster updatedEntity = repository.save(entity);

        // Log the successful update with before/after values for audit trail
        logger.info("✓ DODescription updated SUCCESSFULLY - SaleOrder ID: {}. Previous: '{}' → New: '{}'",
                id, previousDescription, newDescription);

        // Return the updated entity as DTO for API response
        return mapper.toDto(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto> checkSaleOrderInvoice(my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest request) {
        if (request.getComid() == null) {
            throw new my.maleva.api.common.exception.InvalidRequestException("Company ID is required");
        }
        return repository.checkSaleOrderInvoice(request);
    }
    @Override
    @Transactional(readOnly = true)
    public List<VesselActivityReportProjection> getVesselActivityReport(Integer companyId, String fromDate, String toDate, String portName) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID is required");
        }
        return repository.getVesselActivityReport(companyId, fromDate, toDate, portName);
    }
    @Override
    public List<SaleOrderMasterDto> getJobsWithMissingRemarks(Integer companyId, String fromDateStr, String toDateStr, Integer customerRefId, Integer referenceStatus) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company ID is required and must be positive.");
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime fromDate;
        LocalDateTime toDate;
        
        try {
            // Parse start date, defaults to start of day
            fromDate = java.time.LocalDate.parse(fromDateStr, formatter).atStartOfDay();
            
            // Parse end date, ADD 1 DAY as requested by the SQL query `DATEADD(DAY, 1, @ToDate)`
            toDate = java.time.LocalDate.parse(toDateStr, formatter).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Dates must be in yyyy-MM-dd format.");
        }
        
        List<Object[]> results = repository.findJobsWithMissingRemarks(companyId, fromDate, toDate, customerRefId, referenceStatus);
        return results.stream()
                .map(obj -> {
                    SaleOrderMaster master = (SaleOrderMaster) obj[0];
                    String customerName = (String) obj[1];
                    SaleOrderMasterDto dto = mapper.toDto(master);
                    dto.setCustomerName(customerName);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}

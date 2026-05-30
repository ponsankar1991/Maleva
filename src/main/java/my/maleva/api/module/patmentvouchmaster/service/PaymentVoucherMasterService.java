package my.maleva.api.module.patmentvouchmaster.service;

import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherComboDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherComboResponse;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.patmentvouchmaster.mapper.PaymentVoucherMasterMapper;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentVoucherMasterService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVoucherMasterService.class);

    private final PaymentVoucherMasterRepository repository;
    private final PaymentVoucherMasterMapper mapper;

    public PaymentVoucherMasterService(PaymentVoucherMasterRepository repository, PaymentVoucherMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentVoucherMasterDto> listAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public PaymentVoucherMasterDto getById(Integer id) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        return mapper.toDto(ent);
    }

    @Transactional
    public PaymentVoucherMasterDto create(PaymentVoucherMasterDto dto) {
        LocalDateTime now = LocalDateTime.now();
        PaymentVoucherMaster ent = mapper.toEntity(dto);
        ent.setCreatedDate(now);
        ent.setModifiedDate(now);
        PaymentVoucherMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentVoucherMasterDto update(Integer id, PaymentVoucherMasterDto dto) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        mapper.updateFromDto(dto, ent);
        ent.setModifiedDate(LocalDateTime.now());
        PaymentVoucherMaster saved = repository.save(ent);
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Integer id) {
        PaymentVoucherMaster ent = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("PaymentVoucherMaster not found: " + id));
        repository.delete(ent);
    }

    /**
     * SelectPaymentTo - Get distinct PayTo values for a company
     * Equivalent to .NET SelectPaymentTo method
     *
     * @param companyRefId Company reference ID
     * @return PaymentVoucherComboResponse with distinct PayTo values
     */
    @Transactional(readOnly = true)
    public PaymentVoucherComboResponse selectPaymentTo(Integer companyRefId) {
        logger.info("SelectPaymentTo request received - companyRefId: {}", companyRefId);

        try {
            // Validate input
            if (companyRefId == null || companyRefId <= 0) {
                logger.warn("Invalid companyRefId: {}", companyRefId);
                return PaymentVoucherComboResponse.error("Company ID must be a positive number");
            }

            // Fetch distinct PayTo values from repository
            List<String> payToValues = repository.findDistinctPayToByCompanyId(companyRefId);
            logger.info("Retrieved {} distinct PayTo values for company: {}", payToValues.size(), companyRefId);

            // Convert strings to DTOs
            List<PaymentVoucherComboDto> comboDtos = payToValues.stream()
                    .map(payTo -> PaymentVoucherComboDto.builder()
                            .accountName(payTo)
                            .build())
                    .collect(Collectors.toList());

            logger.info("Successfully converted to {} PaymentVoucherComboDto objects", comboDtos.size());

            // Return success response
            return PaymentVoucherComboResponse.success(comboDtos);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentTo for companyRefId: {}", companyRefId, e);
            return PaymentVoucherComboResponse.error("Error retrieving PayTo values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    /**
     * SelectPaymentFrom - Get distinct PayFrom values for a company
     * Equivalent to .NET SelectPaymentFrom method
     *
     * @param companyRefId Company reference ID
     * @return PaymentVoucherComboResponse with distinct PayFrom values
     */
    @Transactional(readOnly = true)
    public PaymentVoucherComboResponse selectPaymentFrom(Integer companyRefId) {
        logger.info("SelectPaymentFrom request received - companyRefId: {}", companyRefId);

        try {
            // Validate input
            if (companyRefId == null || companyRefId <= 0) {
                logger.warn("Invalid companyRefId: {}", companyRefId);
                return PaymentVoucherComboResponse.error("Company ID must be a positive number");
            }

            // Fetch distinct PayFrom values from repository
            List<String> payFromValues = repository.findDistinctPayFromByCompanyId(companyRefId);
            logger.info("Retrieved {} distinct PayFrom values for company: {}", payFromValues.size(), companyRefId);

            // Convert strings to DTOs
            List<PaymentVoucherComboDto> comboDtos = payFromValues.stream()
                    .map(payFrom -> PaymentVoucherComboDto.builder()
                            .accountName(payFrom)
                            .build())
                    .collect(Collectors.toList());

            logger.info("Successfully converted to {} PaymentVoucherComboDto objects", comboDtos.size());

            // Return success response
            return PaymentVoucherComboResponse.success(comboDtos);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentFrom for companyRefId: {}", companyRefId, e);
            return PaymentVoucherComboResponse.error("Error retrieving PayFrom values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }
}

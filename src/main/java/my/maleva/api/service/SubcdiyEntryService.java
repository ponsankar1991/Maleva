package my.maleva.api.service;

import my.maleva.api.dto.SubcdiyEntryDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SubcdiyEntryService - Business logic for SubcdiyEntry
 * Handles subsidy entry processing and management
 */
public interface SubcdiyEntryService {

    List<SubcdiyEntryDto> getByActive(Integer active);

    List<SubcdiyEntryDto> getByEntryDate(LocalDate entryDate);

    List<SubcdiyEntryDto> getByEntryDateRange(LocalDate startDate, LocalDate endDate);

    List<SubcdiyEntryDto> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

    List<SubcdiyEntryDto> getByCreatedDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Optional<SubcdiyEntryDto> getById(Integer id);

    SubcdiyEntryDto create(SubcdiyEntryDto dto);

    SubcdiyEntryDto update(Integer id, SubcdiyEntryDto dto);

    boolean delete(Integer id);

    long countByActive(Integer active);

    boolean existsByEntryDate(LocalDate entryDate);

    void validateSubcdiyEntryData(SubcdiyEntryDto dto);

    SubcdiyEntryDto activateEntry(Integer id);

    SubcdiyEntryDto deactivateEntry(Integer id);

    List<SubcdiyEntryDto> getAllActive();
}


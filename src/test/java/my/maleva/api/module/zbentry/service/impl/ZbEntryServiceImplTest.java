package my.maleva.api.module.zbentry.service.impl;

import my.maleva.api.common.exception.InvalidDateRangeException;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import my.maleva.api.module.zbentry.entity.ZbEntry;
import my.maleva.api.module.zbentry.mapper.ZbEntryMapper;
import my.maleva.api.module.zbentry.repository.ZbEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZbEntryServiceImplTest {

    @Mock
    private ZbEntryRepository zbEntryRepository;

    @Mock
    private ZbEntryMapper zbEntryMapper;

    @InjectMocks
    private ZbEntryServiceImpl zbEntryService;

    @Test
    void searchZbEntries_HappyPath_ReturnsData() {
        // Arrange
        ZbEntrySearchRequest request = ZbEntrySearchRequest.builder()
                .companyRefId(1)
                .chargeType("TYPE_A")
                .keyword("test")
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 1, 31))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        ZbEntry entity = new ZbEntry();
        entity.setId(100);
        entity.setEntryDate("15/01/2026");
        entity.setAmount("500.50");

        ZbEntryResponse responseDto = new ZbEntryResponse();
        responseDto.setId(100);
        responseDto.setEntryDate(LocalDate.of(2026, 1, 15));
        responseDto.setAmount(new BigDecimal("500.50"));

        Page<ZbEntry> entityPage = new PageImpl<>(List.of(entity));

        when(zbEntryRepository.searchZbEntries(1, "TYPE_A", "test",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), pageable))
                .thenReturn(entityPage);
        when(zbEntryMapper.toDto(entity)).thenReturn(responseDto);

        // Act
        Page<ZbEntryResponse> result = zbEntryService.searchZbEntries(request, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("500.50"), result.getContent().get(0).getAmount());
        verify(zbEntryRepository, times(1)).searchZbEntries(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchZbEntries_EmptyResult_ReturnsEmptyPageWithoutError() {
        // Arrange
        ZbEntrySearchRequest request = ZbEntrySearchRequest.builder()
                .companyRefId(1)
                .fromDate(LocalDate.of(2026, 1, 1))
                .toDate(LocalDate.of(2026, 1, 31))
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<ZbEntry> entityPage = new PageImpl<>(Collections.emptyList());

        when(zbEntryRepository.searchZbEntries(eq(1), isNull(), isNull(),
                eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31)), eq(pageable)))
                .thenReturn(entityPage);

        // Act
        Page<ZbEntryResponse> result = zbEntryService.searchZbEntries(request, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void searchZbEntries_InvalidDateRange_ThrowsException() {
        // Arrange
        ZbEntrySearchRequest request = ZbEntrySearchRequest.builder()
                .companyRefId(1)
                .fromDate(LocalDate.of(2026, 2, 1))
                .toDate(LocalDate.of(2026, 1, 31)) // from > to
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        InvalidDateRangeException ex = assertThrows(InvalidDateRangeException.class, () -> {
            zbEntryService.searchZbEntries(request, pageable);
        });

        assertEquals("FromDate must be less than or equal to ToDate", ex.getMessage());
        verify(zbEntryRepository, never()).searchZbEntries(any(), any(), any(), any(), any(), any());
    }
}

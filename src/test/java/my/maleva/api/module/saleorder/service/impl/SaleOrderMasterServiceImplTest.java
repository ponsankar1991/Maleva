package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import my.maleva.api.module.saleorder.dto.SaleOrderRemarksUpdateDto;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.mapper.SaleOrderMasterMapper;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleOrderMasterServiceImplTest {

    @Mock
    private SaleOrderMasterRepository repository;

    @Mock
    private SaleOrderMasterMapper mapper;

    @InjectMocks
    private SaleOrderMasterServiceImpl service;

    private SaleOrderMaster entity;

    @BeforeEach
    void setUp() {
        entity = new SaleOrderMaster();
        entity.setId(1);
        entity.setCompanyRefId(6);
        entity.setCustomerRefId(144);
        entity.setRemarks("Initial Remarks");
        entity.setActive(1);
    }

    @Test
    void updateRemarks_Success() {
        // Arrange
        SaleOrderRemarksUpdateDto request = new SaleOrderRemarksUpdateDto();
        request.setId(1);
        request.setCompanyRefId(6);
        request.setRemarks("Updated Remarks");
        
        when(repository.findByIdAndCompanyRefId(1, 6)).thenReturn(Optional.of(entity));
        when(repository.save(any(SaleOrderMaster.class))).thenReturn(entity);

        // Act
        service.updateRemarks(request);

        // Assert
        assertEquals("Updated Remarks", entity.getRemarks());
        verify(repository, times(1)).findByIdAndCompanyRefId(1, 6);
        verify(repository, times(1)).save(entity);
    }


    @Test
    void updateRemarksBulk_Success() {
        // Arrange
        SaleOrderRemarksUpdateDto req1 = new SaleOrderRemarksUpdateDto();
        req1.setId(1);
        req1.setCompanyRefId(6);
        req1.setRemarks("Remark 1");

        SaleOrderRemarksUpdateDto req2 = new SaleOrderRemarksUpdateDto();
        req2.setId(2);
        req2.setCompanyRefId(6);
        req2.setRemarks("Remark 2");
        
        SaleOrderMaster entity2 = new SaleOrderMaster();
        entity2.setId(2);
        entity2.setCompanyRefId(6);

        when(repository.findByIdAndCompanyRefId(1, 6)).thenReturn(Optional.of(entity));
        when(repository.findByIdAndCompanyRefId(2, 6)).thenReturn(Optional.of(entity2));
        
        // Act
        service.updateRemarksBulk(java.util.Arrays.asList(req1, req2));

        // Assert
        assertEquals("Remark 1", entity.getRemarks());
        assertEquals("Remark 2", entity2.getRemarks());
        verify(repository, times(1)).findByIdAndCompanyRefId(1, 6);
        verify(repository, times(1)).findByIdAndCompanyRefId(2, 6);
        verify(repository, times(1)).save(entity);
        verify(repository, times(1)).save(entity2);
    }

    @Test
    void updateRemarks_NotFound_ThrowsEntityNotFoundException() {
        // Arrange
        SaleOrderRemarksUpdateDto request = new SaleOrderRemarksUpdateDto();
        request.setId(99);
        request.setCompanyRefId(6);
        request.setRemarks("Updated Remarks");
        
        when(repository.findByIdAndCompanyRefId(99, 6)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> service.updateRemarks(request));
        verify(repository, times(1)).findByIdAndCompanyRefId(99, 6);
        verify(repository, never()).save(any(SaleOrderMaster.class));
    }

    @Test
    void getJobsWithMissingRemarks_Success() {
        // Arrange
        LocalDateTime fromDate = LocalDateTime.of(2026, 6, 29, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 7, 1, 0, 0);
        
        Object[] record = new Object[2];
        record[0] = entity;
        record[1] = "Test Customer";
        
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(record);
        
        when(repository.findJobsWithMissingRemarks(eq(6), eq(fromDate), eq(toDate), eq(144), eq(2)))
                .thenReturn(queryResult);
                
        SaleOrderMasterDto dto = new SaleOrderMasterDto();
        dto.setId(1);
        dto.setRemarks("Initial Remarks");
        when(mapper.toDto(any(SaleOrderMaster.class))).thenReturn(dto);

        // Act
        List<SaleOrderMasterDto> result = service.getJobsWithMissingRemarks(6, "2026-06-29", "2026-06-30", 144, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Test Customer", result.get(0).getCustomerName());
        assertEquals("Initial Remarks", result.get(0).getRemarks());
        
        verify(repository, times(1)).findJobsWithMissingRemarks(eq(6), eq(fromDate), eq(toDate), eq(144), eq(2));
    }

    @Test
    void getJobsWithMissingRemarks_EmptyResult_Success() {
        // Arrange
        LocalDateTime fromDate = LocalDateTime.of(2026, 6, 29, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 7, 1, 0, 0);
        
        when(repository.findJobsWithMissingRemarks(eq(6), eq(fromDate), eq(toDate), eq(144), eq(2)))
                .thenReturn(new ArrayList<>());

        // Act
        List<SaleOrderMasterDto> result = service.getJobsWithMissingRemarks(6, "2026-06-29", "2026-06-30", 144, 2);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(repository, times(1)).findJobsWithMissingRemarks(eq(6), eq(fromDate), eq(toDate), eq(144), eq(2));
    }
}

package my.maleva.api.module.rti.service.impl;

import my.maleva.api.common.exception.DateRangeTooLargeException;
import my.maleva.api.common.exception.InvalidDateRangeException;
import my.maleva.api.module.rti.dto.RtiJobWiseViewRequest;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import my.maleva.api.module.rti.repository.RtiJobWiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RtiJobWiseServiceImplTest {

    @Mock
    private RtiJobWiseRepository repository;

    private RtiJobWiseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RtiJobWiseServiceImpl(repository, 90);
    }

    @Test
    void getJobWiseView_ValidDates_ReturnsResults() {
        LocalDate fromDate = LocalDate.of(2026, 5, 2);
        LocalDate toDate = LocalDate.of(2026, 5, 10);
        RtiJobWiseViewRequest request = new RtiJobWiseViewRequest(fromDate, toDate);
        List<RtiJobWiseViewResponse> expectedResponse = Collections.singletonList(
                new RtiJobWiseViewResponse(1L, "RTI-001", "SO-001", "Vessel", "Cargo", "Origin", "Dest", fromDate, toDate, "Truck1", "Driver1", "Size1", 1, 1, "Remarks", 1)
        );

        when(repository.findJobWiseView(fromDate, toDate)).thenReturn(expectedResponse);

        List<RtiJobWiseViewResponse> actualResponse = service.getJobWiseView(request);

        assertEquals(expectedResponse, actualResponse);
        verify(repository, times(1)).findJobWiseView(fromDate, toDate);
    }

    @Test
    void getJobWiseView_FromDateAfterToDate_ThrowsInvalidDateRangeException() {
        LocalDate fromDate = LocalDate.of(2026, 5, 10);
        LocalDate toDate = LocalDate.of(2026, 5, 2);
        RtiJobWiseViewRequest request = new RtiJobWiseViewRequest(fromDate, toDate);

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> service.getJobWiseView(request)
        );

        assertEquals("fromDate cannot be after toDate.", exception.getMessage());
        verify(repository, never()).findJobWiseView(any(), any());
    }

    @Test
    void getJobWiseView_DateRangeExceedsLimit_ThrowsDateRangeTooLargeException() {
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 5, 1); // 120 days later
        RtiJobWiseViewRequest request = new RtiJobWiseViewRequest(fromDate, toDate);

        DateRangeTooLargeException exception = assertThrows(
                DateRangeTooLargeException.class,
                () -> service.getJobWiseView(request)
        );

        assertEquals("Date range exceeds the maximum allowed limit of 90 days.", exception.getMessage());
        verify(repository, never()).findJobWiseView(any(), any());
    }
}

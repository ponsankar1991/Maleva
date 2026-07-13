package my.maleva.api.module.rti.repository.impl;

import my.maleva.api.common.exception.RtiJobWiseQueryException;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import my.maleva.api.module.rti.mapper.RtiJobWiseRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RtiJobWiseRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private RtiJobWiseRowMapper rowMapper;

    private RtiJobWiseRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new RtiJobWiseRepositoryImpl(jdbcTemplate, rowMapper);
    }

    @Test
    void findJobWiseView_ExecutesQueryWithCorrectParameters() {
        LocalDate fromDate = LocalDate.of(2026, 5, 2);
        LocalDate toDate = LocalDate.of(2026, 5, 2);
        List<RtiJobWiseViewResponse> expected = Collections.singletonList(mock(RtiJobWiseViewResponse.class));

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(rowMapper))).thenReturn(expected);

        List<RtiJobWiseViewResponse> actual = repository.findJobWiseView(fromDate, toDate);

        assertEquals(expected, actual);

        ArgumentCaptor<MapSqlParameterSource> paramCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(anyString(), paramCaptor.capture(), eq(rowMapper));

        MapSqlParameterSource params = paramCaptor.getValue();
        assertEquals(java.sql.Date.valueOf(fromDate), params.getValue("fromDate"));
        assertEquals(java.sql.Date.valueOf(toDate), params.getValue("toDate"));
    }

    @Test
    void findJobWiseView_ThrowsCustomExceptionOnDataAccessException() {
        LocalDate fromDate = LocalDate.of(2026, 5, 2);
        LocalDate toDate = LocalDate.of(2026, 5, 2);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), eq(rowMapper)))
                .thenThrow(new DataRetrievalFailureException("DB Error"));

        RtiJobWiseQueryException exception = assertThrows(
                RtiJobWiseQueryException.class,
                () -> repository.findJobWiseView(fromDate, toDate)
        );

        assertEquals("Failed to execute RTI Job Wise View query.", exception.getMessage());
    }
}

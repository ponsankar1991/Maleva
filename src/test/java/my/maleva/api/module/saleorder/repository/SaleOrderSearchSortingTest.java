package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.invoice.dto.SaleMasterViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaleOrderSearchSortingTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private SaleOrderMasterRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(repository, "jdbcTemplate", jdbcTemplate);
    }

    @Test
    void findSaleMasterRows_SortsNewestSaleDateFirst() throws Exception {
        ResultSet rs1 = createMockResultSet(101, "2026-09-01T10:00:00", "2026-09-02T10:00:00");
        ResultSet rs2 = createMockResultSet(102, "2026-09-09T10:00:00", "2026-09-10T10:00:00");
        ResultSet rs3 = createMockResultSet(103, "2026-09-05T10:00:00", "2026-09-06T10:00:00");

        RowMapper mapper = (RowMapper) ReflectionTestUtils.getField(SaleOrderMasterRepositoryImpl.class, "MASTER_ROW_MAPPER");

        Object row1 = mapper.mapRow(rs1, 1);
        Object row2 = mapper.mapRow(rs2, 2);
        Object row3 = mapper.mapRow(rs3, 3);

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(inv -> List.of(row1, row2, row3));

        List<SaleMasterViewModel> result = repository.findSaleMasterRows(6, List.of(101, 102, 103));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(102);
        assertThat(result.get(1).getId()).isEqualTo(103);
        assertThat(result.get(2).getId()).isEqualTo(101);
    }

    private ResultSet createMockResultSet(int id, String saleDateIso, String detaIso) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("Id")).thenReturn(id);
        when(rs.getString("Remarks")).thenReturn("Remarks " + id);

        Timestamp saleDateTs = saleDateIso != null ? Timestamp.valueOf(LocalDateTime.parse(saleDateIso)) : null;
        Timestamp detaTs = detaIso != null ? Timestamp.valueOf(LocalDateTime.parse(detaIso)) : null;

        when(rs.getTimestamp("SaleDateSort")).thenReturn(saleDateTs);
        when(rs.getTimestamp("DETASort")).thenReturn(detaTs);

        return rs;
    }
}

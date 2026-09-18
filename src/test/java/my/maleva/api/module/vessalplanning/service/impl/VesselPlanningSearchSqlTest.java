package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The SQL Vessel Planning's search builds. Nothing runs against a database: the statement and its
 * parameters are captured and parsed the way NamedParameterJdbcTemplate parses them.
 */
class VesselPlanningSearchSqlTest {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private VesselPlanningMasterService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        service = new VesselPlanningMasterService(mock(VesselPlanningMasterRepository.class), jdbcTemplate);
    }

    private static VesselPlanningLegacyDtos.SearchRequest search() {
        VesselPlanningLegacyDtos.SearchRequest filter = new VesselPlanningLegacyDtos.SearchRequest();
        filter.setComid(6);
        filter.setFromdate("2026-09-01");
        filter.setTodate("2026-09-30");
        return filter;
    }

    private record Captured(String sql, MapSqlParameterSource params) {
    }

    @SuppressWarnings("unchecked")
    private Captured run(VesselPlanningLegacyDtos.SearchRequest filter) {
        service.vesselPlanningSearch(filter);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbcTemplate).query(sql.capture(), params.capture(), any(RowMapper.class));
        return new Captured(sql.getValue(), (MapSqlParameterSource) params.getValue());
    }

    /** Throws exactly like the real template when a :name in the SQL has no value. */
    private static void assertEveryParameterBound(Captured captured) {
        ParsedSql parsed = NamedParameterUtils.parseSqlStatement(captured.sql());
        assertThatCode(() -> NamedParameterUtils.buildValueArray(parsed, captured.params(), null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("employee filter together with Delivery Done builds valid SQL (no \"=:employeeIdAND\")")
    void employeeAndDeliveryDoneTogether() {
        VesselPlanningLegacyDtos.SearchRequest filter = search();
        filter.setEmployeeid(15);
        filter.setDeliveryDone(true);

        Captured captured = run(filter);

        assertThat(captured.sql()).doesNotContain("employeeIdAND")
                .contains("=:employeeId AND (J.Name IS NULL OR J.Name NOT IN (:finishedStatuses))");
        assertThat(captured.params().getValue("finishedStatuses"))
                .isEqualTo(VesselPlanningMasterService.FINISHED_JOB_STATUSES);
        assertEveryParameterBound(captured);
    }

    @Test
    @DisplayName("Delivery Done hides finished jobs but keeps jobs that have no status yet")
    void deliveryDoneKeepsJobsWithoutStatus() {
        VesselPlanningLegacyDtos.SearchRequest filter = search();
        filter.setDeliveryDone(true);

        Captured captured = run(filter);

        assertThat(captured.sql()).contains("J.Name IS NULL OR J.Name NOT IN (:finishedStatuses)");
        assertEveryParameterBound(captured);
    }

    @Test
    @DisplayName("unticked, no status filter is added")
    void noDeliveryDoneFilter() {
        Captured captured = run(search());

        assertThat(captured.sql()).doesNotContain("finishedStatuses").doesNotContain("J.Name NOT IN");
        assertEveryParameterBound(captured);
    }

    @Test
    @DisplayName("OETA and ETA searches sort by the date they filter on")
    void sortsByChosenEtaType() {
        VesselPlanningLegacyDtos.SearchRequest offloading = search();
        offloading.setEtaType(1);
        assertThat(run(offloading).sql()).contains("ORDER BY CASE WHEN S.OETA IS NULL");

        setUp();
        VesselPlanningLegacyDtos.SearchRequest loading = search();
        loading.setEtaType(2);
        Captured captured = run(loading);
        assertThat(captured.sql()).contains("ORDER BY CASE WHEN S.ETA IS NULL")
                .endsWith("ISNULL(NULLIF(S.Loadingvesselname,''), S.Offvesselname) ASC, S.Id ASC");
        assertEveryParameterBound(captured);
    }

    @Test
    @DisplayName("ports, employee and all-ETA together still bind every parameter")
    void allFiltersTogether() {
        VesselPlanningLegacyDtos.SearchRequest filter = search();
        filter.setEmployeeid(15);
        filter.setDeliveryDone(true);
        filter.setSearch("PORT KLANG, WESTPORT");

        assertEveryParameterBound(run(filter));
    }
}

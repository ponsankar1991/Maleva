package my.maleva.api.module.invoice.service;

import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the behaviour copied from SP_SaleMaster. Each test is a rule the
 * procedure enforces that a careless rewrite would quietly drop.
 */
class SaleInvoiceWriterTest {

    private static final int COMPANY = 6;
    private static final int NEW_ID = 44100;

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final SaleInvoiceWriter writer = new SaleInvoiceWriter(jdbc);

    @BeforeEach
    void setUp() {
        when(jdbc.queryForMap(contains("SUSER_NAME"), any(SqlParameterSource.class)))
                .thenReturn(Map.of("LoginName", "sa"));
        doAnswer(call -> {
            KeyHolder keys = call.getArgument(2);
            keys.getKeyList().add(Map.of("Id", NEW_ID));
            return 1;
        }).when(jdbc).update(startsWith("INSERT INTO SaleMaster"), any(SqlParameterSource.class),
                any(KeyHolder.class), any(String[].class));
    }

    private SaleInvoiceRequestDTO invoice() {
        return SaleInvoiceRequestDTO.builder()
                .id(0)
                .companyRefId(COMPANY)
                .customerRefId(1109)
                .jobMasterRefId(10)
                .employeeRefId(77)
                .saleDate(LocalDate.of(2026, 9, 8))
                .billType("TR")
                .amount(1060.0)
                .build();
    }

    private SaleInvoiceDetailRequestDTO line() {
        return SaleInvoiceDetailRequestDTO.builder()
                .itemMasterRefId(54)
                .itemQty(2.0)
                .salesRate(500.0)
                .taxRefId(0)
                .saleOrderMasterRefId(20995)
                .build();
    }

    private MapSqlParameterSource capturedInsert() {
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).update(startsWith("INSERT INTO SaleMaster"), params.capture(),
                any(KeyHolder.class), any(String[].class));
        return (MapSqlParameterSource) params.getValue();
    }

    @Test
    void aCreateReturnsTheNewIdentity() {
        assertThat(writer.write(invoice(), List.of(line()))).isEqualTo(NEW_ID);
    }

    @Test
    void aReferenceIdOfZeroIsStoredAsNull() {
        SaleInvoiceRequestDTO request = invoice();
        request.setAgentMasterRefId(0);
        request.setTruckRefId(0);
        request.setSymbolRefId(0);
        request.setUserRefId(null);

        writer.write(request, List.of(line()));
        MapSqlParameterSource params = capturedInsert();

        // The procedure's "if @X = ''" blocks: those are int variables, so
        // T-SQL reads '' as 0 and the rule is really "0 becomes NULL". Storing
        // 0 instead would point the foreign keys at a row that never existed.
        assertThat(params.getValue("AgentMasterRefId")).isNull();
        assertThat(params.getValue("TruckRefid")).isNull();
        assertThat(params.getValue("SymbolRefId")).isNull();
        assertThat(params.getValue("UserRefId")).isNull();
        assertThat(params.getValue("EmployeeRefId")).isEqualTo(77);
    }

    @Test
    void aCreateForcesCreditAndStampsTheAudit() {
        writer.write(invoice(), List.of(line()));
        MapSqlParameterSource params = capturedInsert();

        // The DTO carries no sale type at all; the procedure hard-codes
        // CREDIT on both branches and so do we.
        assertThat(params.getValue("SaleType")).isEqualTo("CREDIT");
        assertThat(params.getValue("Active")).isEqualTo(1);
        assertThat(params.getValue("Created_By")).isEqualTo("sa");
        // Set on a create, and the only one an edit later touches.
        assertThat(params.getValue("LastEmployeeRefId")).isEqualTo(77);
    }

    @Test
    void aCreateLeavesTheNumberForTheCallerToAllocate() {
        writer.write(invoice(), List.of(line()));
        MapSqlParameterSource params = capturedInsert();

        // The number needs the identity, so it is stamped after the insert.
        assertThat(params.getValue("CNumber")).isEqualTo(0);
        assertThat(params.getValue("CNumberDisplay")).isEqualTo("");
    }

    @Test
    void anEditNeverRewritesTheNumberTheDocNoOrTheRaiser() {
        SaleInvoiceRequestDTO request = invoice();
        request.setId(43933);
        when(jdbc.queryForMap(contains("CNumber"), any(SqlParameterSource.class)))
                .thenReturn(Map.of("CNumber", 1, "CNumberDisplay", "INV000000001"));

        writer.write(request, List.of(line()));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc, org.mockito.Mockito.atLeastOnce()).update(sql.capture(), any(SqlParameterSource.class));
        // The edit also runs three cleanup statements; pick out the header one.
        String update = sql.getAllValues().stream()
                .filter(statement -> statement.startsWith("UPDATE SaleMaster SET"))
                .findFirst().orElse("");
        assertThat(update).isNotEmpty();
        assertThat(update).doesNotContain("[CNumber]");
        assertThat(update).doesNotContain("[CNumberDisplay]");
        assertThat(update).doesNotContain("[DOCNo]");
        // The original raiser survives an edit; only the last editor moves.
        assertThat(update).doesNotContain("[EmployeeRefId]");
        assertThat(update).contains("[LastEmployeeRefId]");
        assertThat(update).contains("[CustomerRefId]");
    }

    @Test
    void anEditReleasesEverythingTheOldVersionClaimed() {
        SaleInvoiceRequestDTO request = invoice();
        request.setId(43933);

        writer.write(request, List.of(line()));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc, org.mockito.Mockito.atLeastOnce()).update(sql.capture(), any(SqlParameterSource.class));
        List<String> statements = sql.getAllValues();

        // A job dropped from the invoice must be free to be billed again.
        assertThat(statements).anyMatch(s -> s.contains("UPDATE SaleOrderMaster SET InvoiceNo = 0"));
        assertThat(statements).anyMatch(s -> s.startsWith("DELETE FROM SaleMasterReference"));
        assertThat(statements).anyMatch(s -> s.startsWith("DELETE FROM SaleDetails"));
    }

    @Test
    void aCreateNeverRunsTheEditCleanup() {
        writer.write(invoice(), List.of(line()));

        verify(jdbc, never()).update(startsWith("DELETE FROM SaleDetails"), any(SqlParameterSource.class));
    }

    @Test
    void linesGoInOneBatchNotOneStatementEach() {
        writer.write(invoice(), List.of(line(), line(), line()));

        ArgumentCaptor<MapSqlParameterSource[]> batch = ArgumentCaptor.forClass(MapSqlParameterSource[].class);
        verify(jdbc).batchUpdate(startsWith("INSERT INTO SaleDetails"), batch.capture());
        assertThat(batch.getValue()).hasSize(3);
        assertThat(batch.getValue()[0].getValue("saleMasterRefId")).isEqualTo(NEW_ID);
        // A tax reference of 0 is "no tax code", not a row id.
        assertThat(batch.getValue()[0].getValue("taxRefId")).isNull();
    }

    @Test
    void oneInvoiceCoveringSeveralSaleOrdersStampsAndReferencesEveryOne() {
        // How the real data looks: company 6 has invoices covering 2, 3 and 4
        // jobs. On every one the header SaleOrderMasterNo is 0 — the link is
        // the lines plus one SaleMasterReference row per distinct job.
        SaleInvoiceDetailRequestDTO jobA1 = line();
        SaleInvoiceDetailRequestDTO jobA2 = line();
        SaleInvoiceDetailRequestDTO jobB = line();
        jobB.setSaleOrderMasterRefId(20996);

        writer.write(invoice(), List.of(jobA1, jobA2, jobB));

        // Both jobs marked invoiced, in one statement, scoped to the company.
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).update(eq("UPDATE SaleOrderMaster SET InvoiceNo = :invoiceId "
                + "WHERE Id IN (:ids) AND CompanyRefId = :companyId"), params.capture());
        assertThat(params.getValue().getValue("ids")).isEqualTo(List.of(20995, 20996));

        // One reference row per job, not one per line.
        ArgumentCaptor<MapSqlParameterSource[]> refs = ArgumentCaptor.forClass(MapSqlParameterSource[].class);
        verify(jdbc).batchUpdate(startsWith("INSERT INTO SaleMasterReference"), refs.capture());
        assertThat(refs.getValue()).hasSize(2);
        assertThat(refs.getValue()[0].getValue("saleOrderMasterRefId")).isEqualTo(20995);
        assertThat(refs.getValue()[1].getValue("saleOrderMasterRefId")).isEqualTo(20996);

        // And every line keeps its own job, so the detail rows stay traceable.
        ArgumentCaptor<MapSqlParameterSource[]> lines = ArgumentCaptor.forClass(MapSqlParameterSource[].class);
        verify(jdbc).batchUpdate(startsWith("INSERT INTO SaleDetails"), lines.capture());
        assertThat(lines.getValue())
                .extracting(param -> (Object) param.getValue("saleOrderMasterRefId"))
                .containsExactly(20995, 20995, 20996);
    }

    @Test
    void referencedOrdersComeFromTheRequestElseTheLines() {
        SaleInvoiceRequestDTO named = invoice();
        named.setSaleOrderRefIds(List.of(11, 12, 11));
        assertThat(SaleInvoiceWriter.referencedSaleOrders(named, List.of(line())))
                .containsExactly(11, 12);

        // Nothing named: the lines are the list, de-duplicated, zeros dropped.
        SaleInvoiceDetailRequestDTO zero = line();
        zero.setSaleOrderMasterRefId(0);
        assertThat(SaleInvoiceWriter.referencedSaleOrders(invoice(), List.of(line(), line(), zero)))
                .containsExactly(20995);
    }

    @Test
    void aNamedSaleOrderOnTheHeaderIsStampedOnCreate() {
        SaleInvoiceRequestDTO request = invoice();
        request.setSaleOrderMasterNo(20995);

        writer.write(request, List.of(line()));

        verify(jdbc).update(eq("UPDATE SaleOrderMaster SET InvoiceNo = :invoiceId "
                + "WHERE Id = :saleOrderId AND CompanyRefId = :companyId"), any(SqlParameterSource.class));
    }

    @Test
    void anInvoiceWithNoLinesStillWritesItsHeader() {
        writer.write(invoice(), List.of());

        verify(jdbc, never()).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
        assertThat(capturedInsert().getValue("CompanyRefId")).isEqualTo(COMPANY);
    }
}

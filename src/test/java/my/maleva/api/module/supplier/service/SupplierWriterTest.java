package my.maleva.api.module.supplier.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.supplier.dto.SupplierDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static my.maleva.api.module.supplier.service.SupplierWriter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Pins the Java port of {@code SP_Supplier}: the SQL it runs, what it binds,
 * how many calls a save costs, and when it refuses. The SQL assertions quote
 * the procedure — compare with it before "fixing" a failing one. Whether the
 * SQL actually stores the same rows is SupplierSaveParityIT's job.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupplierWriterTest {

    private static final int COMPANY = 6;

    @Mock private NamedParameterJdbcTemplate jdbc;

    private SupplierWriter writer;
    private Map<String, Object> insertChecks;
    private Map<String, Object> updateChecks;

    @BeforeEach
    void setUp() {
        writer = new SupplierWriter(jdbc);

        insertChecks = new HashMap<>();
        insertChecks.put("LockStatus", 0);
        insertChecks.put("ParentId", 9);
        insertChecks.put("Siblings", 311);
        insertChecks.put("MaxNumber", 318);
        insertChecks.put("SymbolFound", 1);
        insertChecks.put("PaymentTermsFound", 1);
        when(jdbc.queryForMap(eq(INSERT_PRECHECK_SQL), any(SqlParameterSource.class))).thenAnswer(call -> insertChecks);
        when(jdbc.queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class)))
                .thenReturn(Map.of("AccountRefid", 5120, "SupplierId", 402));

        updateChecks = new HashMap<>();
        updateChecks.put("SupplierFound", 1);
        updateChecks.put("SymbolFound", 1);
        updateChecks.put("PaymentTermsFound", 1);
        when(jdbc.queryForMap(eq(UPDATE_PRECHECK_SQL), any(SqlParameterSource.class))).thenAnswer(call -> updateChecks);
    }

    private SupplierDto dto() {
        SupplierDto dto = new SupplierDto();
        dto.setCompanyRefId(COMPANY);
        dto.setSupplierName("Petron Klang");
        dto.setSupplierType("vendor");
        dto.setSymbolRefid(2);
        dto.setPaymentTermsRefid(3);
        return dto;
    }

    private SqlParameterSource insertParams() {
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).queryForMap(eq(INSERT_SQL), params.capture());
        return params.getValue();
    }

    // ─── insert ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("an insert costs two database calls: the checks, then both inserts")
    void insertIsTwoRoundTrips() {
        writer.insert(dto(), COMPANY);

        verify(jdbc).queryForMap(eq(INSERT_PRECHECK_SQL), any(SqlParameterSource.class));
        verify(jdbc).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    @DisplayName("insert binds SUP-<children + 1>, SU + 9 digits, and returns the supplier id")
    void insertBindsNumbersAndReturnsId() {
        int id = writer.insert(dto(), COMPANY);

        SqlParameterSource params = insertParams();
        assertThat(id).isEqualTo(402);
        assertThat(params.getValue("rowNumber")).isEqualTo("SUP-312");
        assertThat(params.getValue("parentId")).isEqualTo(9);
        assertThat(params.getValue("codeNew")).isEqualTo(319);
        assertThat(params.getValue("code")).isEqualTo("SU000000319");
        // Bound as typed; UPPER runs in SQL Server.
        assertThat(params.getValue("supplierName")).isEqualTo("Petron Klang");
    }

    @Test
    @DisplayName("numbering is locked per company, owned by the transaction")
    void insertLocksNumberingPerCompany() {
        writer.insert(dto(), COMPANY);

        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).queryForMap(eq(INSERT_PRECHECK_SQL), params.capture());
        assertThat(params.getValue().getValue("lockKey")).isEqualTo("SupplierNumber:6");
        assertThat(INSERT_PRECHECK_SQL).contains(
                "sp_getapplock", "@LockMode = 'Exclusive'", "@LockOwner = 'Transaction'");
        // The lock is taken before the numbers it protects are read.
        assertThat(INSERT_PRECHECK_SQL.indexOf("sp_getapplock")).isLessThan(INSERT_PRECHECK_SQL.indexOf("MAX(CNumber)"));
    }

    @Test
    @DisplayName("a save that cannot get the numbering lock is refused, not written unguarded")
    void insertRefusedWhenLockTimesOut() {
        insertChecks.put("LockStatus", -1);

        assertThatThrownBy(() -> writer.insert(dto(), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("try again");
        verify(jdbc, never()).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("an insert outside a transaction is a programming error")
    void insertOutsideTransactionFails() {
        insertChecks.put("LockStatus", -999);

        assertThatThrownBy(() -> writer.insert(dto(), COMPANY)).isInstanceOf(IllegalStateException.class);
        verify(jdbc, never()).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("the number keeps the last nine digits, like RIGHT(...,9)")
    void formatsNumberLikeRight() {
        assertThat(formatCNumber(1)).isEqualTo("SU000000001");
        assertThat(formatCNumber(1234567890)).isEqualTo("SU234567890");
    }

    @Test
    @DisplayName("insert SQL carries the procedure's per-column casing and defaults")
    void insertSqlMatchesProcedure() {
        assertThat(INSERT_ACCOUNT_SQL).contains("UPPER(:supplierName)", "SUSER_NAME()", ":parentId, 1, 1,");
        assertThat(INSERT_SUPPLIER_SQL).contains(
                "VALUES (:comid, @accountRefid, UPPER(:supplierName), :code, :codeNew, UPPER(:supplierType)",
                "UPPER(ISNULL(:address1, ''))",
                "UPPER(ISNULL(:city, '')), UPPER(ISNULL(:state, '')), ISNULL(:zipcode, ''), UPPER(ISNULL(:country, ''))",
                "ISNULL(:gstNo, ''), ISNULL(:email, ''), ISNULL(:mobileNo, ''), UPPER(ISNULL(:userName, ''))",
                "ISNULL(:password, '')",
                "UPPER(ISNULL(:oName, ''))",
                "UPPER(ISNULL(:aName, ''))",
                "1, GETDATE(), GETDATE(), 'SA'",
                ":tinNo, :sstNo, :msicCode, :serviceTaxType, :bankName, :accountNo",
                ":registrationNo, :supplierCity)");
        // Columns the procedure never lists keep their database defaults.
        assertThat(INSERT_SUPPLIER_SQL).doesNotContain("OpeningBalance", "QNECode", "QNEId");
        // The supplier is linked to the account inserted just before it.
        assertThat(INSERT_SQL.indexOf("INSERT INTO AccountsGroupMaster"))
                .isLessThan(INSERT_SQL.indexOf("SCOPE_IDENTITY()"));
    }

    @Test
    @DisplayName("the zero date means no expiry; a real date binds as a SQL date")
    void insertBindsExpiryDate() {
        SupplierDto dto = dto();
        dto.setExpiryDate(LocalDate.of(1900, 1, 1));
        writer.insert(dto, COMPANY);
        assertThat(insertParams().getValue("expiryDate")).isNull();
    }

    @Test
    @DisplayName("a real expiry date is bound as a SQL date")
    void insertBindsRealExpiryDate() {
        SupplierDto dto = dto();
        dto.setExpiryDate(LocalDate.of(2027, 3, 31));
        writer.insert(dto, COMPANY);
        assertThat(insertParams().getValue("expiryDate")).isEqualTo(Date.valueOf("2027-03-31"));
    }

    @Test
    @DisplayName("an unknown symbol is refused before anything is written")
    void insertRejectsUnknownSymbol() {
        insertChecks.put("SymbolFound", 0);

        assertThatThrownBy(() -> writer.insert(dto(), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Symbol Master Not Found Issue id 2");
        verify(jdbc, never()).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("an unknown payment term is refused before anything is written")
    void insertRejectsUnknownPaymentTerm() {
        insertChecks.put("PaymentTermsFound", 0);

        assertThatThrownBy(() -> writer.insert(dto(), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Payment Terms Master Not Found Issue id 3");
        verify(jdbc, never()).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("zero means 'not chosen' and passes the master checks, as <> 0 does")
    void insertAllowsZeroReferences() {
        insertChecks.put("SymbolFound", 0);
        insertChecks.put("PaymentTermsFound", 0);
        SupplierDto dto = dto();
        dto.setSymbolRefid(0);
        dto.setPaymentTermsRefid(0);

        assertThat(writer.insert(dto, COMPANY)).isEqualTo(402);
    }

    @Test
    @DisplayName("a company with no SUPPLIERS group is refused instead of an orphan account")
    void insertRejectsMissingAccountGroup() {
        insertChecks.put("ParentId", null);

        assertThatThrownBy(() -> writer.insert(dto(), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("SUPPLIERS");
        verify(jdbc, never()).queryForMap(eq(INSERT_SQL), any(SqlParameterSource.class));
    }

    // ─── update ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("an update costs two database calls: the checks, then the update and rename")
    void updateIsTwoRoundTrips() {
        writer.update(402, dto(), COMPANY);

        verify(jdbc).queryForMap(eq(UPDATE_PRECHECK_SQL), any(SqlParameterSource.class));
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).update(eq(UPDATE_SQL), params.capture());
        verifyNoMoreInteractions(jdbc);
        assertThat(params.getValue().getValue("id")).isEqualTo(402);
        assertThat(params.getValue().getValue("comid")).isEqualTo(COMPANY);
    }

    @Test
    @DisplayName("an update of a supplier that is not the company's is refused before anything is written")
    void updateRefusesUnknownSupplier() {
        updateChecks.put("SupplierFound", 0);

        assertThatThrownBy(() -> writer.update(402, dto(), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("402");
        verify(jdbc, never()).update(eq(UPDATE_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("an update with an unknown symbol is refused before anything is written")
    void updateRefusesUnknownSymbol() {
        updateChecks.put("SymbolFound", 0);

        assertThatThrownBy(() -> writer.update(402, dto(), COMPANY)).isInstanceOf(InvalidRequestException.class);
        verify(jdbc, never()).update(eq(UPDATE_SQL), any(SqlParameterSource.class));
    }

    @Test
    @DisplayName("update SQL carries the edit branch's casing and leaves identity columns alone")
    void updateSqlMatchesProcedure() {
        assertThat(UPDATE_SUPPLIER_SQL).contains(
                "Zipcode = UPPER(ISNULL(:zipcode, ''))",
                "GSTNO = UPPER(ISNULL(:gstNo, ''))",
                "UserName = ISNULL(:userName, '')",
                "Password = :password,",
                "Modified_Date = GETDATE()",
                "WHERE Id = :id");
        assertThat(UPDATE_SUPPLIER_SQL).doesNotContain(
                "CNumber", "AccountRefid", "Latitude", "longitude", "TokenId", "Created_Date", "Modified_By");
        // The rename finds the account through the supplier, inside this company only.
        assertThat(RENAME_ACCOUNT_SQL).contains(
                "SET AccountName = UPPER(:supplierName)",
                "SELECT TOP 1 AccountRefid FROM Supplier WHERE Id = :id AND CompanyRefId = :comid",
                "AND CompanyRefId = :comid");
        assertThat(UPDATE_SQL.indexOf("UPDATE Supplier")).isLessThan(UPDATE_SQL.indexOf("UPDATE AccountsGroupMaster"));
    }

    // ─── soft delete ────────────────────────────────────────────────────

    @Test
    @DisplayName("soft delete is one call that changes Active and nothing else")
    void softDeleteChangesOnlyActive() {
        when(jdbc.queryForObject(eq(SOFT_DELETE_SQL), any(SqlParameterSource.class), eq(Integer.class))).thenReturn(1);

        assertThat(writer.softDelete(402, COMPANY)).isTrue();

        assertThat(SOFT_DELETE_SQL).contains("UPDATE Supplier SET Active = 2 WHERE Id = :id AND CompanyRefId = :comid");
        // Exactly one column is assigned: nothing between SET and WHERE but Active.
        String assignments = SOFT_DELETE_SQL.substring(
                SOFT_DELETE_SQL.indexOf("UPDATE Supplier SET ") + "UPDATE Supplier SET ".length(),
                SOFT_DELETE_SQL.indexOf(" WHERE "));
        assertThat(assignments).isEqualTo("Active = 2");
        verify(jdbc).queryForObject(eq(SOFT_DELETE_SQL), any(SqlParameterSource.class), eq(Integer.class));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    @DisplayName("soft delete of a supplier that is not the company's reports nothing deleted")
    void softDeleteReportsMissingSupplier() {
        when(jdbc.queryForObject(eq(SOFT_DELETE_SQL), any(SqlParameterSource.class), eq(Integer.class))).thenReturn(0);

        assertThat(writer.softDelete(402, COMPANY)).isFalse();
    }
}

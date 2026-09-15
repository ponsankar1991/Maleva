package my.maleva.api.module.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.service.SupplierWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The proof that {@link SupplierWriter} is {@code SP_Supplier} without the
 * procedure: the same supplier is saved both ways, against MalevanewDemo, and
 * the stored rows are compared column by column.
 *
 * <p>The procedure is fed exactly what legacy {@code InsertSupplier} sent it:
 * a one-element JSON array of {@code SupplierModel}, after the caller's
 * {@code Replace("null", "\"\"")} — so text is {@code ""}, never null, and the
 * {@code Int32} fields are 0 when the page left them out. The writer is fed the
 * {@code SupplierDto} the React screen sends for the same form.
 *
 * <p>{@code SELECT *} on both sides, so a column neither version names still
 * has to agree — that is how a JPA NULL written over a column default would be
 * caught. Only identity, numbering and clock columns are excused, and those are
 * asserted separately.
 *
 * <p>Everything runs in the test's transaction and is rolled back. The
 * procedure's own BEGIN/COMMIT TRAN nests inside it on the success path, which
 * is the only path these tests drive through the procedure. QNE is switched
 * off for the context.
 */
@SpringBootTest(properties = "qne.enabled=false")
@Transactional
class SupplierSaveParityIT {

    private static final int COMPANY = 6;

    /** Differ by construction: new ids, the next number, and GETDATE(). */
    private static final Set<String> SUPPLIER_IDENTITY_COLUMNS =
            Set.of("id", "accountrefid", "cnumber", "cnumberdisplay", "created_date", "modified_date");
    private static final Set<String> ACCOUNT_IDENTITY_COLUMNS =
            Set.of("id", "accountcode", "created_date", "modified_date");

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private SupplierWriter writer;

    private final ObjectMapper json = new ObjectMapper();

    private int symbolId;
    private int paymentTermsId;
    private int msicCodeId;
    private int selfBilledId;

    @BeforeEach
    void pickRealMasters() {
        MapSqlParameterSource company = new MapSqlParameterSource("comid", COMPANY);
        symbolId = firstId("SELECT TOP 1 Id FROM SymbolMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company);
        paymentTermsId = firstId("SELECT TOP 1 Id FROM PaymentTermsMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company);
        msicCodeId = firstId("SELECT TOP 1 Id FROM MSICcode ORDER BY Id", company);
        selfBilledId = firstId("SELECT TOP 1 Id FROM Selfbilled ORDER BY Id", company);
    }

    // ─── insert ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("a fully filled supplier is stored identically by the procedure and the writer")
    void fullInsertMatchesProcedure() throws Exception {
        Sample sample = Sample.full(symbolId, paymentTermsId, msicCodeId, selfBilledId);

        int procedureId = runProcedure(sample.legacyModel(0));
        int javaId = writer.insert(sample.dto(COMPANY), COMPANY);

        assertSameRows(procedureId, javaId);
    }

    @Test
    @DisplayName("a supplier with only the required fields gets the same '' / NULL / 0 defaults")
    void minimalInsertMatchesProcedure() throws Exception {
        Sample sample = Sample.minimal(symbolId, paymentTermsId);

        int procedureId = runProcedure(sample.legacyModel(0));
        int javaId = writer.insert(sample.dto(COMPANY), COMPANY);

        assertSameRows(procedureId, javaId);
    }

    @Test
    @DisplayName("the writer takes the next number and the next SUP code after the procedure's")
    void insertNumbersFollowTheProcedure() throws Exception {
        Sample sample = Sample.minimal(symbolId, paymentTermsId);

        Map<String, Object> procedure = supplierRow(runProcedure(sample.legacyModel(0)));
        Map<String, Object> java = supplierRow(writer.insert(sample.dto(COMPANY), COMPANY));

        int procedureNumber = ((Number) procedure.get("CNumber")).intValue();
        assertThat(((Number) java.get("CNumber")).intValue()).isEqualTo(procedureNumber + 1);
        assertThat(procedure.get("CNumberDisplay")).isEqualTo(String.format("SU%09d", procedureNumber));
        assertThat(java.get("CNumberDisplay")).isEqualTo(String.format("SU%09d", procedureNumber + 1));

        int procedureChild = sequenceOf(accountRow(procedure).get("AccountCode"));
        assertThat(accountRow(java).get("AccountCode")).isEqualTo("SUP-" + (procedureChild + 1));
    }

    /** The legacy caller stripped apostrophes before building the EXEC; with a bound parameter neither side loses them. */
    @Test
    @DisplayName("an apostrophe survives in the name and in the ledger account")
    void apostropheIsKept() {
        int javaId = writer.insert(Sample.minimal(symbolId, paymentTermsId).dto(COMPANY), COMPANY);

        Map<String, Object> java = supplierRow(javaId);
        assertThat(java.get("SupplierName")).isEqualTo("O'NEILL HAULAGE SDN BHD");
        assertThat(accountRow(java).get("AccountName")).isEqualTo("O'NEILL HAULAGE SDN BHD");
    }

    // ─── update ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("an edit is stored identically, including the account rename")
    void updateMatchesProcedure() throws Exception {
        Sample original = Sample.full(symbolId, paymentTermsId, msicCodeId, selfBilledId);
        int procedureId = runProcedure(original.legacyModel(0));
        int javaId = writer.insert(original.dto(COMPANY), COMPANY);

        Sample edited = Sample.edited(symbolId, paymentTermsId);
        runProcedure(edited.legacyModel(procedureId));
        writer.update(javaId, edited.dto(COMPANY), COMPANY);

        assertSameRows(procedureId, javaId);
        assertThat(supplierRow(javaId).get("Active")).isEqualTo(0);
        assertThat(accountRow(supplierRow(javaId)).get("AccountName")).isEqualTo("PETRON KLANG TERMINAL");
    }

    // ─── helpers ────────────────────────────────────────────────────────

    /** {@code Exec [SP_Supplier] '<details>', Comid}, with the JSON bound rather than pasted in. */
    private int runProcedure(Map<String, Object> legacyModel) throws Exception {
        String details = json.writeValueAsString(List.of(legacyModel));
        Map<String, Object> result = jdbc.queryForMap("EXEC [SP_Supplier] :details, :comid",
                new MapSqlParameterSource("details", details).addValue("comid", COMPANY));
        assertThat(result.get("Result")).as("SP_Supplier said: %s", result.get("Msg")).isEqualTo(1);
        return ((Number) result.get("Id")).intValue();
    }

    private void assertSameRows(int procedureId, int javaId) {
        Map<String, Object> procedure = supplierRow(procedureId);
        Map<String, Object> java = supplierRow(javaId);
        assertSameColumns("Supplier", procedure, java, SUPPLIER_IDENTITY_COLUMNS);
        assertSameColumns("AccountsGroupMaster", accountRow(procedure), accountRow(java), ACCOUNT_IDENTITY_COLUMNS);
    }

    private static void assertSameColumns(String table, Map<String, Object> procedure, Map<String, Object> java,
                                          Set<String> excused) {
        List<String> differences = new ArrayList<>();
        for (Map.Entry<String, Object> column : procedure.entrySet()) {
            if (excused.contains(column.getKey().toLowerCase(Locale.ROOT))) {
                continue;
            }
            Object actual = java.get(column.getKey());
            if (!Objects.equals(column.getValue(), actual)) {
                differences.add(column.getKey() + ": procedure=" + render(column.getValue()) + ", java=" + render(actual));
            }
        }
        assertThat(differences).as("%s columns where the Java writer differs from SP_Supplier", table).isEmpty();
    }

    private Map<String, Object> supplierRow(int id) {
        return jdbc.queryForMap("SELECT * FROM Supplier WHERE Id = :id", new MapSqlParameterSource("id", id));
    }

    private Map<String, Object> accountRow(Map<String, Object> supplier) {
        return jdbc.queryForMap("SELECT * FROM AccountsGroupMaster WHERE Id = :id",
                new MapSqlParameterSource("id", supplier.get("AccountRefid")));
    }

    private int firstId(String sql, MapSqlParameterSource params) {
        List<Integer> ids = jdbc.queryForList(sql, params, Integer.class);
        assertThat(ids).as("MalevanewDemo has no row for: %s", sql).isNotEmpty();
        return ids.get(0);
    }

    private static int sequenceOf(Object accountCode) {
        String code = String.valueOf(accountCode);
        assertThat(code).startsWith("SUP-");
        return Integer.parseInt(code.substring("SUP-".length()));
    }

    private static String render(Object value) {
        return value == null ? "NULL" : "'" + value + "' (" + value.getClass().getSimpleName() + ")";
    }

    /**
     * One form's worth of supplier, expressible both as the legacy JSON model
     * and as the React DTO. Text is mixed case on purpose, so every UPPER — and
     * every column that is NOT upper-cased — is exercised.
     */
    private record Sample(Map<String, Object> text, int symbolId, int paymentTermsId, int msicCodeRefId,
                          int selfBilled, int active, LocalDate expiryDate) {

        static Sample full(int symbolId, int paymentTermsId, int msicCodeId, int selfBilledId) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("SupplierName", "O'Neill Haulage Sdn Bhd");
            t.put("SupplierType", "Vendor");
            t.put("Address1", "Lot 5, Jalan Kem\nPelabuhan Klang");
            t.put("Address2", "Block b");
            t.put("Address3", "");
            t.put("PersonId", "roc-12");
            t.put("City", "Ms Tan");
            t.put("State", "Selangor");
            t.put("Zipcode", "42000a");
            t.put("Country", "10");
            t.put("GSTNO", "gst-9");
            t.put("Email", "AP@ONeill.example");
            t.put("MobileNo", "012-3456789");
            t.put("UserName", "oneill");
            t.put("Password", "Pw-7");
            t.put("OEmail", "49301");
            t.put("OEmail1", "02");
            t.put("OName", "Maybank");
            t.put("OPhone", "514011223344");
            t.put("AEmail", "Accounts@ONeill.example");
            t.put("AEmail1", "C2584563222");
            t.put("AName", "Raj");
            t.put("APhone", "w10-1808");
            t.put("TinNo", "c123");
            t.put("SSTNo", "sst-1");
            t.put("MsicCode", "49301");
            t.put("ServiceTaxType", "01");
            t.put("BankName", "Maybank");
            t.put("AccountNo", "5140");
            t.put("TinType", "Company (Malaysia)");
            t.put("SupplierTin", "c2584563222");
            t.put("TaxExemptionNo", "te-1");
            t.put("TaxExemptionDetails", "Exempt goods");
            t.put("RegistrationNo", "201901012345");
            t.put("SupplierCity", "Klang");
            return new Sample(t, symbolId, paymentTermsId, msicCodeId, selfBilledId, 1, LocalDate.of(2027, 3, 31));
        }

        /** What the screen sends when only name, type, symbol and payment term are filled. */
        static Sample minimal(int symbolId, int paymentTermsId) {
            Map<String, Object> t = new LinkedHashMap<>();
            for (String key : full(0, 0, 0, 0).text().keySet()) {
                t.put(key, "");
            }
            t.put("SupplierName", "O'Neill Haulage Sdn Bhd");
            t.put("SupplierType", "ALL");
            return new Sample(t, symbolId, paymentTermsId, 0, 0, 1, null);
        }

        static Sample edited(int symbolId, int paymentTermsId) {
            Sample base = full(symbolId, paymentTermsId, 0, 0);
            Map<String, Object> t = new LinkedHashMap<>(base.text());
            t.put("SupplierName", "Petron Klang Terminal");
            t.put("SupplierType", "maintenance");
            t.put("Zipcode", "41000b");
            t.put("GSTNO", "gst-10");
            t.put("UserName", "petron");
            t.put("Password", "");
            t.put("OName", "cimb");
            t.put("SupplierCity", "Port Klang");
            return new Sample(t, symbolId, paymentTermsId, 0, 0, 0, null);
        }

        /** {@code SupplierModel} as Json.NET serialised it, after InsertSupplier's null → "" replace. */
        Map<String, Object> legacyModel(int id) {
            Map<String, Object> model = new LinkedHashMap<>(text);
            model.put("Id", id);
            model.put("SymbolRefid", symbolId);
            model.put("PaymentTermsRefid", paymentTermsId);
            model.put("MSICCodeRefId", msicCodeRefId);
            model.put("SelfBilled", selfBilled);
            model.put("Active", active);
            model.put("ExpiryDate", expiryDate == null ? "" : expiryDate.toString());
            model.put("Latitude", "");
            model.put("longitude", "");
            model.put("TokenId", "");
            return model;
        }

        /** What supplier.contract.ts {@code toSupplierDto} produces for the same form. */
        SupplierDto dto(int companyId) {
            SupplierDto dto = new SupplierDto();
            dto.setCompanyRefId(companyId);
            dto.setSupplierName(s("SupplierName"));
            dto.setSupplierType(s("SupplierType"));
            dto.setAddress1(s("Address1"));
            dto.setAddress2(s("Address2"));
            dto.setAddress3(s("Address3"));
            dto.setPersonId(s("PersonId"));
            dto.setCity(s("City"));
            dto.setState(s("State"));
            dto.setZipcode(s("Zipcode"));
            dto.setCountry(s("Country"));
            dto.setGstNo(s("GSTNO"));
            dto.setEmail(s("Email"));
            dto.setMobileNo(s("MobileNo"));
            dto.setUserName(s("UserName"));
            dto.setPassword(s("Password"));
            dto.setOEmail(s("OEmail"));
            dto.setOEmail1(s("OEmail1"));
            dto.setOName(s("OName"));
            dto.setOPhone(s("OPhone"));
            dto.setAEmail(s("AEmail"));
            dto.setAEmail1(s("AEmail1"));
            dto.setAName(s("AName"));
            dto.setAPhone(s("APhone"));
            dto.setTinNo(s("TinNo"));
            dto.setSstNo(s("SSTNo"));
            dto.setMsicCode(s("MsicCode"));
            dto.setServiceTaxType(s("ServiceTaxType"));
            dto.setBankName(s("BankName"));
            dto.setAccountNo(s("AccountNo"));
            dto.setTinType(s("TinType"));
            dto.setSupplierTin(s("SupplierTin"));
            dto.setTaxExemptionNo(s("TaxExemptionNo"));
            dto.setTaxExemptionDetails(s("TaxExemptionDetails"));
            dto.setRegistrationNo(s("RegistrationNo"));
            dto.setSupplierCity(s("SupplierCity"));
            dto.setSymbolRefid(symbolId);
            dto.setPaymentTermsRefid(paymentTermsId);
            dto.setMsicCodeRefId(msicCodeRefId);
            dto.setSelfBilled(selfBilled);
            dto.setActive(active);
            dto.setExpiryDate(expiryDate);
            return dto;
        }

        private String s(String key) {
            return (String) text.get(key);
        }
    }
}

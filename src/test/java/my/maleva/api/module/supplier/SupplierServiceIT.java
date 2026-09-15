package my.maleva.api.module.supplier;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierGridPage;
import my.maleva.api.module.supplier.dto.SupplierGridRequest;
import my.maleva.api.module.supplier.dto.SupplierListRow;
import my.maleva.api.module.supplier.dto.SupplierLookupOption;
import my.maleva.api.module.supplier.dto.SupplierQneOutcome;
import my.maleva.api.module.supplier.dto.SupplierQneSyncResult;
import my.maleva.api.module.supplier.repository.SupplierQneSyncRepository;
import my.maleva.api.module.supplier.service.SupplierQneService;
import my.maleva.api.module.supplier.service.SupplierQneSyncService;
import my.maleva.api.module.supplier.service.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The supplier screen's server side, exercised against MalevanewDemo: saves,
 * the grid search, the QNE sync's database side, lookups and routes. Everything
 * is rolled back, and QNE is switched off so no test can reach QNE.
 *
 * <p>The grid search and the sync reads are hand-written SQL, so only running
 * them proves the column names, joins, escaping and paging — mocks cannot.
 * Suppliers created here carry a unique tag so searches find exactly them
 * among the company's real suppliers.
 */
@SpringBootTest(properties = "qne.enabled=false")
@Transactional
class SupplierServiceIT {

    private static final int COMPANY = 6;

    @Autowired private SupplierService service;
    @Autowired private SupplierQneService qneService;
    @Autowired private SupplierQneSyncService syncService;
    @Autowired private SupplierQneSyncRepository syncRepository;
    @Autowired private NamedParameterJdbcTemplate jdbc;

    // Qualified: actuator contributes a second RequestMappingHandlerMapping.
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private int symbolId;
    private int paymentTermsId;
    private String tag;

    @BeforeEach
    void setUp() {
        MapSqlParameterSource company = new MapSqlParameterSource("comid", COMPANY);
        symbolId = jdbc.queryForObject(
                "SELECT TOP 1 Id FROM SymbolMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company, Integer.class);
        paymentTermsId = jdbc.queryForObject(
                "SELECT TOP 1 Id FROM PaymentTermsMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company, Integer.class);
        tag = "ZZIT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ─── safety ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("the test context cannot push to or pull from QNE")
    void qneIsOffForTheseTests() {
        SupplierDto created = service.create(newSupplier(tag + " QNE GUARD", "VENDOR"));

        assertThat(qneService.pushSaved(created, newSupplier(tag + " QNE GUARD", "VENDOR")).status())
                .isEqualTo(SupplierQneOutcome.Status.DISABLED);
        assertThat(syncService.syncFromQne(COMPANY).status()).isEqualTo(SupplierQneSyncResult.Status.DISABLED);
    }

    // ─── save ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("create numbers the supplier, links a SUP account, and returns every box")
    void createNumbersAndLinks() {
        SupplierDto dto = newSupplier(tag + " Integration Supplier", "vendor");
        dto.setAEmail1("C2584563222");   // the box labelled TIN NO
        dto.setOName("maybank");         // the box labelled BANK NAME
        SupplierDto created = service.create(dto);

        assertThat(created.getCNumberDisplay()).matches("^SU\\d{9}$");
        assertThat(created.getCNumber()).isPositive();
        assertThat(created.getSupplierName()).isEqualTo(tag + " INTEGRATION SUPPLIER");
        assertThat(created.getSupplierType()).isEqualTo("VENDOR");
        assertThat(created.getActive()).isEqualTo(1);
        assertThat(created.getAEmail1()).isEqualTo("C2584563222");
        assertThat(created.getOName()).isEqualTo("MAYBANK");

        // What the edit screen loads: the same boxes must come back, or the next Save erases them.
        SupplierDto reloaded = service.getById(created.getId()).orElseThrow();
        assertThat(reloaded.getAEmail1()).isEqualTo("C2584563222");
        assertThat(reloaded.getOName()).isEqualTo("MAYBANK");

        Map<String, Object> account = jdbc.queryForMap(
                "SELECT A.AccountCode, A.AccountName, P.AccountName AS ParentName FROM AccountsGroupMaster A "
                        + "JOIN AccountsGroupMaster P ON P.Id = A.ParentId WHERE A.Id = :id",
                new MapSqlParameterSource("id", created.getAccountRefid()));
        assertThat((String) account.get("AccountCode")).startsWith("SUP-");
        assertThat(account.get("AccountName")).isEqualTo(tag + " INTEGRATION SUPPLIER");
        assertThat(account.get("ParentName")).isEqualTo("SUPPLIERS");
    }

    @Test
    @DisplayName("saves one after another take consecutive numbers and SUP codes")
    void consecutiveCreatesTakeConsecutiveNumbers() {
        SupplierDto first = service.create(newSupplier(tag + " Sequence One", "VENDOR"));
        SupplierDto second = service.create(newSupplier(tag + " Sequence Two", "VENDOR"));

        assertThat(second.getCNumber()).isEqualTo(first.getCNumber() + 1);
        assertThat(accountSequence(second)).isEqualTo(accountSequence(first) + 1);
    }

    @Test
    @DisplayName("update keeps the number and account, and applies the edit")
    void updateKeepsIdentity() {
        SupplierDto created = service.create(newSupplier(tag + " Before Edit", "VENDOR"));

        SupplierDto edit = newSupplier(tag + " After Edit", "OPERATION");
        edit.setActive(0);
        SupplierDto updated = service.update(created.getId(), edit);

        assertThat(updated.getCNumberDisplay()).isEqualTo(created.getCNumberDisplay());
        assertThat(updated.getAccountRefid()).isEqualTo(created.getAccountRefid());
        assertThat(updated.getSupplierName()).isEqualTo(tag + " AFTER EDIT");
        assertThat(updated.getActive()).isZero();
    }

    @Test
    @DisplayName("another company's supplier is refused and left untouched")
    void updateRefusesAnotherCompany() {
        SupplierDto created = service.create(newSupplier(tag + " Owned By Six", "VENDOR"));

        SupplierDto intruder = newSupplier("Hijacked", "VENDOR");
        intruder.setCompanyRefId(COMPANY + 1);

        assertThatThrownBy(() -> service.update(created.getId(), intruder)).isInstanceOf(InvalidRequestException.class);
        assertThat(service.getById(created.getId()).orElseThrow().getSupplierName()).isEqualTo(tag + " OWNED BY SIX");
    }

    @Test
    @DisplayName("an update of a supplier that does not exist is refused")
    void updateOfMissingSupplierIsRefused() {
        assertThatThrownBy(() -> service.update(999_999_999, newSupplier("Ghost", "VENDOR")))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("999999999");
    }

    @Test
    @DisplayName("a symbol that is not the company's is refused before anything is written")
    void createRefusesForeignSymbol() {
        SupplierDto dto = newSupplier(tag + " Bad Symbol", "VENDOR");
        dto.setSymbolRefid(999_999);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Symbol Master Not Found Issue id 999999");
        assertThat(search(q -> q.setKeyword(tag)).total()).isZero();
    }

    // ─── soft delete ────────────────────────────────────────────────────

    @Test
    @DisplayName("soft delete changes Active and no other column")
    void softDeleteChangesOnlyActive() {
        int id = service.create(newSupplier(tag + " Only Active Changes", "VENDOR")).getId();
        Map<String, Object> before = supplierRow(id);

        service.softDelete(id, COMPANY);

        Map<String, Object> after = supplierRow(id);
        assertThat(after.get("Active")).isEqualTo(2);
        before.remove("Active");
        after.remove("Active");
        assertThat(after).isEqualTo(before);
        // The row loaded by create() in this transaction was refreshed, not left stale.
        assertThat(service.getById(id).orElseThrow().getActive()).isEqualTo(2);
    }

    @Test
    @DisplayName("a soft-deleted supplier leaves the search")
    void softDeletedSupplierLeavesSearch() {
        int id = service.create(newSupplier(tag + " To Delete", "VENDOR")).getId();
        assertThat(ids(search(q -> q.setKeyword(tag)))).contains(id);

        service.softDelete(id, COMPANY);

        assertThat(ids(search(q -> q.setKeyword(tag)))).doesNotContain(id);
    }

    @Test
    @DisplayName("soft delete refuses another company's supplier")
    void softDeleteRefusesAnotherCompany() {
        int id = service.create(newSupplier(tag + " Not Yours", "VENDOR")).getId();

        assertThatThrownBy(() -> service.softDelete(id, COMPANY + 1)).isInstanceOf(InvalidRequestException.class);
        assertThat(supplierRow(id).get("Active")).isEqualTo(1);
    }

    // ─── the grid search ────────────────────────────────────────────────

    @Test
    @DisplayName("a row carries the joined symbol, payment term and account code")
    void searchRowCarriesJoinedMasters() {
        int id = service.create(newSupplier(tag + " Grid Row", "VENDOR")).getId();

        SupplierListRow row = findRow(search(q -> q.setKeyword(tag)), id);

        assertThat(row.sName()).isEqualTo(jdbc.queryForObject("SELECT SName FROM SymbolMaster WHERE Id = :id",
                new MapSqlParameterSource("id", symbolId), String.class));
        assertThat(row.termsName()).isEqualTo(jdbc.queryForObject("SELECT TermsName FROM PaymentTermsMaster WHERE Id = :id",
                new MapSqlParameterSource("id", paymentTermsId), String.class));
        assertThat(row.accountCode()).startsWith("SUP-");
        assertThat(row.supplierName()).isEqualTo(tag + " GRID ROW");
        assertThat(row.active()).isEqualTo(1);
    }

    @Test
    @DisplayName("the keyword finds a supplier by name, by account code, by city and by id — case-insensitively")
    void keywordSearchesEveryColumn() {
        SupplierDto dto = newSupplier(tag + " Keyword Target", "VENDOR");
        dto.setCity(tag + "-PIC");
        SupplierDto created = service.create(dto);
        String accountCode = jdbc.queryForObject("SELECT AccountCode FROM AccountsGroupMaster WHERE Id = :id",
                new MapSqlParameterSource("id", created.getAccountRefid()), String.class);

        assertThat(ids(search(q -> q.setKeyword(tag.toLowerCase() + " keyword")))).containsExactly(created.getId());
        assertThat(ids(search(q -> q.setKeyword(accountCode)))).contains(created.getId());
        assertThat(ids(search(q -> q.setKeyword(tag + "-pic")))).containsExactly(created.getId());
        assertThat(ids(search(q -> q.setKeyword(String.valueOf(created.getId()))))).contains(created.getId());
    }

    @Test
    @DisplayName("column filters narrow by their own column only")
    void columnFiltersNarrow() {
        SupplierDto inKlang = newSupplier(tag + " In Klang", "VENDOR");
        inKlang.setCity("KLANG-" + tag);
        int klang = service.create(inKlang).getId();
        int elsewhere = service.create(newSupplier(tag + " Elsewhere", "VENDOR")).getId();

        List<Integer> found = ids(search(q -> {
            q.setSupplierName(tag);
            q.setCity("klang-" + tag.toLowerCase());
        }));

        assertThat(found).containsExactly(klang).doesNotContain(elsewhere);
    }

    @Test
    @DisplayName("the Type combo filters exactly, and ALL or blank means every type")
    void typeFilter() {
        int operation = service.create(newSupplier(tag + " Operation Only", "OPERATION")).getId();

        assertThat(ids(search(q -> { q.setKeyword(tag); q.setType("OPERATION"); }))).containsExactly(operation);
        assertThat(ids(search(q -> { q.setKeyword(tag); q.setType("VENDOR"); }))).isEmpty();
        assertThat(ids(search(q -> { q.setKeyword(tag); q.setType("ALL"); }))).containsExactly(operation);
        assertThat(ids(search(q -> { q.setKeyword(tag); q.setType(""); }))).containsExactly(operation);
    }

    @Test
    @DisplayName("the active filter separates active from inactive, and deleted rows never show")
    void activeFilter() {
        int active = service.create(newSupplier(tag + " Active One", "VENDOR")).getId();
        SupplierDto inactiveDto = newSupplier(tag + " Inactive One", "VENDOR");
        int inactive = service.create(inactiveDto).getId();
        inactiveDto.setActive(0);
        service.update(inactive, inactiveDto);

        assertThat(ids(search(q -> { q.setKeyword(tag); q.setActive("active"); }))).containsExactly(active);
        assertThat(ids(search(q -> { q.setKeyword(tag); q.setActive("inactive"); }))).containsExactly(inactive);
        assertThat(search(q -> q.setSize(500)).items()).noneMatch(r -> Integer.valueOf(2).equals(r.active()));
    }

    @Test
    @DisplayName("% and _ in a search are literal, not wildcards")
    void wildcardsAreLiteral() {
        int percent = service.create(newSupplier(tag + " 100% PURE", "VENDOR")).getId();
        service.create(newSupplier(tag + " 1000 PURE", "VENDOR"));

        assertThat(ids(search(q -> q.setKeyword(tag + " 100%")))).containsExactly(percent);
        assertThat(ids(search(q -> q.setKeyword(tag + " 1_0")))).isEmpty();
    }

    @Test
    @DisplayName("pages hold the requested size, report the full total, and never repeat a row")
    void paging() {
        for (int i = 0; i < 5; i++) {
            service.create(newSupplier(tag + " Page " + i, "VENDOR"));
        }

        SupplierGridPage first = search(q -> { q.setKeyword(tag); q.setSize(2); q.setPage(0); });
        SupplierGridPage second = search(q -> { q.setKeyword(tag); q.setSize(2); q.setPage(1); });
        SupplierGridPage third = search(q -> { q.setKeyword(tag); q.setSize(2); q.setPage(2); });
        SupplierGridPage beyond = search(q -> { q.setKeyword(tag); q.setSize(2); q.setPage(9); });

        assertThat(first.total()).isEqualTo(5);
        assertThat(first.items()).hasSize(2);
        assertThat(second.items()).hasSize(2);
        assertThat(third.items()).hasSize(1);
        assertThat(ids(first)).doesNotContainAnyElementsOf(ids(second));
        assertThat(beyond.items()).isEmpty();
        assertThat(beyond.total()).isEqualTo(5);
    }

    @Test
    @DisplayName("sorting by a column orders the rows; an unknown sort falls back without an error")
    void sorting() {
        service.create(newSupplier(tag + " B", "VENDOR"));
        service.create(newSupplier(tag + " A", "VENDOR"));
        service.create(newSupplier(tag + " C", "VENDOR"));

        List<String> ascending = search(q -> { q.setKeyword(tag); q.setSortBy("supplierName"); q.setSortDir("asc"); })
                .items().stream().map(SupplierListRow::supplierName).toList();
        assertThat(ascending).containsExactly(tag + " A", tag + " B", tag + " C");

        assertThat(search(q -> { q.setKeyword(tag); q.setSortBy("Id; DROP TABLE Supplier"); }).total()).isEqualTo(3);
    }

    @Test
    @DisplayName("search refuses a request without a company")
    void searchNeedsCompany() {
        assertThatThrownBy(() -> service.search(new SupplierGridRequest())).isInstanceOf(InvalidRequestException.class);
    }

    // ─── the QNE sync's database side ───────────────────────────────────

    @Test
    @DisplayName("a supplier created from QNE carries its QNE identity and is found by QNE code")
    void createFromQneLinksIdentity() {
        String code = "QIT-" + tag;
        int id = service.createFromQne(newSupplier(tag + " From Qne", "VENDOR"), "guid-" + tag, code);

        Map<String, Object> row = supplierRow(id);
        assertThat(row.get("QNECode")).isEqualTo(code);
        assertThat(row.get("QNEId")).isEqualTo("guid-" + tag);
        assertThat(ids(search(q -> q.setQneCode(code)))).containsExactly(id);
    }

    @Test
    @DisplayName("the sync's reads run, and a deleted supplier still counts as known")
    void syncReadsIncludeDeletedSuppliers() {
        String code = "QIT-" + tag;
        int id = service.createFromQne(newSupplier(tag + " Deleted In Maleva", "VENDOR"), "guid-" + tag, code);
        service.softDelete(id, COMPANY);

        assertThat(syncRepository.linkedSuppliers(COMPANY))
                .anyMatch(link -> link.id() == id && code.equals(link.qneCode()));
        assertThat(syncRepository.symbols(COMPANY)).isNotEmpty().allMatch(s -> s.name() != null || s.id() > 0);
        assertThat(syncRepository.paymentTerms(COMPANY)).isNotEmpty();
    }

    @Test
    @DisplayName("repairing QNE ids writes the id against the code, in one batch")
    void repairQneIds() {
        String code = "QIT-" + tag;
        int id = service.createFromQne(newSupplier(tag + " Repair Me", "VENDOR"), null, code);

        syncRepository.repairQneIds(COMPANY, List.of(new SupplierQneSyncRepository.IdRepair(code, "guid-repaired")));

        assertThat(supplierRow(id).get("QNEId")).isEqualTo("guid-repaired");
    }

    // ─── lookups and routes ─────────────────────────────────────────────

    @Test
    @DisplayName("the MSIC and Self Billed combos load labelled rows")
    void lookupsLoad() {
        List<SupplierLookupOption> msic = service.msicCodes();
        List<SupplierLookupOption> selfBilled = service.selfBilledTypes();

        assertThat(msic).isNotEmpty().allMatch(o -> o.id() != null && o.label() != null && !o.label().isBlank());
        assertThat(selfBilled).isNotEmpty().allMatch(o -> o.id() != null && o.label() != null && !o.label().isBlank());
    }

    @Test
    @DisplayName("every endpoint the React screens call is mapped")
    void screenEndpointsAreMapped() {
        Set<String> patterns = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(e -> e.getValue().getBeanType().getSimpleName().equals("SupplierController"))
                .flatMap(e -> e.getKey().getPathPatternsCondition() == null
                        ? Stream.<String>empty()
                        : e.getKey().getPathPatternsCondition().getPatternValues().stream())
                .collect(Collectors.toSet());

        assertThat(patterns).contains(
                "/api/suppliers",
                "/api/suppliers/{id}",
                "/api/suppliers/search",
                "/api/suppliers/qne/sync",
                "/api/suppliers/msic-codes",
                "/api/suppliers/self-billed-types",
                "/api/suppliers/{id}/soft-delete");
    }

    // ─── helpers ────────────────────────────────────────────────────────

    private SupplierDto newSupplier(String name, String type) {
        SupplierDto dto = new SupplierDto();
        dto.setCompanyRefId(COMPANY);
        dto.setSupplierName(name);
        dto.setSupplierType(type);
        dto.setSymbolRefid(symbolId);
        dto.setPaymentTermsRefid(paymentTermsId);
        dto.setSelfBilled(0);
        dto.setMsicCodeRefId(0);
        dto.setActive(1);
        return dto;
    }

    private SupplierGridPage search(java.util.function.Consumer<SupplierGridRequest> criteria) {
        SupplierGridRequest request = new SupplierGridRequest();
        request.setCompanyId(COMPANY);
        request.setSize(500);
        criteria.accept(request);
        return service.search(request);
    }

    private Map<String, Object> supplierRow(int id) {
        return jdbc.queryForMap("SELECT * FROM Supplier WHERE Id = :id", new MapSqlParameterSource("id", id));
    }

    private int accountSequence(SupplierDto supplier) {
        String code = jdbc.queryForObject("SELECT AccountCode FROM AccountsGroupMaster WHERE Id = :id",
                new MapSqlParameterSource("id", supplier.getAccountRefid()), String.class);
        return Integer.parseInt(code.substring("SUP-".length()));
    }

    private static SupplierListRow findRow(SupplierGridPage page, int id) {
        return page.items().stream().filter(r -> r.id() == id).findFirst()
                .orElseThrow(() -> new AssertionError("supplier " + id + " is not in the grid"));
    }

    private static List<Integer> ids(SupplierGridPage page) {
        return page.items().stream().map(SupplierListRow::id).toList();
    }
}

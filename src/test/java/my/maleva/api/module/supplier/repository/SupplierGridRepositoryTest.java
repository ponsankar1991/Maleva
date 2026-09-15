package my.maleva.api.module.supplier.repository;

import my.maleva.api.module.supplier.dto.SupplierGridRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins how the grid search turns a request into SQL, without a database: what
 * reaches the SQL text (whitelisted names only) and what is bound. Whether the
 * SQL returns the right suppliers is SupplierServiceIT's job.
 */
class SupplierGridRepositoryTest {

    private static SupplierGridRequest request() {
        SupplierGridRequest request = new SupplierGridRequest();
        request.setCompanyId(6);
        return request;
    }

    @Test
    @DisplayName("an empty search lists the company's suppliers, newest first, never the deleted")
    void defaultSearch() {
        SupplierGridRepository.Query query = SupplierGridRepository.build(request());

        assertThat(query.selectSql()).contains("WHERE s.CompanyRefId = :comid AND s.Active <> 2");
        assertThat(query.selectSql()).contains("ORDER BY s.Id DESC OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY");
        assertThat(query.selectSql()).contains("COUNT(*) OVER () AS TotalRows");
        assertThat(query.params().getValue("comid")).isEqualTo(6);
        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(50);
    }

    @Test
    @DisplayName("the keyword is bound, never pasted into the SQL")
    void keywordIsBound() {
        SupplierGridRequest request = request();
        request.setKeyword("x' OR 1=1 --");

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);

        assertThat(query.selectSql()).doesNotContain("OR 1=1");
        assertThat(query.params().getValue("keyword")).isEqualTo("%x' OR 1=1 --%");
        assertThat(query.selectSql()).contains("s.SupplierName LIKE :keyword ESCAPE '\\'");
        assertThat(query.selectSql()).contains("ag.AccountCode LIKE :keyword");
        assertThat(query.selectSql()).contains("s.QNECode LIKE :keyword");
    }

    @Test
    @DisplayName("a numeric keyword also finds the supplier by its id")
    void numericKeywordMatchesId() {
        SupplierGridRequest request = request();
        request.setKeyword("137");

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);

        assertThat(query.selectSql()).contains("s.Id = :keywordId");
        assertThat(query.params().getValue("keywordId")).isEqualTo(137);
    }

    @Test
    @DisplayName("LIKE wildcards typed by the user are literal characters")
    void wildcardsAreEscaped() {
        assertThat(SupplierGridRepository.containsPattern("100%_[x\\")).isEqualTo("%100\\%\\_\\[x\\\\%");
    }

    @Test
    @DisplayName("a sort that is not a grid column falls back to the default instead of reaching the SQL")
    void sortIsWhitelisted() {
        SupplierGridRequest request = request();
        request.setSortBy("SupplierName; DROP TABLE Supplier --");
        request.setSortDir("asc");

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);

        assertThat(query.selectSql()).doesNotContain("DROP").contains("ORDER BY s.Id ASC");
    }

    @Test
    @DisplayName("a known column sorts, with the id as a stable tie-breaker")
    void knownColumnSorts() {
        SupplierGridRequest request = request();
        request.setSortBy("symbolName");
        request.setSortDir("asc");

        assertThat(SupplierGridRepository.build(request).selectSql()).contains("ORDER BY sm.SName ASC, s.Id DESC");
    }

    @Test
    @DisplayName("type ALL filters nothing; any other type is exact, as legacy SelectSupplier")
    void typeFilter() {
        SupplierGridRequest all = request();
        all.setType("ALL");
        assertThat(SupplierGridRepository.build(all).selectSql()).doesNotContain(":type");

        SupplierGridRequest vendor = request();
        vendor.setType("VENDOR");
        SupplierGridRepository.Query query = SupplierGridRepository.build(vendor);
        assertThat(query.selectSql()).contains("AND s.SupplierType = :type");
        assertThat(query.params().getValue("type")).isEqualTo("VENDOR");
    }

    @Test
    @DisplayName("the active filter maps to Active = 1 / 0 and anything else to both")
    void activeFilter() {
        SupplierGridRequest active = request();
        active.setActive("active");
        assertThat(SupplierGridRepository.build(active).selectSql()).contains("AND s.Active = 1");

        SupplierGridRequest inactive = request();
        inactive.setActive("inactive");
        assertThat(SupplierGridRepository.build(inactive).selectSql()).contains("AND s.Active = 0");

        SupplierGridRequest junk = request();
        junk.setActive("1; DELETE");
        assertThat(SupplierGridRepository.build(junk).selectSql()).doesNotContain("DELETE").doesNotContain("s.Active = 1");
    }

    @Test
    @DisplayName("each column filter narrows its own column with its own bound value")
    void columnFilters() {
        SupplierGridRequest request = request();
        request.setCity("klang");
        request.setSymbolName("MYR");
        request.setGstNo("  ");

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);

        assertThat(query.selectSql()).contains("sm.SName LIKE :filter0", "s.City LIKE :filter1");
        assertThat(query.params().getValue("filter0")).isEqualTo("%MYR%");
        assertThat(query.params().getValue("filter1")).isEqualTo("%klang%");
        // Blank filters are ignored.
        assertThat(query.selectSql()).doesNotContain("s.GSTNO LIKE :filter");
    }

    @Test
    @DisplayName("paging is clamped: no negative page, no page larger than 500 rows")
    void pagingIsClamped() {
        SupplierGridRequest request = request();
        request.setPage(-3);
        request.setSize(100_000);

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);

        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(500);
        assertThat(query.params().getValue("offset")).isEqualTo(0L);

        request.setPage(3);
        request.setSize(100);
        assertThat(SupplierGridRepository.build(request).params().getValue("offset")).isEqualTo(300L);
    }

    @Test
    @DisplayName("the count query applies the same filters as the page query")
    void countMatchesSelect() {
        SupplierGridRequest request = request();
        request.setKeyword("petron");
        request.setType("VENDOR");

        SupplierGridRepository.Query query = SupplierGridRepository.build(request);
        String where = query.selectSql().substring(query.selectSql().indexOf(" WHERE "), query.selectSql().indexOf(" ORDER BY "));

        assertThat(query.countSql()).endsWith(where);
    }
}

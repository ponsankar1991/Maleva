package my.maleva.api.module.supplier;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.service.SupplierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Save as the browser makes it: {@code create} is the FIRST statement of its
 * transaction.
 *
 * <p>SupplierServiceIT cannot catch this: it runs inside the test's transaction
 * and its setup SELECTs have already opened it. On a real request the SQL
 * Server driver's implicit transaction was not open yet when the numbering lock
 * ran, so {@code sp_getapplock} returned -999 and every Save failed with
 * "Supplier numbering needs a transaction to own its lock".
 *
 * <p>No test-managed transaction here; the save runs in a TransactionTemplate
 * that is marked rollback-only, so nothing is left in MalevanewDemo.
 */
@SpringBootTest(properties = "qne.enabled=false")
class SupplierCreateFirstStatementIT {

    private static final int COMPANY = 6;

    @Autowired private SupplierService service;
    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("a Save that opens its own transaction gets the numbering lock and creates the supplier")
    void createAsFirstStatementOfTransaction() {
        MapSqlParameterSource company = new MapSqlParameterSource("comid", COMPANY);
        // Read outside any transaction, so the save's transaction starts clean.
        int symbolId = jdbc.queryForObject(
                "SELECT TOP 1 Id FROM SymbolMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company, Integer.class);
        int paymentTermsId = jdbc.queryForObject(
                "SELECT TOP 1 Id FROM PaymentTermsMaster WHERE CompanyRefId = :comid AND Active = 1 ORDER BY Id", company, Integer.class);

        SupplierDto dto = new SupplierDto();
        dto.setCompanyRefId(COMPANY);
        dto.setSupplierName("ZZIT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + " FIRST STATEMENT");
        dto.setSupplierType("VENDOR");
        dto.setSymbolRefid(symbolId);
        dto.setPaymentTermsRefid(paymentTermsId);
        dto.setSelfBilled(0);
        dto.setMsicCodeRefId(0);
        dto.setActive(1);

        SupplierDto created = new TransactionTemplate(transactionManager).execute(status -> {
            status.setRollbackOnly();
            return service.create(dto);
        });

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getCNumberDisplay()).startsWith("SU");
    }
}

package my.maleva.api.module.dashboard.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the payable dashboard's row classes actually get populated.
 *
 * They are plain classes with public fields and no setters.
 * {@link BeanPropertyRowMapper} maps to public <em>setters</em>, so it returns
 * the right number of rows with every field null — which reads on screen as a
 * quiet day rather than as a broken query, and so goes unreported. The first
 * test pins that behaviour down; the rest prove the explicit mappers used by
 * the repository do not share it.
 */
class DashboardRowMapperTest {

    private EmbeddedDatabase db;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
        jdbc = new JdbcTemplate(db);

        jdbc.execute("""
            CREATE TABLE completed (
                Id INT, SSaleDate VARCHAR(20), CNumberDisplay VARCHAR(50),
                ExpenseName VARCHAR(200), RefNumber VARCHAR(100), Amount DOUBLE,
                Remarks VARCHAR(50), DetailedId INT, FilePath VARCHAR(200),
                TotalAmount DOUBLE)
            """);
        jdbc.update("""
            INSERT INTO completed VALUES
            (9, '05/08/2026', 'PV00009', 'SIN HOCK', 'R-9', 12000.0, 'Q1', 0, 'UTILITY', 30000.0)
            """);
        // The supplier arm of the union sends a NULL TotalAmount.
        jdbc.update("""
            INSERT INTO completed VALUES
            (10, '07/08/2026', 'PY00010', 'KWANG HENG', 'R-10', 3500.75, NULL, 1, 'VENDOR', NULL)
            """);

        jdbc.execute("""
            CREATE TABLE pending (
                Id INT, ExpenseName VARCHAR(200), SubExpenseName VARCHAR(200),
                Amount DOUBLE, DueDateOut VARCHAR(20), DueReportId INT,
                DetailedId INT, BankName VARCHAR(100), AccountNo VARCHAR(50))
            """);
        jdbc.update("""
            INSERT INTO pending VALUES
            (1, 'DIESEL', 'PETRON KLANG', 4820.5, '2026-08-24', 2, 1, 'MAYBANK', '5140')
            """);
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    /**
     * The reason the explicit mappers exist. If this ever starts failing,
     * BeanPropertyRowMapper has gained field support and the hand-written
     * mappers could be reconsidered.
     */
    @Test
    void beanPropertyRowMapperLeavesPublicFieldsNull() {
        List<DashboardRepository.CompletedPaymentRow> rows = jdbc.query(
                "SELECT * FROM completed WHERE Id = 9",
                new BeanPropertyRowMapper<>(DashboardRepository.CompletedPaymentRow.class));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).Id).isNull();
        assertThat(rows.get(0).ExpenseName).isNull();
    }

    @Test
    void completedPaymentMapperReadsEveryColumn() {
        List<DashboardRepository.CompletedPaymentRow> rows = jdbc.query(
                "SELECT * FROM completed ORDER BY Id",
                DashboardRepository.completedPaymentMapper());

        assertThat(rows).hasSize(2);

        DashboardRepository.CompletedPaymentRow voucher = rows.get(0);
        assertThat(voucher.Id).isEqualTo(9);
        assertThat(voucher.SSaleDate).isEqualTo("05/08/2026");
        assertThat(voucher.CNumberDisplay).isEqualTo("PV00009");
        assertThat(voucher.ExpenseName).isEqualTo("SIN HOCK");
        assertThat(voucher.RefNumber).isEqualTo("R-9");
        assertThat(voucher.Amount).isEqualTo(12000.0);
        assertThat(voucher.Remarks).isEqualTo("Q1");
        assertThat(voucher.DetailedId).isZero();
        assertThat(voucher.FilePath).isEqualTo("UTILITY");
        assertThat(voucher.TotalAmount).isEqualTo(30000.0);

        // A NULL must stay null, not collapse to 0.0 via ResultSet.getDouble.
        DashboardRepository.CompletedPaymentRow supplier = rows.get(1);
        assertThat(supplier.DetailedId).isEqualTo(1);
        assertThat(supplier.TotalAmount).isNull();
        assertThat(supplier.Remarks).isNull();
    }

    @Test
    void pendingPaymentMapperReadsEveryColumn() {
        List<DashboardRepository.PendingPaymentRow> rows = jdbc.query(
                "SELECT * FROM pending", DashboardRepository.pendingPaymentMapper());

        assertThat(rows).hasSize(1);
        DashboardRepository.PendingPaymentRow row = rows.get(0);
        assertThat(row.Id).isEqualTo(1);
        assertThat(row.ExpenseName).isEqualTo("DIESEL");
        assertThat(row.SubExpenseName).isEqualTo("PETRON KLANG");
        assertThat(row.Amount).isEqualTo(4820.5);
        // The union aliases both arms' dates to DueDateOut; the mapper reads that.
        assertThat(row.DueDate).isEqualTo("2026-08-24");
        // 2 = past due; the dashboard paints this row red and alerts on it.
        assertThat(row.DueReportId).isEqualTo(2);
        assertThat(row.DetailedId).isEqualTo(1);
        assertThat(row.BankName).isEqualTo("MAYBANK");
        assertThat(row.AccountNo).isEqualTo("5140");
    }
}

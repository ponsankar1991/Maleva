package my.maleva.api.module.supplier.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The reads and the id repair behind "Update from QNE" (legacy
 * {@code UpdateSupplierId}). One query per lookup table, loaded once per sync
 * — legacy matched every QNE supplier against in-memory lists too.
 */
@Repository
@RequiredArgsConstructor
public class SupplierQneSyncRepository {

    /**
     * Every supplier of the company that carries a QNE code — deleted ones
     * INCLUDED. Legacy read only {@code Active != 2}, so a supplier deleted
     * here looked unknown to the next sync and was created all over again.
     */
    static final String LINKED_SUPPLIERS_SQL =
            "SELECT Id, QNECode, QNEId FROM Supplier WITH (NOLOCK) "
            + "WHERE CompanyRefId = :comid AND QNECode IS NOT NULL AND QNECode <> ''";

    /** Legacy: {@code select Id, SName ... from SymbolMaster where CompanyRefId = @c and Active != 2}. */
    static final String SYMBOLS_SQL =
            "SELECT Id, SName AS Name FROM SymbolMaster WITH (NOLOCK) "
            + "WHERE CompanyRefId = :comid AND Active <> 2 ORDER BY Id";

    /** Legacy: {@code select Id, TermsName ... from PaymentTermsMaster where CompanyRefId = @c and Active != 2}. */
    static final String PAYMENT_TERMS_SQL =
            "SELECT Id, TermsName AS Name FROM PaymentTermsMaster WITH (NOLOCK) "
            + "WHERE CompanyRefId = :comid AND Active <> 2 ORDER BY Id";

    /** Legacy: {@code update Supplier set QNEId = '...' where QNECode = '...' and CompanyRefId = @c}. */
    static final String REPAIR_QNE_ID_SQL =
            "UPDATE Supplier SET QNEId = :qneId WHERE QNECode = :qneCode AND CompanyRefId = :comid";

    public record LinkedSupplier(int id, String qneCode, String qneId) {
    }

    public record NamedId(int id, String name) {
    }

    public record IdRepair(String qneCode, String qneId) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public List<LinkedSupplier> linkedSuppliers(int companyId) {
        return jdbc.query(LINKED_SUPPLIERS_SQL, new MapSqlParameterSource("comid", companyId),
                (rs, row) -> new LinkedSupplier(rs.getInt("Id"), rs.getString("QNECode"), rs.getString("QNEId")));
    }

    public List<NamedId> symbols(int companyId) {
        return jdbc.query(SYMBOLS_SQL, new MapSqlParameterSource("comid", companyId),
                (rs, row) -> new NamedId(rs.getInt("Id"), rs.getString("Name")));
    }

    public List<NamedId> paymentTerms(int companyId) {
        return jdbc.query(PAYMENT_TERMS_SQL, new MapSqlParameterSource("comid", companyId),
                (rs, row) -> new NamedId(rs.getInt("Id"), rs.getString("Name")));
    }

    /**
     * Writes the QNE ids in one batch rather than one call per supplier.
     *
     * @return how many repairs were sent (row counts are unavailable: the
     *         datasource runs SET NOCOUNT ON)
     */
    public int repairQneIds(int companyId, List<IdRepair> repairs) {
        if (repairs.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] batch = repairs.stream()
                .map(r -> new MapSqlParameterSource("comid", companyId)
                        .addValue("qneCode", r.qneCode())
                        .addValue("qneId", r.qneId()))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(REPAIR_QNE_ID_SQL, batch);
        return repairs.size();
    }
}

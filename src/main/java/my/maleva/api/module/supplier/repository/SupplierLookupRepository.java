package my.maleva.api.module.supplier.repository;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.supplier.dto.SupplierLookupOption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The two e-invoice lookup tables behind the supplier screen's combos, ported
 * from legacy {@code SupplierServices.GetMSICCode} and {@code GetSelfbilled}.
 * Neither table has an entity; they are global (no CompanyRefId) and read-only.
 */
@Repository
@RequiredArgsConstructor
public class SupplierLookupRepository {

    private final JdbcTemplate jdbc;

    /** {@code select Id, Description from MSICcode} — the label is the description, as legacy showed it. */
    public List<SupplierLookupOption> msicCodes() {
        return jdbc.query(
                "SELECT Id, Description FROM MSICcode WITH (NOLOCK) ORDER BY Id",
                (rs, row) -> new SupplierLookupOption(rs.getInt("Id"), rs.getString("Description")));
    }

    /** {@code select Id, Type from Selfbilled}. Supplier.SelfBilled stores the Id. */
    public List<SupplierLookupOption> selfBilledTypes() {
        return jdbc.query(
                "SELECT Id, Type FROM Selfbilled WITH (NOLOCK) ORDER BY Id",
                (rs, row) -> new SupplierLookupOption(rs.getInt("Id"), rs.getString("Type")));
    }
}

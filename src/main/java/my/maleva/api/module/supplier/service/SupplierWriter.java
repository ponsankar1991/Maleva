package my.maleva.api.module.supplier.service;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.supplier.dto.SupplierDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Map;

/**
 * Writes a supplier without calling {@code SP_Supplier} — the procedure's own
 * statements, run from Java inside the caller's transaction.
 *
 * <p>The statements are the procedure's with {@code @Variables} bound as
 * parameters: the same column lists, the same {@code UPPER(ISNULL(...))} per
 * column, {@code GETDATE()}, {@code SUSER_NAME()} and the literal {@code 'SA'}.
 * Upper-casing happens in SQL Server under the table's collation, and columns
 * the procedure never lists (OpeningBalance, QNECode, QNEId…) keep their
 * database defaults. {@code SupplierSaveParityIT} compares the rows both ways.
 *
 * <p><b>Round trips are counted.</b> The database is remote, so the cost of a
 * save is its number of calls, not the size of any one query. Everything the
 * procedure reads before writing goes in one batch, and everything it writes
 * in one more:
 * <ul>
 *   <li>insert — 2 calls (was 7): {@link #INSERT_PRECHECK_SQL}, {@link #INSERT_SQL}</li>
 *   <li>update — 2 calls (was 5): {@link #UPDATE_PRECHECK_SQL}, {@link #UPDATE_SQL}</li>
 *   <li>soft delete — 1 call: {@link #SOFT_DELETE_SQL}</li>
 * </ul>
 *
 * <p><b>Numbering is serialised per company.</b> {@code MAX(CNumber) + 1} and
 * {@code SUP-<children + 1>} are read-then-write; two saves at the same moment
 * both read 318 and both write SU000000319. The pre-check batch takes
 * {@code sp_getapplock} on {@code SupplierNumber:<company>}, owned by the
 * transaction, so the second save waits a few milliseconds for the first to
 * commit and then reads 319. A save that cannot get the lock is refused, never
 * written unguarded. (The legacy .NET screen does not take this lock, so a save
 * there racing one here is still unguarded until it is retired.)
 *
 * <p>Deliberately different from the procedure, each because the original was a fault:
 * <ul>
 *   <li>A company with no SUPPLIERS/SUP group is refused; the procedure created
 *       an account under a NULL parent.</li>
 *   <li>A failed master check says so; the procedure's
 *       {@code 'Symbol Master Not Found Issue id' + @SymbolRefid} raised a
 *       varchar-to-int conversion error instead.</li>
 *   <li>An edit with no Active flag keeps the stored one; the procedure failed
 *       the NOT NULL column.</li>
 *   <li>The .NET caller stripped every {@code '} from the payload and replaced
 *       the text {@code null} anywhere in it; nothing is stripped here.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SupplierWriter {

    static final int LOCK_TIMEOUT_MS = 15_000;

    /** {@code sp_getapplock}'s answer when there is no transaction to own the lock. */
    private static final int LOCK_NEEDS_TRANSACTION = -999;

    private static final String SYMBOL_FOUND =
            "(SELECT COUNT(*) FROM SymbolMaster WITH (NOLOCK) "
            + "WHERE CompanyRefId = :comid AND Id = :symbolRefid AND Active = 1) AS SymbolFound";

    private static final String PAYMENT_TERMS_FOUND =
            "(SELECT COUNT(*) FROM PaymentTermsMaster WITH (NOLOCK) "
            + "WHERE CompanyRefId = :comid AND Id = :paymentTermsRefid AND Active = 1) AS PaymentTermsFound";

    /**
     * The lock, then everything the procedure reads before its inserts:
     * {@code @Parentid}, the count behind {@code @RowNumber}, {@code @codeNew},
     * and the two master checks.
     */
    static final String INSERT_PRECHECK_SQL =
            "SET NOCOUNT ON; "
            + "DECLARE @lock int; "
            + "EXEC @lock = sp_getapplock @Resource = :lockKey, @LockMode = 'Exclusive', "
            + "@LockOwner = 'Transaction', @LockTimeout = " + LOCK_TIMEOUT_MS + "; "
            + "DECLARE @parentId int = (SELECT TOP 1 Id FROM AccountsGroupMaster WITH (NOLOCK) "
            + "WHERE AccountName = 'SUPPLIERS' AND AccountCode = 'SUP' AND CompanyRefId = :comid AND Active = 1); "
            + "SELECT @lock AS LockStatus, @parentId AS ParentId, "
            + "(SELECT COUNT(*) FROM AccountsGroupMaster WITH (NOLOCK) "
            + "WHERE ParentId = @parentId AND CompanyRefId = :comid) AS Siblings, "
            + "(SELECT ISNULL(MAX(CNumber), 0) FROM Supplier WHERE CompanyRefId = :comid) AS MaxNumber, "
            + SYMBOL_FOUND + ", " + PAYMENT_TERMS_FOUND;

    static final String INSERT_ACCOUNT_SQL =
            "INSERT INTO AccountsGroupMaster (CompanyRefId, AccountName, ParentId, Editmode, NoChild, "
            + "Created_Date, Modified_Date, Modified_By, Active, AccountCode) "
            + "VALUES (:comid, UPPER(:supplierName), :parentId, 1, 1, GETDATE(), GETDATE(), SUSER_NAME(), 1, :rowNumber)";

    static final String INSERT_SUPPLIER_SQL =
            "INSERT INTO Supplier (CompanyRefId, AccountRefid, SupplierName, CNumberDisplay, CNumber, SupplierType, "
            + "Address1, Address2, Address3, PersonId, City, State, Zipcode, Country, SymbolRefid, PaymentTermsRefid, "
            + "GSTNO, Email, MobileNo, UserName, Password, Latitude, longitude, TokenId, "
            + "OEmail, OEmail1, OName, OPhone, AEmail, AEmail1, AName, APhone, "
            + "Active, Created_Date, Modified_Date, Modified_By, "
            + "TinNo, SSTNo, MsicCode, ServiceTaxType, BankName, AccountNo, "
            + "SelfBilled, TinType, SupplierTin, MSICCodeRefId, TaxExemptionNo, ExpiryDate, TaxExemptionDetails, "
            + "RegistrationNo, SupplierCity) "
            + "VALUES (:comid, @accountRefid, UPPER(:supplierName), :code, :codeNew, UPPER(:supplierType), "
            + "UPPER(ISNULL(:address1, '')), UPPER(ISNULL(:address2, '')), UPPER(ISNULL(:address3, '')), "
            + "UPPER(ISNULL(:personId, '')), UPPER(ISNULL(:city, '')), UPPER(ISNULL(:state, '')), "
            + "ISNULL(:zipcode, ''), UPPER(ISNULL(:country, '')), :symbolRefid, :paymentTermsRefid, "
            + "ISNULL(:gstNo, ''), ISNULL(:email, ''), ISNULL(:mobileNo, ''), UPPER(ISNULL(:userName, '')), "
            + "ISNULL(:password, ''), ISNULL(:latitude, ''), ISNULL(:longitude, ''), ISNULL(:tokenId, ''), "
            + "ISNULL(:oEmail, ''), ISNULL(:oEmail1, ''), UPPER(ISNULL(:oName, '')), ISNULL(:oPhone, ''), "
            + "ISNULL(:aEmail, ''), ISNULL(:aEmail1, ''), UPPER(ISNULL(:aName, '')), ISNULL(:aPhone, ''), "
            + "1, GETDATE(), GETDATE(), 'SA', "
            + ":tinNo, :sstNo, :msicCode, :serviceTaxType, :bankName, :accountNo, "
            + ":selfBilled, :tinType, :supplierTin, :msicCodeRefId, :taxExemptionNo, :expiryDate, "
            + ":taxExemptionDetails, :registrationNo, :supplierCity)";

    /** The procedure's two inserts, chained by {@code Scope_identity()} as it does, in one call. */
    static final String INSERT_SQL =
            "SET NOCOUNT ON; "
            + INSERT_ACCOUNT_SQL + "; "
            + "DECLARE @accountRefid int = CAST(SCOPE_IDENTITY() AS int); "
            + INSERT_SUPPLIER_SQL + "; "
            + "SELECT @accountRefid AS AccountRefid, CAST(SCOPE_IDENTITY() AS int) AS SupplierId";

    /** Ownership (the procedure had none) and the two master checks, before anything is written. */
    static final String UPDATE_PRECHECK_SQL =
            "SELECT (SELECT COUNT(*) FROM Supplier WHERE Id = :id AND CompanyRefId = :comid) AS SupplierFound, "
            + SYMBOL_FOUND + ", " + PAYMENT_TERMS_FOUND;

    static final String UPDATE_SUPPLIER_SQL =
            "UPDATE Supplier SET "
            + "CompanyRefId = :comid, "
            + "SupplierName = UPPER(:supplierName), "
            + "SupplierType = UPPER(:supplierType), "
            + "Address1 = UPPER(ISNULL(:address1, '')), "
            + "Address2 = UPPER(ISNULL(:address2, '')), "
            + "Address3 = UPPER(ISNULL(:address3, '')), "
            + "PersonId = UPPER(ISNULL(:personId, '')), "
            + "City = UPPER(ISNULL(:city, '')), "
            + "State = UPPER(ISNULL(:state, '')), "
            + "Zipcode = UPPER(ISNULL(:zipcode, '')), "
            + "Country = UPPER(ISNULL(:country, '')), "
            + "SymbolRefid = :symbolRefid, "
            + "PaymentTermsRefid = :paymentTermsRefid, "
            + "GSTNO = UPPER(ISNULL(:gstNo, '')), "
            + "Email = ISNULL(:email, ''), "
            + "MobileNo = ISNULL(:mobileNo, ''), "
            + "UserName = ISNULL(:userName, ''), "
            + "Password = :password, "
            + "OEmail = ISNULL(:oEmail, ''), "
            + "OEmail1 = ISNULL(:oEmail1, ''), "
            + "OName = UPPER(ISNULL(:oName, '')), "
            + "OPhone = ISNULL(:oPhone, ''), "
            + "AEmail = ISNULL(:aEmail, ''), "
            + "AEmail1 = ISNULL(:aEmail1, ''), "
            + "AName = UPPER(ISNULL(:aName, '')), "
            + "APhone = ISNULL(:aPhone, ''), "
            + "Active = ISNULL(:active, Active), "
            + "TinNo = :tinNo, "
            + "SSTNo = :sstNo, "
            + "MsicCode = :msicCode, "
            + "ServiceTaxType = :serviceTaxType, "
            + "BankName = :bankName, "
            + "AccountNo = :accountNo, "
            + "SelfBilled = :selfBilled, "
            + "TinType = :tinType, "
            + "SupplierTin = :supplierTin, "
            + "MSICCodeRefId = :msicCodeRefId, "
            + "TaxExemptionNo = :taxExemptionNo, "
            + "ExpiryDate = :expiryDate, "
            + "TaxExemptionDetails = :taxExemptionDetails, "
            + "RegistrationNo = :registrationNo, "
            + "SupplierCity = :supplierCity, "
            + "Modified_Date = GETDATE() "
            + "WHERE Id = :id";

    /** {@code set @AccountRefid = (select top 1 AccountRefid ...)} and the rename, as one statement. */
    static final String RENAME_ACCOUNT_SQL =
            "UPDATE AccountsGroupMaster SET AccountName = UPPER(:supplierName) "
            + "WHERE Id = (SELECT TOP 1 AccountRefid FROM Supplier WHERE Id = :id AND CompanyRefId = :comid) "
            + "AND CompanyRefId = :comid";

    static final String UPDATE_SQL = "SET NOCOUNT ON; " + UPDATE_SUPPLIER_SQL + "; " + RENAME_ACCOUNT_SQL;

    /**
     * Legacy {@code DeleteSupplier}, word for word: only Active changes. The
     * affected-row count comes back as a SELECT because this datasource runs
     * {@code SET NOCOUNT ON}, which makes every update count read -1.
     */
    static final String SOFT_DELETE_SQL =
            "SET NOCOUNT ON; "
            + "UPDATE Supplier SET Active = 2 WHERE Id = :id AND CompanyRefId = :comid; "
            + "SELECT @@ROWCOUNT AS Affected";

    /** Legacy UpdateSupplierId, after SP_Supplier created a supplier pulled from QNE. */
    static final String LINK_TO_QNE_SQL =
            "UPDATE Supplier SET QNECode = :qneCode, QNEId = :qneId WHERE Id = :id AND CompanyRefId = :comid";

    private static final String CNUMBER_PREFIX = "SU";
    private static final int CNUMBER_WIDTH = 9;

    /** {@code if @ExpiryDate = ''} on a date variable compares against the zero date. */
    private static final LocalDate ZERO_DATE = LocalDate.of(1900, 1, 1);

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * The procedure's {@code @Id = 0} branch. Must run inside a transaction:
     * the numbering lock is owned by it and released at commit.
     *
     * @return the new Supplier.Id
     */
    public int insert(SupplierDto dto, Integer companyId) {
        MapSqlParameterSource params = supplierParameters(dto, companyId)
                .addValue("lockKey", "SupplierNumber:" + companyId, Types.VARCHAR);

        Map<String, Object> checks = jdbc.queryForMap(INSERT_PRECHECK_SQL, params);
        requireNumberingLock(checks);

        Object parentId = checks.get("ParentId");
        if (parentId == null) {
            throw new InvalidRequestException(
                    "This company has no active SUPPLIERS (SUP) account group, so a supplier account cannot be created.");
        }
        requireMasters(checks, dto);

        int codeNew = intOf(checks.get("MaxNumber")) + 1;
        params.addValue("parentId", intOf(parentId), Types.INTEGER)
              .addValue("rowNumber", "SUP-" + (intOf(checks.get("Siblings")) + 1), Types.VARCHAR)
              .addValue("code", formatCNumber(codeNew), Types.VARCHAR)
              .addValue("codeNew", codeNew, Types.INTEGER);

        Object supplierId = jdbc.queryForMap(INSERT_SQL, params).get("SupplierId");
        if (supplierId == null) {
            throw new IllegalStateException("The Supplier insert returned no Id");
        }
        return intOf(supplierId);
    }

    /** The procedure's ELSE branch: update the supplier, then rename its account. */
    public void update(int supplierId, SupplierDto dto, Integer companyId) {
        MapSqlParameterSource params = supplierParameters(dto, companyId)
                .addValue("id", supplierId, Types.INTEGER);

        Map<String, Object> checks = jdbc.queryForMap(UPDATE_PRECHECK_SQL, params);
        if (intOf(checks.get("SupplierFound")) == 0) {
            throw new InvalidRequestException("Supplier " + supplierId + " was not found for this company.");
        }
        requireMasters(checks, dto);

        jdbc.update(UPDATE_SQL, params);
    }

    /**
     * Legacy {@code DeleteSupplier}: {@code Active = 2}, nothing else.
     *
     * @return false when no supplier with that id belongs to the company
     */
    public boolean softDelete(int supplierId, Integer companyId) {
        Integer affected = jdbc.queryForObject(SOFT_DELETE_SQL,
                new MapSqlParameterSource("id", supplierId).addValue("comid", companyId), Integer.class);
        return affected != null && affected > 0;
    }

    /** Stores the QNE identity of a supplier that was created from QNE's list. */
    public void linkToQne(int supplierId, Integer companyId, String qneId, String qneCode) {
        jdbc.update(LINK_TO_QNE_SQL, new MapSqlParameterSource("id", supplierId)
                .addValue("comid", companyId)
                .addValue("qneId", qneId, Types.VARCHAR)
                .addValue("qneCode", qneCode, Types.VARCHAR));
    }

    // ─── Steps ──────────────────────────────────────────────────────────

    private static void requireNumberingLock(Map<String, Object> checks) {
        int status = intOf(checks.get("LockStatus"));
        if (status == LOCK_NEEDS_TRANSACTION) {
            throw new IllegalStateException("Supplier numbering needs a transaction to own its lock");
        }
        if (status < 0) {
            // -1 timeout, -2 cancelled, -3 deadlock victim: another save held the number.
            throw new InvalidRequestException("Another supplier is being saved for this company. Please try again.");
        }
    }

    /** {@code If @SymbolRefid <> 0 ...} and {@code If @PaymentTermsRefid <> 0 ...}; a NULL skips, as {@code <>} does. */
    private static void requireMasters(Map<String, Object> checks, SupplierDto dto) {
        Integer symbolId = dto.getSymbolRefid();
        if (symbolId != null && symbolId != 0 && intOf(checks.get("SymbolFound")) == 0) {
            throw new InvalidRequestException("Symbol Master Not Found Issue id " + symbolId);
        }
        Integer termsId = dto.getPaymentTermsRefid();
        if (termsId != null && termsId != 0 && intOf(checks.get("PaymentTermsFound")) == 0) {
            throw new InvalidRequestException("Payment Terms Master Not Found Issue id " + termsId);
        }
    }

    /** {@code 'SU' + RIGHT('000000000' + cast(@codeNew as varchar(50)), 9)}. */
    static String formatCNumber(int number) {
        String padded = "000000000" + number;
        return CNUMBER_PREFIX + padded.substring(padded.length() - CNUMBER_WIDTH);
    }

    /**
     * Every {@code @Variable} the OPENJSON block declared, typed explicitly so a
     * null binds as a typed NULL — the procedure's variables were typed too.
     */
    private static MapSqlParameterSource supplierParameters(SupplierDto dto, Integer companyId) {
        return new MapSqlParameterSource()
                .addValue("comid", companyId, Types.INTEGER)
                .addValue("supplierName", dto.getSupplierName(), Types.VARCHAR)
                .addValue("supplierType", dto.getSupplierType(), Types.VARCHAR)
                .addValue("address1", dto.getAddress1(), Types.VARCHAR)
                .addValue("address2", dto.getAddress2(), Types.VARCHAR)
                .addValue("address3", dto.getAddress3(), Types.VARCHAR)
                .addValue("personId", dto.getPersonId(), Types.VARCHAR)
                .addValue("city", dto.getCity(), Types.VARCHAR)
                .addValue("state", dto.getState(), Types.VARCHAR)
                .addValue("zipcode", dto.getZipcode(), Types.VARCHAR)
                .addValue("country", dto.getCountry(), Types.VARCHAR)
                .addValue("symbolRefid", dto.getSymbolRefid(), Types.INTEGER)
                .addValue("paymentTermsRefid", dto.getPaymentTermsRefid(), Types.INTEGER)
                .addValue("gstNo", dto.getGstNo(), Types.VARCHAR)
                .addValue("email", dto.getEmail(), Types.VARCHAR)
                .addValue("mobileNo", dto.getMobileNo(), Types.VARCHAR)
                .addValue("userName", dto.getUserName(), Types.VARCHAR)
                .addValue("password", dto.getPassword(), Types.VARCHAR)
                .addValue("latitude", dto.getLatitude(), Types.VARCHAR)
                .addValue("longitude", dto.getLongitude(), Types.VARCHAR)
                .addValue("tokenId", dto.getTokenId(), Types.VARCHAR)
                .addValue("oEmail", dto.getOEmail(), Types.VARCHAR)
                .addValue("oEmail1", dto.getOEmail1(), Types.VARCHAR)
                .addValue("oName", dto.getOName(), Types.VARCHAR)
                .addValue("oPhone", dto.getOPhone(), Types.VARCHAR)
                .addValue("aEmail", dto.getAEmail(), Types.VARCHAR)
                .addValue("aEmail1", dto.getAEmail1(), Types.VARCHAR)
                .addValue("aName", dto.getAName(), Types.VARCHAR)
                .addValue("aPhone", dto.getAPhone(), Types.VARCHAR)
                .addValue("active", dto.getActive(), Types.INTEGER)
                .addValue("tinNo", dto.getTinNo(), Types.VARCHAR)
                .addValue("sstNo", dto.getSstNo(), Types.VARCHAR)
                .addValue("msicCode", dto.getMsicCode(), Types.VARCHAR)
                .addValue("serviceTaxType", dto.getServiceTaxType(), Types.VARCHAR)
                .addValue("bankName", dto.getBankName(), Types.VARCHAR)
                .addValue("accountNo", dto.getAccountNo(), Types.VARCHAR)
                .addValue("selfBilled", dto.getSelfBilled(), Types.INTEGER)
                .addValue("tinType", dto.getTinType(), Types.VARCHAR)
                .addValue("supplierTin", dto.getSupplierTin(), Types.VARCHAR)
                .addValue("msicCodeRefId", dto.getMsicCodeRefId(), Types.INTEGER)
                .addValue("taxExemptionNo", dto.getTaxExemptionNo(), Types.VARCHAR)
                .addValue("expiryDate", expiryDate(dto.getExpiryDate()), Types.DATE)
                .addValue("taxExemptionDetails", dto.getTaxExemptionDetails(), Types.VARCHAR)
                .addValue("registrationNo", dto.getRegistrationNo(), Types.VARCHAR)
                .addValue("supplierCity", dto.getSupplierCity(), Types.VARCHAR);
    }

    /** {@code if @ExpiryDate = '' set @ExpiryDate = null}. */
    private static Date expiryDate(LocalDate value) {
        return value == null || ZERO_DATE.equals(value) ? null : Date.valueOf(value);
    }

    private static int intOf(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}

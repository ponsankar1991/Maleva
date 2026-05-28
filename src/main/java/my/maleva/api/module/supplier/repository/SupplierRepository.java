package my.maleva.api.module.supplier.repository;

import my.maleva.api.module.supplier.dto.SupplierComboList;
import my.maleva.api.module.supplier.dto.SupplierExtendedResponse;
import my.maleva.api.module.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SupplierRepository - Repository for Supplier
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    /**
     * Find all Supplier records by company ID
     */
    List<Supplier> findByCompanyRefId(Integer companyRefId);

    /**
     * Find Supplier by name
     */
    Optional<Supplier> findBySupplierName(String supplierName);

    /**
     * Find Supplier by C Number and Company
     */
    Optional<Supplier> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find all active Supplier records by company
     */
    List<Supplier> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find all Supplier records by supplier type
     */
    List<Supplier> findBySupplierType(String supplierType);

    /**
     * Find Supplier by email
     */
    Optional<Supplier> findByEmail(String email);

    /**
     * Find all Supplier records by country
     */
    List<Supplier> findByCountry(String country);

    /**
     * Find all Supplier records by city
     */
    List<Supplier> findByCity(String city);

    /**
     * Count Supplier records by company ID
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active Supplier records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if supplier name exists
     */
    boolean existsBySupplierName(String supplierName);

    /**
     * Find Supplier by GST No
     */
    Optional<Supplier> findByGstNo(String gstNo);

    /**
     * Search suppliers by supplier name with keyword filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierName LIKE %:keyword% ORDER BY s.id")
    List<Supplier> searchBySupplierName(@Param("comid") Integer comid, @Param("keyword") String keyword);

    /**
     * Search suppliers by supplier name with type filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierType = :type AND s.supplierName LIKE %:keyword% ORDER BY s.id")
    List<Supplier> searchBySupplierNameWithType(@Param("comid") Integer comid,
                                                 @Param("keyword") String keyword,
                                                 @Param("type") String type);

    /**
     * Search suppliers by mobile number with keyword filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.mobileNo LIKE %:keyword% ORDER BY s.id")
    List<Supplier> searchByMobileNo(@Param("comid") Integer comid, @Param("keyword") String keyword);

    /**
     * Search suppliers by mobile number with type filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierType = :type AND s.mobileNo LIKE %:keyword% ORDER BY s.id")
    List<Supplier> searchByMobileNoWithType(@Param("comid") Integer comid,
                                             @Param("keyword") String keyword,
                                             @Param("type") String type);

    /**
     * Search suppliers by ID
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.id = :id ORDER BY s.id")
    List<Supplier> searchById(@Param("comid") Integer comid, @Param("id") Integer id);

    /**
     * Search suppliers by ID with type filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierType = :type AND s.id = :id ORDER BY s.id")
    List<Supplier> searchByIdWithType(@Param("comid") Integer comid,
                                       @Param("id") Integer id,
                                       @Param("type") String type);

    /**
     * Get all suppliers for a company without keyword filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "ORDER BY s.id")
    List<Supplier> findAllByCompanyId(@Param("comid") Integer comid);

    /**
     * Get all suppliers for a company with type filter
     */
    @Query("SELECT s FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierType = :type ORDER BY s.id")
    List<Supplier> findAllByCompanyIdWithType(@Param("comid") Integer comid, @Param("type") String type);

    /**
     * Count suppliers by company
     */
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2")
    Integer countByCompany(@Param("comid") Integer comid);

    /**
     * Count suppliers by company with type filter
     */
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.companyRefId = :comid AND s.active != 2 " +
           "AND s.supplierType = :type")
    Integer countByCompanyWithType(@Param("comid") Integer comid, @Param("type") String type);

    /**
     * Get supplier combo list (Id and AccountName) by company ID
     * Active = 1 and CompanyRefId = comid
     * Used for GetSupplier endpoint
     */
    @Query("SELECT new my.maleva.api.module.supplier.dto.SupplierComboList(s.id, s.supplierName, " +
           "CONCAT(s.supplierName, '-', s.mobileNo)) FROM Supplier s " +
           "WHERE s.companyRefId = :comid AND s.active = 1 ORDER BY s.id")
    List<SupplierComboList> getSupplierComboList(@Param("comid") Integer comid);

    /**
     * Get supplier combo list (Id and AccountName) by company ID with type filter
     * Active = 1 and CompanyRefId = comid and SupplierType = type
     * Used for GetSupplier endpoint with type filter
     */
    @Query("SELECT new my.maleva.api.module.supplier.dto.SupplierComboList(s.id, s.supplierName, " +
           "CONCAT(s.supplierName, '-', s.mobileNo)) FROM Supplier s " +
           "WHERE s.companyRefId = :comid AND s.active = 1 AND " +
           "(s.supplierType = :type OR :type = 'ALL') ORDER BY s.id")
    List<SupplierComboList> getSupplierComboListWithType(@Param("comid") Integer comid, @Param("type") String type);

    /**
     * Fetch all suppliers with joined master data
     * Equivalent to .NET SelectSupplierAll method
     *
     * Joins:
     * - SymbolMaster (SName)
     * - PaymentTermsMaster (TermsName)
     * - AccountsGroupMaster (AccountCode)
     *
     * Filters:
     * - CompanyRefId = comid
     * - Active != 2
     *
     * Sorted by SupplierName
     *
     * @param comid Company Reference ID
     * @return List of SupplierExtendedResponse with joined master data
     */
    @Query(value = "SELECT " +
            "S.Id, S.CompanyRefId, S.SupplierName, S.CNumberDisplay, S.CNumber, " +
            "S.Address1, S.Address2, S.Address3, S.City, S.State, S.Zipcode, S.Country, " +
            "S.SupplierCity, S.SymbolRefid, S.PaymentTermsRefid, S.GSTNO, S.Email, " +
            "S.OEmail, S.OEmail1, S.AEmail, S.AEmail1, S.MobileNo, S.OPhone, S.APhone, " +
            "S.UserName, S.Password, S.Latitude, S.longitude, S.TokenId, S.OName, S.AName, " +
            "S.PersonId, S.Active, S.Created_Date, S.Modified_Date, S.Modified_By, " +
            "S.SupplierType, S.AccountRefid, S.TinNo, S.SSTNo, S.MsicCode, " +
            "S.ServiceTaxType, S.BankName, S.AccountNo, S.QNECode, S.QNEId, " +
            "S.SelfBilled, S.TinType, S.SupplierTin, S.MSICCodeRefId, " +
            "S.TaxExemptionNo, S.TaxExemptionDetails, S.RegistrationNo, " +
            "ISNULL(SM.SName, '') AS SName, " +
            "ISNULL(PT.TermsName, '') AS TermsName, " +
            "ISNULL(AG.AccountCode, '') AS AccountCode " +
            "FROM Supplier S WITH (NOLOCK) " +
            "INNER JOIN SymbolMaster SM WITH (NOLOCK) ON S.SymbolRefid = SM.Id " +
            "INNER JOIN PaymentTermsMaster PT WITH (NOLOCK) ON S.PaymentTermsRefid = PT.Id " +
            "INNER JOIN AccountsGroupMaster AG WITH (NOLOCK) ON AG.Id = S.AccountRefid " +
            "WHERE S.CompanyRefId = :comid AND S.Active != 2 " +
            "ORDER BY S.SupplierName",
            nativeQuery = true)
    List<SupplierExtendedResponse> findAllSupplierWithMasterData(@Param("comid") Integer comid);
}

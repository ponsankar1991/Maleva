package my.maleva.api.module.supplier.service;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierSearchResponse;
import my.maleva.api.module.supplier.dto.SupplierComboList;
import my.maleva.api.module.supplier.dto.SupplierExtendedResponse;
import my.maleva.api.common.dto.ResponseViewModel;
import java.util.List;
import java.util.Optional;

/**
 * SupplierService - Business logic for Supplier
 * Handles comprehensive supplier/vendor management
 */
public interface SupplierService {

    List<SupplierDto> getByCompanyRefId(Integer companyRefId);

    Optional<SupplierDto> getBySupplierName(String supplierName);

    Optional<SupplierDto> getByCNumber(Integer cNumber, Integer companyRefId);

    List<SupplierDto> getActiveByCompany(Integer companyRefId);

    List<SupplierDto> getBySupplierType(String supplierType);

    List<SupplierDto> getByCountry(String country);

    List<SupplierDto> getByCity(String city);

    Optional<SupplierDto> getByEmail(String email);

    Optional<SupplierDto> getByGstNo(String gstNo);

    Optional<SupplierDto> getById(Integer id);

    SupplierDto create(SupplierDto dto);

    SupplierDto update(Integer id, SupplierDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompany(Integer companyRefId);

    void validateSupplierData(SupplierDto dto);

    SupplierDto activateSupplier(Integer id);

    SupplierDto deactivateSupplier(Integer id);

    boolean existsBySupplierName(String supplierName);

    SupplierDto processSupplierBatch(SupplierDto dto);

    /**
     * Select Supplier with pagination and search filters
     * Equivalent to .NET SelectSupplier method
     */
    SupplierSearchResponse selectSupplier(Integer comid, Integer startindex, Integer pageCount, 
                                          String keyword, String column, String type);

    /**
     * Get Supplier combo list for dropdowns/comboboxes
     * Equivalent to .NET GetSupplier method
     *
     * Returns: List of SupplierComboList with Id and AccountName (SupplierName + MobileNo)
     * Filters:
     * - Company ID
     * - Active = 1
     * - Optional: SupplierType filter (if type is not null, empty, or "ALL")
     *
     * @param comid Company ID
     * @param type Supplier Type filter (null/""/ALL for no type filter)
     * @return ResponseViewModel with list of SupplierComboList
     */
    ResponseViewModel getSupplier(Integer comid, String type);

    /**
     * Select All Suppliers with joined master data
     * Equivalent to .NET SelectSupplierAll method
     *
     * Fetches all suppliers for a company with joined data from:
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
     * @return List of SupplierExtendedResponse with all supplier details and joined master data
     */
    List<SupplierExtendedResponse> selectSupplierAll(Integer comid);

}

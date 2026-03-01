package my.maleva.api.service;

import my.maleva.api.dto.SupplierDto;
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
}



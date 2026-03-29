package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.SymbolMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SymbolMasterRepository - Repository for SymbolMaster
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SymbolMasterRepository extends JpaRepository<SymbolMaster, Integer> {

    /**
     * Find all SymbolMaster records by company ID
     */
    List<SymbolMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all active SymbolMaster records by company
     */
    List<SymbolMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find SymbolMaster by symbol name and company
     */
    Optional<SymbolMaster> findBySNameAndCompanyRefId(String sName, Integer companyRefId);

    /**
     * Find all SymbolMaster records by display flag
     */
    List<SymbolMaster> findByDFlag(Integer dFlag);

    /**
     * Find SymbolMaster by currency name
     */
    Optional<SymbolMaster> findByCName(String cName);

    /**
     * Find all SymbolMaster records by QNE ID
     */
    List<SymbolMaster> findByQneId(Integer qneId);

    /**
     * Count SymbolMaster records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active SymbolMaster records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if symbol name exists for company
     */
    boolean existsBySNameAndCompanyRefId(String sName, Integer companyRefId);
}


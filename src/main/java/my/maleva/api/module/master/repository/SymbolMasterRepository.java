package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.SymbolMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SymbolMasterRepository - Repository for SymbolMaster
 * Provides CRUD operations and custom query methods
 * 
 * ⚠️ CRITICAL: Uses native SQL queries to ensure correct column mapping
 * SQL Server column names are case-sensitive in some collations
 */
@Repository
public interface SymbolMasterRepository extends JpaRepository<SymbolMaster, Integer> {

    /**
     * Find all SymbolMaster records by company ID
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE companyrefid = :companyRefId", nativeQuery = true)
    List<SymbolMaster> findByCompanyRefId(@Param("companyRefId") Integer companyRefId);

    /**
     * Find all active SymbolMaster records by company
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE companyrefid = :companyRefId AND active = :active", nativeQuery = true)
    List<SymbolMaster> findByCompanyRefIdAndActive(@Param("companyRefId") Integer companyRefId, @Param("active") Integer active);

    /**
     * Find SymbolMaster by symbol name and company
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE sname = :sName AND companyrefid = :companyRefId", nativeQuery = true)
    Optional<SymbolMaster> findBySNameAndCompanyRefId(@Param("sName") String sName, @Param("companyRefId") Integer companyRefId);

    /**
     * Find all SymbolMaster records by display flag
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE dflag = :dFlag", nativeQuery = true)
    List<SymbolMaster> findByDFlag(@Param("dFlag") Integer dFlag);

    /**
     * Find SymbolMaster by currency name
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE cname = :cName", nativeQuery = true)
    Optional<SymbolMaster> findByCName(@Param("cName") String cName);

    /**
     * Find all SymbolMaster records by QNE ID
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE qneid = :qneId", nativeQuery = true)
    List<SymbolMaster> findByQneId(@Param("qneId") Integer qneId);

    /**
     * Count SymbolMaster records by company
     */
    @Query(value = "SELECT COUNT(*) FROM SymbolMaster WHERE companyrefid = :companyRefId", nativeQuery = true)
    long countByCompanyRefId(@Param("companyRefId") Integer companyRefId);

    /**
     * Count active SymbolMaster records by company
     */
    @Query(value = "SELECT COUNT(*) FROM SymbolMaster WHERE companyrefid = :companyRefId AND active = :active", nativeQuery = true)
    long countByCompanyRefIdAndActive(@Param("companyRefId") Integer companyRefId, @Param("active") Integer active);

    /**
     * Check if symbol name exists for company
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM SymbolMaster WHERE sname = :sName AND companyrefid = :companyRefId", nativeQuery = true)
    boolean existsBySNameAndCompanyRefId(@Param("sName") String sName, @Param("companyRefId") Integer companyRefId);

    /**
     * Find all non-deleted SymbolMaster records by company (Active != 2)
     * Equivalent to .NET SelectSymbol() method
     * Used for dropdowns and selection lists
     */
    @Query(value = "SELECT * FROM SymbolMaster WHERE companyrefid = :companyRefId AND active != :active", nativeQuery = true)
    List<SymbolMaster> findByCompanyRefIdAndActiveNot(@Param("companyRefId") Integer companyRefId, @Param("active") Integer active);
}


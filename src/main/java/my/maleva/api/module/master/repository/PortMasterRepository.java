package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.PortMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortMasterRepository extends JpaRepository<PortMaster, Integer> {

    /**
     * Find all port records by company and active status
     */
    List<PortMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    /**
     * Find port by company and port name
     */
    Optional<PortMaster> findByCompanyRefIdAndPortName(Integer companyRefId, String portName);

    /**
     * Find all ports by company
     */
    List<PortMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Search port by name (case-insensitive)
     */
    @Query("SELECT p FROM PortMaster p WHERE p.companyRefId = :companyId " +
            "AND LOWER(p.portName) LIKE LOWER(CONCAT('%', :portName, '%')) " +
            "AND p.active != 2")
    List<PortMaster> searchByCompanyAndPortName(
            @Param("companyId") Integer companyId,
            @Param("portName") String portName);

    /**
     * Find active ports by company
     */
    List<PortMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if port name exists for company
     */
    boolean existsByCompanyRefIdAndPortName(Integer companyRefId, String portName);
}


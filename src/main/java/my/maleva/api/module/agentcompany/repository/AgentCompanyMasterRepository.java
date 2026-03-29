package my.maleva.api.module.agentcompany.repository;

import my.maleva.api.module.agentcompany.entity.AgentCompanyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AgentCompanyMaster entity.
 * Provides custom queries based on stored procedure SP_AgentCompany logic.
 */
@Repository
public interface AgentCompanyMasterRepository extends JpaRepository<AgentCompanyMaster, Long> {

    /**
     * Find all agent companies where Active != 2 (exclude soft deleted).
     * @param active The active status to exclude (typically 2)
     * @return List of active agent companies
     */
    List<AgentCompanyMaster> findByActiveNot(Integer active);

    /**
     * Find agent companies by CompanyRefId where Active != 2.
     * @param companyRefId The company reference ID
     * @param active The active status to exclude
     * @return List of agent companies for the given company
     */
    List<AgentCompanyMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    /**
     * Find agent companies by CompanyRefId, Name, and Active status.
     * Used to check for duplicates before insert/update (from SP_AgentCompany logic).
     * @param companyRefId The company reference ID
     * @param name The name to search for
     * @param active The active status
     * @return List of matching agent companies
     */
    List<AgentCompanyMaster> findByCompanyRefIdAndNameAndActive(Integer companyRefId, String name, Integer active);

    /**
     * Find agent company by CompanyRefId and Name.
     * @param companyRefId The company reference ID
     * @param name The name to search for
     * @return Optional containing the agent company if found
     */
    Optional<AgentCompanyMaster> findByCompanyRefIdAndName(Integer companyRefId, String name);

    /**
     * Custom JPQL query to find all active agent companies for a company.
     * @param companyRefId The company reference ID
     * @return List of active agent companies
     */
    @Query("SELECT a FROM AgentCompanyMaster a WHERE a.companyRefId = :companyRefId AND a.active != 2 ORDER BY a.id ASC")
    List<AgentCompanyMaster> findAllActiveByCompanyId(@Param("companyRefId") Integer companyRefId);
}

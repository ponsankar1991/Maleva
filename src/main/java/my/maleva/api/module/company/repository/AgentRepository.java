package my.maleva.api.module.company.repository;

import my.maleva.api.module.company.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Integer> {
    List<Agent> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all agents by companyRefId where Active != 2
     * @param companyRefId The company reference ID
     * @return List of agents, sorted by agentName
     */
    @Query("SELECT a FROM Agent a WHERE a.companyRefId = :companyRefId AND a.active != 2 ORDER BY a.agentName ASC")
    List<Agent> findByCompanyRefIdActiveNot2(@Param("companyRefId") Integer companyRefId);

    /**
     * Find agents by companyRefId and agentCompanyRefId where Active != 2
     * @param companyRefId The company reference ID
     * @param agentCompanyRefId The agent company reference ID (filter)
     * @return List of agents, sorted by agentName
     */
    @Query("SELECT a FROM Agent a WHERE a.companyRefId = :companyRefId AND a.agentCompanyRefId = :agentCompanyRefId AND a.active != 2 ORDER BY a.agentName ASC")
    List<Agent> findByCompanyRefIdAndAgentCompanyRefIdActiveNot2(
            @Param("companyRefId") Integer companyRefId,
            @Param("agentCompanyRefId") Integer agentCompanyRefId
    );
}

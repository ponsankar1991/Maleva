package my.maleva.api.agentcompany.repository;

import my.maleva.api.agentcompany.entity.AgentCompanyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentCompanyMasterRepository extends JpaRepository<AgentCompanyMaster, Long> {
    // ...existing code...
}

package my.maleva.api.repo;

import my.maleva.api.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Integer> {
    List<Agent> findByCompanyRefId(Integer companyRefId);
}

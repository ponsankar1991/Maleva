package my.maleva.api.module.agent.repository;

import my.maleva.api.module.agent.entity.AgentMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentMasterRepository extends JpaRepository<AgentMaster, Integer>, JpaSpecificationExecutor<AgentMaster> {
    
    Optional<AgentMaster> findByIdAndActiveTrue(Integer id);
    
    Page<AgentMaster> findAll(Specification<AgentMaster> spec, Pageable pageable);
    
    boolean existsByLocationCodeIgnoreCase(String locationCode);
    
    boolean existsByLocationCodeIgnoreCaseAndIdNot(String locationCode, Integer id);
}

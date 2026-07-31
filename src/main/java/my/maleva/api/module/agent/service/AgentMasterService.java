package my.maleva.api.module.agent.service;

import my.maleva.api.module.agent.dto.AgentMasterCreateRequest;
import my.maleva.api.module.agent.dto.AgentMasterDto;
import my.maleva.api.module.agent.dto.AgentMasterUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AgentMasterService {
    
    AgentMasterDto createAgent(AgentMasterCreateRequest request);
    
    AgentMasterDto updateAgent(Integer id, AgentMasterUpdateRequest request);
    
    AgentMasterDto getAgentById(Integer id);
    
    Page<AgentMasterDto> getAllAgents(Pageable pageable, String search, Integer companyRefId, Boolean active);
    
    void softDeleteAgent(Integer id);
}

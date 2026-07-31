package my.maleva.api.module.agent.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.module.agent.dto.AgentMasterCreateRequest;
import my.maleva.api.module.agent.dto.AgentMasterDto;
import my.maleva.api.module.agent.dto.AgentMasterUpdateRequest;
import my.maleva.api.module.agent.entity.AgentMaster;
import my.maleva.api.module.agent.entity.AgentRole;
import my.maleva.api.module.agent.mapper.AgentMasterMapper;
import my.maleva.api.module.agent.repository.AgentMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentMasterServiceImplTest {

    @Mock
    private AgentMasterRepository repository;

    @Mock
    private AgentMasterMapper mapper;

    @InjectMocks
    private AgentMasterServiceImpl service;

    private AgentMaster entity;
    private AgentMasterDto dto;
    private AgentMasterCreateRequest createRequest;
    private AgentMasterUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        entity = new AgentMaster();
        entity.setId(1);
        entity.setCompanyRefId(10);
        entity.setAgentName("Test Agent");
        entity.setLocationCode("LOC-1");
        entity.setPhoneNumber("1234567890");
        entity.setAgentRole(AgentRole.BOTH);
        entity.setActive(true);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setCreatedBy("admin");

        dto = new AgentMasterDto(1, 10, "Test Agent", "LOC-1", "1234567890", AgentRole.BOTH, true, LocalDateTime.now(), "admin", null, null);

        createRequest = new AgentMasterCreateRequest(10, "Test Agent", "LOC-1", "1234567890", AgentRole.BOTH, true);
        
        updateRequest = new AgentMasterUpdateRequest(1, 10, "Updated Agent", "LOC-1", "0987654321", AgentRole.LOADING, true);
    }

    @Test
    void createAgent_Success() {
        when(repository.existsByLocationCodeIgnoreCase(createRequest.locationCode())).thenReturn(false);
        when(mapper.toEntity(createRequest)).thenReturn(entity);
        when(repository.save(any(AgentMaster.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        AgentMasterDto result = service.createAgent(createRequest);

        assertNotNull(result);
        assertEquals("Test Agent", result.agentName());
        verify(repository, times(1)).save(any(AgentMaster.class));
    }

    @Test
    void createAgent_DuplicateLocationCode_ThrowsInvalidRequestException() {
        when(repository.existsByLocationCodeIgnoreCase(createRequest.locationCode())).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> service.createAgent(createRequest));
        verify(repository, never()).save(any(AgentMaster.class));
    }

    @Test
    void getAgentById_Success() {
        when(repository.findByIdAndActiveTrue(1)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        AgentMasterDto result = service.getAgentById(1);

        assertNotNull(result);
        assertEquals(1, result.id());
    }

    @Test
    void getAgentById_NotFound_ThrowsEntityNotFoundException() {
        when(repository.findByIdAndActiveTrue(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getAgentById(99));
    }

    @Test
    void softDeleteAgent_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(repository.save(any(AgentMaster.class))).thenReturn(entity);

        service.softDeleteAgent(1);

        assertFalse(entity.getActive());
        verify(repository, times(1)).save(entity);
    }
}

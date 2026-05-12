package my.maleva.api.module.accounting.service;

import my.maleva.api.common.service.BaseService;
import my.maleva.api.module.accounting.dto.GLAccountsDto;
import my.maleva.api.module.accounting.mapper.GLAccountsMapper;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GLAccountsService extends BaseService<GLAccounts, GLAccountsDto, UUID> {

    private final GLAccountsMapper mapper;

    public GLAccountsService(GLAccountsRepository repository, GLAccountsMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    protected GLAccountsDto toDto(GLAccounts entity) {
        return mapper.toDto(entity);
    }

    @Override
    protected GLAccounts toEntity(GLAccountsDto dto) {
        return mapper.toEntity(dto);
    }

    @Override
    protected void updateFromDto(GLAccountsDto dto, GLAccounts entity) {
        mapper.updateFromDto(dto, entity);
    }
}

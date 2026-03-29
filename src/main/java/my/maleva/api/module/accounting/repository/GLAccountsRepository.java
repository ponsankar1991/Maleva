package my.maleva.api.module.accounting.repository;

import my.maleva.api.module.accounting.entity.GLAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GLAccountsRepository extends JpaRepository<GLAccounts, UUID> {
    List<GLAccounts> findByCompanyRefId(Integer companyRefId);
    List<GLAccounts> findByParentId(UUID parentId);
}

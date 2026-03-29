package my.maleva.api.module.accounting.repository;

import my.maleva.api.module.accounting.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByCompanyRefId(Integer companyRefId);
}

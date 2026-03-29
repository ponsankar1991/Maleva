package my.maleva.api.module.accounting.repository;

import my.maleva.api.module.accounting.entity.AccountsGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsGroupMasterRepository extends JpaRepository<AccountsGroupMaster, Integer> {
    List<AccountsGroupMaster> findByCompanyRefId(Integer companyRefId);
}

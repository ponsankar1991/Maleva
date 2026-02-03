package my.maleva.api.repo;

import my.maleva.api.model.AccountsGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsGroupMasterRepository extends JpaRepository<AccountsGroupMaster, Integer> {
    List<AccountsGroupMaster> findByCompanyRefId(Integer companyRefId);
}

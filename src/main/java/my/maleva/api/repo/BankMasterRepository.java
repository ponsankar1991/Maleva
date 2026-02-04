package my.maleva.api.repo;

import my.maleva.api.model.BankMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankMasterRepository extends JpaRepository<BankMaster, Integer> {
    List<BankMaster> findByCompanyRefId(Integer companyRefId);
}

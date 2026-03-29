package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.BankMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankMasterRepository extends JpaRepository<BankMaster, Integer> {
    List<BankMaster> findByCompanyRefId(Integer companyRefId);
}

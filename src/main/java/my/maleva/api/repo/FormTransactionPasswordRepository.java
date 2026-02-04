package my.maleva.api.repo;

import my.maleva.api.model.FormTransactionPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormTransactionPasswordRepository extends JpaRepository<FormTransactionPassword, Integer> {
    List<FormTransactionPassword> findByCompanyRefId(Integer companyRefId);
}

package my.maleva.api.module.user.repository;

import my.maleva.api.module.user.entity.FormTransactionPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormTransactionPasswordRepository extends JpaRepository<FormTransactionPassword, Integer> {
    List<FormTransactionPassword> findByCompanyRefId(Integer companyRefId);
}

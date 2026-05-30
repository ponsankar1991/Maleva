package my.maleva.api.module.accountsgroupmaster.repository;

import my.maleva.api.module.accountsgroupmaster.entity.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, UUID> {

    List<GLAccount> findByGlAccountCodeAndIsActive(String glAccountCode, Integer isActive);

    @Query(value = "SELECT S.* FROM GLAccounts S WITH (NOLOCK) " +
           "WHERE S.CompanyRefId = :companyRefId AND S.IsActive != 2",
           nativeQuery = true)
    List<GLAccount> findByCompanyRefIdAndIsActiveNot(
        @Param("companyRefId") Integer companyRefId);
}


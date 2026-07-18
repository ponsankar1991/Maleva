package my.maleva.api.module.accountsgroupmaster.repository;

import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountsGroupMasterRepository extends JpaRepository<AccountsGroupMaster, Integer> {

    @Query(value = "SELECT S.* FROM AccountsGroupMaster S WITH (NOLOCK) " +
           "WHERE S.CompanyRefId = :companyRefId AND S.Active = 1 " +
           "AND S.ParentId IN (" +
           "  SELECT SS.Id FROM AccountsGroupMaster SS WITH (NOLOCK) " +
           "  WHERE SS.AccountCode IN (:codes) " +
           "  AND SS.CompanyRefId = :companyRefId AND SS.Active = 1" +
           ")",
           nativeQuery = true)
    List<AccountsGroupMaster> findAccountsByTypeAndCompany(
        @Param("companyRefId") Integer companyRefId,
        @Param("codes") String codes);

    List<AccountsGroupMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    Optional<AccountsGroupMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);

    List<AccountsGroupMaster> findByIdInAndCompanyRefId(List<Integer> ids, Integer companyRefId);

    Optional<AccountsGroupMaster> findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive(String accountName, String accountCode, Integer companyRefId, Integer active);

    int countByParentIdAndCompanyRefId(Integer parentId, Integer companyRefId);
}


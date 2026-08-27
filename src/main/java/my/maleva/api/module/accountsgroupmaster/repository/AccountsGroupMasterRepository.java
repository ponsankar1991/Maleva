package my.maleva.api.module.accountsgroupmaster.repository;

import my.maleva.api.module.accountsgroupmaster.entity.AccountsGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface AccountsGroupMasterRepository extends JpaRepository<AccountsGroupMaster, Integer> {

    /**
     * Accounts filed under one of the given parent codes.
     *
     * <p>{@code codes} must be a collection, not a comma-joined string: bound
     * as a single String it becomes {@code IN ('AGE,SCR,CUS,...')}, one literal
     * that matches no account, which is what made the {@code PV} list come back
     * empty.
     */
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
        @Param("codes") Collection<String> codes);

    /** Every active account, for an unrecognised type — legacy applied no filter at all. */
    List<AccountsGroupMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    List<AccountsGroupMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    Optional<AccountsGroupMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);

    List<AccountsGroupMaster> findByIdInAndCompanyRefId(List<Integer> ids, Integer companyRefId);

    Optional<AccountsGroupMaster> findFirstByAccountNameAndAccountCodeAndCompanyRefIdAndActive(String accountName, String accountCode, Integer companyRefId, Integer active);

    int countByParentIdAndCompanyRefId(Integer parentId, Integer companyRefId);
}


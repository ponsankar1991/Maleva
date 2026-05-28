package my.maleva.api.module.accounting.repository;

import my.maleva.api.module.accounting.entity.AccountsGroupMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountsGroupMasterRepository extends JpaRepository<AccountsGroupMaster, Integer> {

    @Query(value = """
        SELECT 
            S.Id as id,
            (S.AccountName + ' (' + S.AccountCode + ')') as accountName,
            S.AccountName as accountName1,
            S.AccountCode as accountCode
        FROM AccountsGroupMaster S
        WHERE S.CompanyRefId = :companyId
        AND S.Active = 1
        AND (
            :parentCode IS NULL
            OR S.ParentId IN (
                SELECT SS.Id
                FROM AccountsGroupMaster SS
                WHERE SS.AccountCode IN (:parentCode)
                AND SS.CompanyRefId = :companyId
                AND SS.Active = 1
            )
        )
        """, nativeQuery = true)
    List<Object[]> getAccountsGroupMaster(
            @Param("companyId") Integer companyId,
            @Param("parentCode") List<String> parentCode
    );
}
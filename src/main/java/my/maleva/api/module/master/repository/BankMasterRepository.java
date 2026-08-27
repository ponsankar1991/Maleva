package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.BankMaster;
import my.maleva.api.common.dto.ComboListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankMasterRepository extends JpaRepository<BankMaster, Integer> {
    List<BankMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active banks by company ID
     * Equivalent to .NET: SELECT Id, Name as AccountName FROM BankMaster
     *                     WHERE CompanyRefId = :companyRefId AND Active = 1
     */
    @Query("SELECT new my.maleva.api.common.dto.ComboListModel(b.id, b.name as AccountName) " +
           "FROM BankMaster b " +
           "WHERE b.companyRefId = :companyRefId AND b.active = 1 " +
           "ORDER BY b.name")
    List<ComboListModel> findActiveBanksByCompany(@Param("companyRefId") Integer companyRefId);

    /** Reference check for the payment screen — SP_Payment refused an unknown or inactive bank. */
    boolean existsByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);
}

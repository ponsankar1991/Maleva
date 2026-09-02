package my.maleva.api.module.company.repository;

import my.maleva.api.module.company.entity.MasterSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterSettingRepository extends JpaRepository<MasterSetting, Integer> {

    /** First match wins: the legacy table has no unique key on (CompanyRefId, VariableName). */
    Optional<MasterSetting> findFirstByCompanyRefIdAndVariableName(Integer companyRefId, String variableName);
}

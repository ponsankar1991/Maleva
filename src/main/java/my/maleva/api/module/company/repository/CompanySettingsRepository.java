package my.maleva.api.module.company.repository;

import my.maleva.api.module.company.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Integer> {
    Optional<CompanySettings> findByCompanyRefId(Integer companyRefId);
    List<CompanySettings> findAll();
}

package my.maleva.api.repo;

import my.maleva.api.model.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Integer> {
    Optional<CompanySettings> findByCompanyRefId(Integer companyRefId);
    List<CompanySettings> findAll();
}

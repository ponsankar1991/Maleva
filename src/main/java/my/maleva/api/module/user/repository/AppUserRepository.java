package my.maleva.api.module.user.repository;

import my.maleva.api.module.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {
    Optional<AppUser> findByUserId(String userId);

    /**
     * Check if user exists by ID, company, and active status.
     * Used for SP validation: AppUser with CompanyRefId and Active=1
     */
    boolean existsByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);
}

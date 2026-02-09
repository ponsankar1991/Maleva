package my.maleva.api.repo;

import my.maleva.api.model.MENUPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MENUPrivilegeRepository extends JpaRepository<MENUPrivilege, Integer> {
}

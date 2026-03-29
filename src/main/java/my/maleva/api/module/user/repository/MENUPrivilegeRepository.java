package my.maleva.api.module.user.repository;

import my.maleva.api.module.user.entity.MENUPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MENUPrivilegeRepository extends JpaRepository<MENUPrivilege, Integer> {
}

package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.MSICCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MSICCodeRepository extends JpaRepository<MSICCode, Integer> {
}

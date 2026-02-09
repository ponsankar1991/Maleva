package my.maleva.api.repo;

import my.maleva.api.model.MSICCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MSICCodeRepository extends JpaRepository<MSICCode, Integer> {
}

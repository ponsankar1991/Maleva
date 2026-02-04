package my.maleva.api.repo;

import my.maleva.api.model.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, Integer> {
}

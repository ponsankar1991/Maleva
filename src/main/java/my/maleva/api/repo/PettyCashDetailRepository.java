package my.maleva.api.repo;

import my.maleva.api.model.PettyCashDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashDetailRepository extends JpaRepository<PettyCashDetail, Integer> {
}

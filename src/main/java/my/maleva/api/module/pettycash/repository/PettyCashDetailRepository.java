package my.maleva.api.module.pettycash.repository;

import my.maleva.api.module.pettycash.entity.PettyCashDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PettyCashDetailRepository extends JpaRepository<PettyCashDetail, Integer> {
}

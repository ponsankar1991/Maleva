package my.maleva.api.module.accountsgroupmaster.repository;

import my.maleva.api.module.accountsgroupmaster.entity.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("accountsGroupClassificationRepository")
public interface ClassificationRepository extends JpaRepository<Classification, Integer> {
}


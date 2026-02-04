package my.maleva.api.repo;

import my.maleva.api.model.CardMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardMasterRepository extends JpaRepository<CardMaster, Integer> {
    List<CardMaster> findByCompanyRefId(Integer companyRefId);
}

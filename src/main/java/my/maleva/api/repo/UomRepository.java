package my.maleva.api.repo;

import my.maleva.api.model.Uom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UomRepository extends JpaRepository<Uom, Integer> {
    List<Uom> findByCompanyRefId(Integer companyRefId);
}

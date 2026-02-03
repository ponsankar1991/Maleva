package my.maleva.api.repo;

import my.maleva.api.model.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Integer> {
    List<AddressMaster> findByCompanyRefId(Integer companyRefId);
}

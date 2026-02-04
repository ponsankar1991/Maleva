package my.maleva.api.repo;

import my.maleva.api.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Integer> {
    List<Counter> findByCompanyRefId(Integer companyRefId);
}

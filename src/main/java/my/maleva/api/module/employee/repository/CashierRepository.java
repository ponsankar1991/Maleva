package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.entity.Cashier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashierRepository extends JpaRepository<Cashier, Integer> {
    List<Cashier> findByCompanyRefId(Integer companyRefId);
}

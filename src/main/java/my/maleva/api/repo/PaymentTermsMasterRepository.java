package my.maleva.api.repo;

import my.maleva.api.model.PaymentTermsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTermsMasterRepository extends JpaRepository<PaymentTermsMaster, Integer> {
}

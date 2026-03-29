package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.PaymentTermsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTermsMasterRepository extends JpaRepository<PaymentTermsMaster, Integer> {
}

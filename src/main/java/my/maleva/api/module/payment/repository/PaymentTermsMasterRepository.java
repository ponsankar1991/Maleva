package my.maleva.api.module.payment.repository;

import my.maleva.api.module.master.entity.PaymentTermsMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PaymentTermsMasterRepository
 *
 * Standard JPA repository for PaymentTermsMaster entity
 * Provides CRUD operations via JpaRepository methods:
 * - findById(Integer id) - inherited from JpaRepository
 * - save(PaymentTermsMaster) - inherited from JpaRepository
 * - delete(PaymentTermsMaster) - inherited from JpaRepository
 * - findAll() - inherited from JpaRepository
 */
@Repository
public interface PaymentTermsMasterRepository extends JpaRepository<PaymentTermsMaster, Integer> {
    // Standard JpaRepository methods are sufficient
    // No custom queries needed
    boolean existsByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);
}

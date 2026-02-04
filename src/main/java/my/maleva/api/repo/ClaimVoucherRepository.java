package my.maleva.api.repo;

import my.maleva.api.model.ClaimVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimVoucherRepository extends JpaRepository<ClaimVoucher, Integer> {
    List<ClaimVoucher> findByCompanyRefId(Integer companyRefId);
    List<ClaimVoucher> findBySupplierRefId(Integer supplierRefId);
}

package my.maleva.api.repo;

import my.maleva.api.model.ItemMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, Integer> {
    List<ItemMaster> findByCompanyRefId(Integer companyRefId);
    List<ItemMaster> findByProdCode(String prodCode);
}

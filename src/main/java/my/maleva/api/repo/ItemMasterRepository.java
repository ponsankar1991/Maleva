package my.maleva.api.repo;

import my.maleva.api.model.ItemMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, Integer> {
    List<ItemMaster> findByCompanyRefId(Integer companyRefId);
    List<ItemMaster> findByProdCode(String prodCode);

    // Get product list for company with only active items, sorted by product name
    @Query("SELECT e FROM ItemMaster e WHERE e.companyRefId = :companyRefId AND e.activestatus = 1 ORDER BY e.pName ASC")
    List<ItemMaster> findProductListByCompanyId(@Param("companyRefId") Integer companyRefId);
}

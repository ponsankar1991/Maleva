package my.maleva.api.module.itemmaster.repository;

import my.maleva.api.module.itemmaster.entity.ItemMasterCStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMasterCStockRepository extends JpaRepository<ItemMasterCStock, Integer> {
    List<ItemMasterCStock> findByCompanyRefId(Integer companyRefId);
}

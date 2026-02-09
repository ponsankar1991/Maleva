package my.maleva.api.repo;

import my.maleva.api.model.ItemMasterJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMasterJobDetailsRepository extends JpaRepository<ItemMasterJobDetails, Integer> {
}

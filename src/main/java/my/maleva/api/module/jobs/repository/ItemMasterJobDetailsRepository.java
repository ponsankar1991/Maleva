package my.maleva.api.module.jobs.repository;

import my.maleva.api.module.jobs.entity.ItemMasterJobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMasterJobDetailsRepository extends JpaRepository<ItemMasterJobDetails, Integer> {
}

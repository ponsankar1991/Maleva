package my.maleva.api.module.inventory.recon.repository;

import my.maleva.api.module.inventory.recon.entity.ReconCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconCostRepository extends JpaRepository<ReconCost, Integer> {

    List<ReconCost> findByReconRefIdOrderByIdAsc(Integer reconRefId);

    List<ReconCost> findByReconRefIdIn(List<Integer> reconRefIds);

    void deleteByReconRefId(Integer reconRefId);
}

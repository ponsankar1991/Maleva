package my.maleva.api.repo;

import my.maleva.api.model.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageUploadRepository extends JpaRepository<ImageUpload, Integer> {
    List<ImageUpload> findByCompanyRefId(Integer companyRefId);
    List<ImageUpload> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

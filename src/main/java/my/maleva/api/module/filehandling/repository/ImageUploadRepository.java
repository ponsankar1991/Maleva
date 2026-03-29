package my.maleva.api.module.filehandling.repository;

import my.maleva.api.module.filehandling.entity.ImageUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageUploadRepository extends JpaRepository<ImageUpload, Integer> {
    List<ImageUpload> findByCompanyRefId(Integer companyRefId);
    List<ImageUpload> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

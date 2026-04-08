package my.maleva.api.module.vessalplanning.repository;

import my.maleva.api.module.vessalplanning.entity.VesselPlanningMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VesselPlanningMasterRepository extends JpaRepository<VesselPlanningMaster, Integer> {

    List<VesselPlanningMaster> findByCompanyRefId(Integer companyRefId);

    List<VesselPlanningMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    List<VesselPlanningMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    Optional<VesselPlanningMaster> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    Optional<VesselPlanningMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    Optional<VesselPlanningMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);



    List<VesselPlanningMaster> findByEmployeeRefId(Integer employeeRefId);

    List<VesselPlanningMaster> findByFDateGreaterThanEqualAndTDateLessThanEqual(LocalDate startDate, LocalDate endDate);

    List<VesselPlanningMaster> findByCompanyRefIdAndFDateGreaterThanEqualAndTDateLessThanEqual(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    long countByCompanyRefId(Integer companyRefId);

    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    @Modifying
    @Query("UPDATE VesselPlanningMaster v SET v.cNumberDisplay = :cNumberDisplay, v.cNumber = :cNumber WHERE v.id = :id")
    void updateCNumberDisplay(@Param("id") Integer id, @Param("cNumberDisplay") String cNumberDisplay, @Param("cNumber") Integer cNumber);
}

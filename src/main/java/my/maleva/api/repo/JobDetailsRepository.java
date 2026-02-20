package my.maleva.api.repo;

import my.maleva.api.dto.JobDetailsWithNameDto;
import my.maleva.api.model.JobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDetailsRepository extends JpaRepository<JobDetails, Integer> {

    @Query("SELECT NEW my.maleva.api.dto.JobDetailsWithNameDto(" +
            "jd.id, jd.jobMasterRefId, jd.description, jm.name, jsm.name, jd.active, jd.mandatory, jd.status) " +
            "FROM JobDetails jd " +
            "LEFT JOIN JobTypeMaster jm ON jm.id = jd.jobMasterRefId " +
            "LEFT JOIN JobStatusMaster jsm ON jsm.id = jd.status " +
            "WHERE jd.companyRefId = :companyId " +
            "AND jd.jobMasterRefId = :jobId " +
            "AND jd.active = 1")
    List<JobDetailsWithNameDto> findJobDetailsWithNames(
            @Param("companyId") Integer companyId,
            @Param("jobId") Integer jobId);
}

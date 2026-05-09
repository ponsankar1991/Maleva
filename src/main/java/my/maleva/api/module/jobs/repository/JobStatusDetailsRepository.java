package my.maleva.api.module.jobs.repository;

import my.maleva.api.module.jobs.dto.JobStatusDetailsWithNameDto;
import my.maleva.api.module.jobs.entity.JobStatusDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusDetailsRepository extends JpaRepository<JobStatusDetails, Integer> {

    @Query("SELECT NEW my.maleva.api.module.jobs.dto.JobStatusDetailsWithNameDto(" +
            "jsd.id, jsd.jobMasterRefId, jsd.status, jsm.name, jsd.minStatus, jsm2.name, jsd.sort) " +
            "FROM JobStatusDetails jsd " +
            "LEFT JOIN JobStatusMaster jsm ON jsm.id = jsd.status " +
            "LEFT JOIN JobStatusMaster jsm2 ON jsm2.id = jsd.minStatus " +
            "WHERE jsd.companyRefId = :companyId " +
            "AND jsd.jobMasterRefId = :jobId " +
            "AND (:complete = 1 OR jsd.status != 8) " +
            "ORDER BY jsd.sort ASC")
    List<JobStatusDetailsWithNameDto> findJobStatusDetailsWithNames(
            @Param("companyId") Integer companyId,
            @Param("jobId") Integer jobId,
            @Param("complete") Integer complete);
            
    // Keep the old one just in case there are other callers
    @Query("SELECT NEW my.maleva.api.module.jobs.dto.JobStatusDetailsWithNameDto(" +
            "jsd.id, jsd.jobMasterRefId, jsd.status, jsm.name, jsd.minStatus, jsm2.name, jsd.sort) " +
            "FROM JobStatusDetails jsd " +
            "LEFT JOIN JobStatusMaster jsm ON jsm.id = jsd.status " +
            "LEFT JOIN JobStatusMaster jsm2 ON jsm2.id = jsd.minStatus " +
            "WHERE jsd.companyRefId = :companyId " +
            "AND jsd.jobMasterRefId = :jobId " +
            "ORDER BY jsd.sort ASC")
    List<JobStatusDetailsWithNameDto> findJobStatusDetailsWithNames(
            @Param("companyId") Integer companyId,
            @Param("jobId") Integer jobId);
}

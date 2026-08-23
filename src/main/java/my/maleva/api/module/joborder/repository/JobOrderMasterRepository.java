package my.maleva.api.module.joborder.repository;

import my.maleva.api.module.joborder.entity.JobOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOrderMasterRepository extends JpaRepository<JobOrderMaster, Integer>, JpaSpecificationExecutor<JobOrderMaster> {
    
    List<JobOrderMaster> findByCompanyRefId(Integer companyRefId);
    
    Optional<JobOrderMaster> findByIdAndCompanyRefId(Integer id, Integer companyRefId);

    /**
     * Jobs that are still open: active, and neither Completed nor Cancelled.
     *
     * Both `assign` and `InProgress` count as open. Filtering on InProgress
     * alone returns nothing on the current data, because that status is marked
     * inactive in JobOrderStatusMaster and nothing can be set to it.
     *
     * The lookups are fetched with the master so the dashboard does not fire a
     * query per row for the truck and status names.
     */
    @Query("select distinct j from JobOrderMaster j "
            + "left join fetch j.truck "
            + "left join fetch j.status "
            + "left join fetch j.priority "
            + "where j.companyRefId = :companyRefId "
            + "and j.isActive = true "
            + "and (j.status is null or j.status.id not in :closedStatusIds) "
            + "order by j.expectedCompletionDate asc")
    List<JobOrderMaster> findOpenJobs(@Param("companyRefId") Integer companyRefId,
                                      @Param("closedStatusIds") List<Integer> closedStatusIds);
}

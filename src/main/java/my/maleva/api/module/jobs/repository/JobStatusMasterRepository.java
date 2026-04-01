package my.maleva.api.module.jobs.repository;

import my.maleva.api.module.jobs.entity.JobStatusMaster;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusMasterRepository extends JpaRepository<JobStatusMaster, Integer> {

    @EntityGraph(attributePaths = "parentStatus")
    @Query("""
            SELECT jsm
            FROM JobStatusMaster jsm
            LEFT JOIN FETCH jsm.parentStatus parent
            WHERE jsm.companyRefId = :companyId
              AND jsm.active <> :excludedActive
            ORDER BY parent.name ASC, jsm.name ASC
            """)
    List<JobStatusMaster> findSelectableByCompanyId(
            @Param("companyId") Integer companyId,
            @Param("excludedActive") Integer excludedActive);
}

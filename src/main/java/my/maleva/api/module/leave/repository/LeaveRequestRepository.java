package my.maleva.api.module.leave.repository;

import my.maleva.api.module.leave.entity.LeaveRequestMaster;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestMaster, Integer> {
    
    @EntityGraph(attributePaths = {"leaveType", "leaveStatus"})
    List<LeaveRequestMaster> findByActive(Integer active);

    @EntityGraph(attributePaths = {"leaveType", "leaveStatus"})
    @Query("SELECT lr FROM LeaveRequestMaster lr WHERE lr.active = 1 " +
           "AND (:companyRefId IS NULL OR lr.companyRefId = :companyRefId) " +
           "AND (:applicantType IS NULL OR lr.applicantType = :applicantType) " +
           "AND (:applicantRefId IS NULL OR lr.applicantRefId = :applicantRefId) " +
           "AND (CAST(:fromDate as date) IS NULL OR lr.fromDate >= :fromDate) " +
           "AND (CAST(:toDate as date) IS NULL OR lr.fromDate <= :toDate) " +
           "ORDER BY lr.createdDate DESC")
    List<LeaveRequestMaster> searchByAdvancedFilters(
            @org.springframework.data.repository.query.Param("companyRefId") Integer companyRefId,
            @org.springframework.data.repository.query.Param("applicantType") Integer applicantType,
            @org.springframework.data.repository.query.Param("applicantRefId") Integer applicantRefId,
            @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDateTime fromDate,
            @org.springframework.data.repository.query.Param("toDate") java.time.LocalDateTime toDate);
            
    @EntityGraph(attributePaths = {"leaveType", "leaveStatus"})
    @Query("SELECT lr FROM LeaveRequestMaster lr WHERE lr.active = :active " +
           "AND lr.applicantType = :applicantType " +
           "AND lr.applicantRefId IN :applicantRefIds " +
           "AND lr.fromDate <= :endDate AND lr.toDate >= :startDate")
    List<LeaveRequestMaster> findOverlappingLeavesForApplicants(
        @org.springframework.data.repository.query.Param("applicantType") Integer applicantType, 
        @org.springframework.data.repository.query.Param("applicantRefIds") List<Integer> applicantRefIds, 
        @org.springframework.data.repository.query.Param("active") Integer active,
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}

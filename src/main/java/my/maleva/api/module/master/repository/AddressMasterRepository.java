package my.maleva.api.module.master.repository;

import my.maleva.api.module.master.entity.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Integer> {

    /**
     * Find all addresses for a specific company
     */
    List<AddressMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active addresses for a company by name keyword
     * Searches by company ID, keyword in name, and active status
     * Results are ordered by name
     *
     * @param companyRefId the company ID
     * @param keyword the search keyword (name contains)
     * @return list of matching active addresses ordered by name
     */
    @Query("SELECT a FROM AddressMaster a " +
            "WHERE a.companyRefId = :companyRefId " +
            "AND a.active != 2 " +
            "AND LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY a.name ASC")
    List<AddressMaster> findByCompanyAndKeyword(
            @Param("companyRefId") Integer companyRefId,
            @Param("keyword") String keyword);

    /**
     * Find all active addresses for a company (active != 2)
     *
     * @param companyRefId the company ID
     * @return list of active addresses ordered by name
     */
    @Query("SELECT a FROM AddressMaster a " +
            "WHERE a.companyRefId = :companyRefId " +
            "AND a.active != 2 " +
            "ORDER BY a.name ASC")
    List<AddressMaster> findActiveByCompanyId(@Param("companyRefId") Integer companyRefId);
}

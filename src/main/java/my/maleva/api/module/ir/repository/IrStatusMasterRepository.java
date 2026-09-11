package my.maleva.api.module.ir.repository;

import my.maleva.api.module.ir.entity.IrStatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IrStatusMasterRepository extends JpaRepository<IrStatusMaster, Integer> {

    /**
     * The dropdown. Ordered by id, which is insertion order - the seed inserts
     * OPEN, UNDER_REVIEW, APPROVED, REJECTED, CLOSED in that sequence, and a
     * status added later lands at the bottom of the list.
     */
    List<IrStatusMaster> findByCompanyRefIdAndActiveOrderByIdAsc(Integer companyRefId, Integer active);

    Optional<IrStatusMaster> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    Optional<IrStatusMaster> findByCompanyRefIdAndStatusCodeAndActive(
            Integer companyRefId, String statusCode, Integer active);

    /** Used to turn the finished status codes into the ids the query filters on. */
    List<IrStatusMaster> findByCompanyRefIdAndStatusCodeIn(Integer companyRefId, Collection<String> statusCodes);
}

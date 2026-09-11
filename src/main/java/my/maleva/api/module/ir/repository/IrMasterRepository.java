package my.maleva.api.module.ir.repository;

import my.maleva.api.module.ir.entity.IrMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IrMasterRepository
        extends JpaRepository<IrMaster, Integer>, JpaSpecificationExecutor<IrMaster> {

    /**
     * Company-scoped read. An id from another company reads as not found rather
     * than leaking a row, and a soft-deleted row is never returned.
     */
    Optional<IrMaster> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);
}

package my.maleva.api.module.fleet.repository;

import my.maleva.api.module.fleet.entity.TruckOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Truck order data access.
 *
 * The calendar's filters are all optional, so the list query is built from
 * {@link my.maleva.api.module.fleet.specification.TruckOrderSpecification}.
 */
@Repository
public interface TruckOrderRepository extends JpaRepository<TruckOrder, Integer>,
        JpaSpecificationExecutor<TruckOrder> {

    /** Calendar order: oldest day first, then truck, matching the legacy ORDER BY. */
    Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("orderDate"), Sort.Order.asc("truckRefId"));

    Optional<TruckOrder> findByIdAndCompanyRefIdAndActive(Integer id, Integer companyRefId, Integer active);

    /**
     * The "this truck is already booked on the selected date" rule.
     *
     * <p>{@code excludeId} keeps an edit from colliding with itself; pass 0 when
     * inserting, since no row has that id.
     */
    @Query("select count(o) from TruckOrder o "
            + "where o.companyRefId = :companyRefId and o.truckRefId = :truckRefId "
            + "and o.orderDate = :orderDate and o.active = 1 and o.id <> :excludeId")
    long countClashes(@Param("companyRefId") Integer companyRefId,
                      @Param("truckRefId") Integer truckRefId,
                      @Param("orderDate") LocalDate orderDate,
                      @Param("excludeId") Integer excludeId);

    // No bulk soft-delete query here on purpose. The other fleet documents have
    // one and test its affected-row count against 0, but the pool runs
    // `SET NOCOUNT ON` as its connection-init SQL, so SQL Server sends no row
    // count and JDBC reports -1 for every UPDATE - the count can never be 0 and
    // the check never fires. TruckOrderServiceImpl.delete loads the order and
    // updates it as a managed entity instead.
}

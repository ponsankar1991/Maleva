package my.maleva.api.module.inventory.recon.repository;

import jakarta.persistence.LockModeType;
import my.maleva.api.module.inventory.recon.entity.ReconJob;
import my.maleva.api.module.inventory.recon.entity.ReconStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconJobRepository extends JpaRepository<ReconJob, Integer> {

    /**
     * Row-locking read used before every status transition, so two people
     * acting on the same job at once cannot both succeed - sending a job to a
     * vendor twice, or completing it twice and putting two units into stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from ReconJob j where j.id = :id")
    Optional<ReconJob> lockById(@Param("id") Integer id);

    /**
     * Search for the list screen. Every filter is optional: a null status,
     * truck or vendor means "any", and the date bounds are applied only when
     * supplied, so one query serves the board, the list and the filters.
     */
    @Query("select j from ReconJob j where j.companyRefId = :companyRefId and j.active = 1 "
         + "and (:status is null or j.status = :status) "
         + "and (:truckRefId is null or j.removedFromTruckRefId = :truckRefId) "
         + "and (:vendorRefId is null or j.vendorRefId = :vendorRefId) "
         + "and (:fromDate is null or j.removedDate >= :fromDate) "
         + "and (:toDate is null or j.removedDate <= :toDate) "
         + "order by j.removedDate desc, j.id desc")
    List<ReconJob> search(@Param("companyRefId") Integer companyRefId,
                          @Param("status") ReconStatus status,
                          @Param("truckRefId") Integer truckRefId,
                          @Param("vendorRefId") Integer vendorRefId,
                          @Param("fromDate") LocalDateTime fromDate,
                          @Param("toDate") LocalDateTime toDate);

    /**
     * The recon shelf: everything removed but not yet finished. Ordered oldest
     * first because the point of the screen is to surface cores that have been
     * sitting too long.
     */
    @Query("select j from ReconJob j where j.companyRefId = :companyRefId and j.active = 1 "
         + "and j.status in (my.maleva.api.module.inventory.recon.entity.ReconStatus.PENDING, "
         + "                 my.maleva.api.module.inventory.recon.entity.ReconStatus.IN_PROGRESS) "
         + "order by j.removedDate asc")
    List<ReconJob> findOpen(@Param("companyRefId") Integer companyRefId);

    List<ReconJob> findByCompanyRefIdAndRemovedFromTruckRefIdAndActiveOrderByRemovedDateDesc(
            Integer companyRefId, Integer removedFromTruckRefId, Integer active);

    List<ReconJob> findByCompanyRefIdAndAssetRefIdAndActiveOrderByRemovedDateDesc(
            Integer companyRefId, Integer assetRefId, Integer active);

    /**
     * An open job on the same unit. A serial can only be on one recon job at a
     * time, so this blocks a second removal before the first is closed out.
     */
    @Query("select j from ReconJob j where j.companyRefId = :companyRefId and j.assetRefId = :assetRefId "
         + "and j.active = 1 and j.status in ("
         + "  my.maleva.api.module.inventory.recon.entity.ReconStatus.PENDING, "
         + "  my.maleva.api.module.inventory.recon.entity.ReconStatus.IN_PROGRESS)")
    Optional<ReconJob> findOpenByAsset(@Param("companyRefId") Integer companyRefId,
                                       @Param("assetRefId") Integer assetRefId);

    /**
     * Recon spend grouped by truck, for the cost-per-truck report.
     * Each row is [truckRefId, jobCount, totalCost].
     */
    @Query("select j.removedFromTruckRefId, count(j), coalesce(sum(j.totalCost), 0) from ReconJob j "
         + "where j.companyRefId = :companyRefId and j.active = 1 "
         + "and (:fromDate is null or j.removedDate >= :fromDate) "
         + "and (:toDate is null or j.removedDate <= :toDate) "
         + "group by j.removedFromTruckRefId order by coalesce(sum(j.totalCost), 0) desc")
    List<Object[]> summariseByTruck(@Param("companyRefId") Integer companyRefId,
                                    @Param("fromDate") LocalDateTime fromDate,
                                    @Param("toDate") LocalDateTime toDate);
}

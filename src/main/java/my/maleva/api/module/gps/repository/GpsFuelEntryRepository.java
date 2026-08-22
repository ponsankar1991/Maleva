package my.maleva.api.module.gps.repository;

import my.maleva.api.module.fleet.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FuelEntry access for the GPS matching: reads the truck-day, and writes back
 * the FuelFillingRefId link once a filling has been assigned.
 */
@Repository
public interface GpsFuelEntryRepository extends JpaRepository<FuelEntry, Integer> {

    /**
     * Active entries of one truck on one day, oldest first.
     *
     * The day is passed as an explicit range rather than a CAST on SaleDate so
     * the query stays sargable and can use an index on (CompanyRefId, SaleDate).
     */
    @Query("select e from FuelEntry e "
            + "where e.companyRefId = :companyRefId "
            + "and e.truckRefid = :truckRefId "
            + "and e.active = 1 "
            + "and e.saleDate >= :dayStart and e.saleDate < :nextDayStart "
            + "order by e.id asc")
    List<FuelEntry> findActiveForTruckOnDay(@Param("companyRefId") Integer companyRefId,
                                            @Param("truckRefId") Integer truckRefId,
                                            @Param("dayStart") LocalDateTime dayStart,
                                            @Param("nextDayStart") LocalDateTime nextDayStart);

    /** Entries currently holding a given filling; used to keep the link one-to-one. */
    List<FuelEntry> findByFuelFillingRefId(Integer fuelFillingRefId);
}

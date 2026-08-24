package my.maleva.api.module.billing.billorder.repository;

import my.maleva.api.module.billing.billorder.entity.BillsOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BillsOrderDetailsRepository extends JpaRepository<BillsOrderDetails, Integer> {
    List<BillsOrderDetails> findByBillsOrderMasterRefId(Integer billsOrderMasterRefId);
    void deleteByBillsOrderMasterRefId(Integer billsOrderMasterRefId);

    /**
     * Claims a line for receiving, atomically.
     *
     * The WHERE clause only matches a line that has never been pushed, so the
     * database - not the caller - decides who wins a race: SQL Server locks the
     * row for the first UPDATE, and a second one arriving before the first
     * commits waits, then re-checks the now-committed StockPushedDate and
     * matches nothing. Returns 1 if this call just claimed the line, 0 if it
     * was already claimed - the caller must check for 0 and refuse to move
     * stock when it sees it.
     */
    @Modifying
    @Query("UPDATE BillsOrderDetails d SET d.stockPushedDate = CURRENT_TIMESTAMP, "
         + "d.stockPushedQty = :qty "
         + "WHERE d.id = :id AND d.stockPushedDate IS NULL")
    int claimForReceiving(@Param("id") Integer id, @Param("qty") BigDecimal qty);
}

package my.maleva.api.module.billing.billorder.repository;

import my.maleva.api.module.billing.billorder.entity.BillsOrderMaster;
import my.maleva.api.module.billing.billorder.dto.PaymentVoucherComboDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillsOrderMasterRepository extends JpaRepository<BillsOrderMaster, Integer> {

    List<BillsOrderMaster> findByCompanyRefId(Integer companyRefId);

    List<BillsOrderMaster> findBySupplierRefId(Integer supplierRefId);

    /**
     * Get all unique invoice numbers for a company where Active != 2
     * Used for Payment Voucher dropdown/combo list
     */
    @Query("SELECT new my.maleva.api.module.billing.billorder.dto.PaymentVoucherComboDto(" +
           "bom.invoiceNo, bom.invoiceNo) " +
           "FROM BillsOrderMaster bom " +
           "WHERE bom.companyRefId = :companyRefId AND bom.active != 2 " +
           "ORDER BY bom.invoiceNo")
    List<PaymentVoucherComboDto> findInvoiceNumbersByCompany(@Param("companyRefId") Integer companyRefId);

    /**
     * Get all distinct descriptions for a company
     * Filters: Active != 2, description not empty/null
     * Used for description dropdown/combo list
     *
     * @param companyRefId the company ID
     * @return list of distinct descriptions (trimmed)
     */
    @Query("SELECT DISTINCT TRIM(bom.description) FROM BillsOrderMaster bom " +
           "WHERE bom.companyRefId = :companyRefId " +
           "AND bom.active != 2 " +
           "AND bom.description IS NOT NULL " +
           "AND TRIM(bom.description) != '' " +
           "ORDER BY TRIM(bom.description)")
    List<String> findDistinctDescriptionsByCompany(@Param("companyRefId") Integer companyRefId);

    /**
     * Get all distinct PayTo values for a company
     * Filters: Active != 2, PayTo not empty/null
     * Used for payment voucher dropdown/combo list
     *
     * @param companyRefId the company ID
     * @return list of distinct PayTo values (trimmed)
     */
    @Query("SELECT DISTINCT TRIM(bom.payTo) FROM BillsOrderMaster bom " +
           "WHERE bom.companyRefId = :companyRefId " +
           "AND bom.active != 2 " +
           "AND bom.payTo IS NOT NULL " +
           "AND TRIM(bom.payTo) != '' " +
           "ORDER BY TRIM(bom.payTo)")
    List<String> findDistinctPayToByCompany(@Param("companyRefId") Integer companyRefId);

    // No bulk soft-delete query here on purpose. There used to be one, and
    // deleteBillsOrderMaster answered the caller from its affected-row count. The
    // pool runs `SET NOCOUNT ON` as its connection-init SQL, so SQL Server sends
    // no row count and JDBC reports -1 for every UPDATE - `rowsUpdated > 0` was
    // false even after a delete that worked. BillsOrderMasterService
    // .deleteBillsOrderMaster loads the order and updates it as a managed entity.
}

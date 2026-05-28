package my.maleva.api.module.billing.billorder.service;

import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;

/**
 * Service interface for BillsOrderMaster insert/update operations
 * Equivalent to .NET ISupplierServices.InsertBillsOrderMaster
 *
 * Provides business logic for:
 * 1. Validation of bill order details
 * 2. Updating related SaleOrderMaster records
 * 3. Persisting data via stored procedure
 * 4. Sending notifications
 */
public interface IBillsOrderMasterInsertService {

    /**
     * Insert or update BillsOrderMaster with related details and validations
     *
     * Process flow:
     * 1. Validates all bill order details have AccountMasterRefId set
     * 2. Updates related SaleOrderMaster records based on charge description type
     * 3. Calls stored procedure SP_BillsOrderMaster for database operations
     * 4. Generates sequence numbers and display numbers
     * 5. Sends WhatsApp notification on new inserts
     *
     * @param billsOrderMasterDto The bills order master data to insert/update
     * @param companyId          The company ID
     * @return Response containing operation result, bill ID, and bill number
     */
    BillsOrderMasterResponseDto insertBillsOrderMaster(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer companyId
    );

    /**
     * Validate bills order details
     * Ensures all items have AccountMasterRefId set (non-zero)
     *
     * @param billsOrderMasterDto The bills order master data
     * @throws IllegalArgumentException if validation fails
     */
    void validateBillsOrderDetails(BillsOrderMasterInsertDto billsOrderMasterDto);

    /**
     * Update SaleOrderMaster flags based on charge description type
     *
     * Different charge types update different flags:
     * - "PORT CHARGES" → PortCPop
     * - "CUSTOM CLEARANCE" / "CUSTOMER CLEARANCE" → ForwardingCPop
     * - "BOAT CHARGES" → BoatCPop
     * - "PERMIT CHARGES" / "INWARD PERMIT CHARGES" → PermitCPop
     * - "MMHE CHARGES" → MMHECPop
     * - "AIR FREIGHT EXPORT CHARGES" → AFpoCPop
     * - "STORAGE FEE" / "FREIGHT CHARGES" → SFWpoCPop
     * - "CRANE & WHARFMARK CHARGES" → BoatCPop1
     * - "PFP & PAC CHARGES" → PFPPCPop1
     *
     * @param billsOrderMasterDto The bills order master data with Description and SaleMasterRefId
     */
    void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto billsOrderMasterDto);
}




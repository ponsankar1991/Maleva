package my.maleva.api.module.billing.billorder.service;

import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;

/**
 * Service interface for BillsOrderMaster insert/update operations
 */
public interface IBillsOrderMasterInsertService {

    /**
     * Insert or update BillsOrderMaster with related details and validations
     *
     * Performs the following:
     * 1. Validates all bill order details have AccountMasterRefId set
     * 2. Updates related SaleOrderMaster records based on charge description type
     * 3. Calls stored procedure SP_BillsOrderMaster for database operations
     * 4. Generates sequence numbers and display numbers
     * 5. Sends WhatsApp notification on new inserts
     *
     * @param billsOrderMasterDto The bills order master data to insert/update
     * @param companyId          The company ID
     * @return Response containing operation result and generated bill number
     */
    BillsOrderMasterResponseDto insertBillsOrderMaster(
            BillsOrderMasterInsertDto billsOrderMasterDto,
            Integer companyId
    );

    /**
     * Validate bills order details
     * Ensures all items have AccountMasterRefId set
     *
     * @param billsOrderMasterDto The bills order master data
     * @throws IllegalArgumentException if validation fails
     */
    void validateBillsOrderDetails(BillsOrderMasterInsertDto billsOrderMasterDto);

    /**
     * Update SaleOrderMaster flags based on charge description type
     * Different charge types (PORT CHARGES, CUSTOM CLEARANCE, etc.) update different flags
     *
     * @param billsOrderMasterDto The bills order master data
     * @param recalculateFlags    Whether to recalculate existing flags
     */
    void updateSaleOrderMasterFlags(BillsOrderMasterInsertDto billsOrderMasterDto, boolean recalculateFlags);
}


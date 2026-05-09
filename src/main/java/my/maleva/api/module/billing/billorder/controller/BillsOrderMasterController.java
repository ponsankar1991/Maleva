package my.maleva.api.module.billing.billorder.controller;

import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderF5ViewDto;
import my.maleva.api.module.billing.billorder.dto.PaymentVoucherComboDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterInsertDto;
import my.maleva.api.module.billing.billorder.dto.BillsOrderMasterResponseDto;
import my.maleva.api.module.billing.billorder.dto.SelectBillsOrderMasterRequestDto;
import my.maleva.api.module.billing.billorder.service.BillsOrderMasterService;
import my.maleva.api.module.billing.billorder.service.IBillsOrderMasterInsertService;
import my.maleva.api.module.billing.billorder.repository.BillsOrderMasterRepository;
import my.maleva.api.common.dto.ResponseViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills-order")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BillsOrderMasterController {

	private static final Logger logger = LoggerFactory.getLogger(BillsOrderMasterController.class);
	private final BillsOrderMasterService service;
	private final IBillsOrderMasterInsertService billsOrderMasterInsertService;
	private final BillsOrderMasterRepository billsOrderMasterRepository;

	public BillsOrderMasterController(
			BillsOrderMasterService service,
			IBillsOrderMasterInsertService billsOrderMasterInsertService,
			BillsOrderMasterRepository billsOrderMasterRepository) {
		this.service = service;
		this.billsOrderMasterInsertService = billsOrderMasterInsertService;
		this.billsOrderMasterRepository = billsOrderMasterRepository;
	}

	/**
	 * Insert or Update BillsOrderMaster with Validations
	 * POST /api/bills-order/insert
	 * 
	 * This endpoint:
	 * 1. Validates all bill order details have AccountMasterRefId set
	 * 2. Updates related SaleOrderMaster records based on charge description type
	 * 3. Calls stored procedure for database operations
	 * 4. Generates sequence numbers and display numbers
	 * 5. Sends WhatsApp notification on new inserts
	 *
	 * Headers Required:
	 * - Comid: Company ID (required)
	 *
	 * @param billsOrderMasterDto The bills order master data to insert/update
	 * @param comid              The company ID from header
	 * @return Response with operation result and generated bill number
	 */
	@PostMapping("/insert")
	public ResponseEntity<Map<String, Object>> insertBillsOrderMaster(
			@Valid @RequestBody BillsOrderMasterInsertDto billsOrderMasterDto,
			@RequestHeader(value = "Comid", required = false) Integer comid) {
		try {
			// Validate Comid
			if (comid == null || comid <= 0) {
				logger.warn("Invalid request: Comid is missing or invalid");
				return ResponseEntity.badRequest().body(Map.of(
						"ok", false,
						"message", "Company ID (Comid) is required and must be positive"
				));
			}

			logger.info("Inserting BillsOrderMaster for Company: {}", comid);

			// Call service
			BillsOrderMasterResponseDto response = billsOrderMasterInsertService
					.insertBillsOrderMaster(billsOrderMasterDto, comid);

			// Build response
			if (response.isSuccess()) {
				logger.info("BillsOrderMaster inserted successfully. ID: {}, BillNo: {}", 
					response.getId(), response.getBillNo());
				return ResponseEntity.ok(Map.of(
						"ok", true,
						"message", response.getMessage(),
						"id", response.getId(),
						"billNo", response.getBillNo(),
						"saleTime", response.getSaleTime()
				));
			} else {
				logger.warn("BillsOrderMaster insert failed. Message: {}", response.getMessage());
				return ResponseEntity.ok(Map.of(
						"ok", false,
						"message", response.getMessage()
				));
			}

		} catch (IllegalArgumentException ex) {
			logger.warn("Validation error in insertBillsOrderMaster: {}", ex.getMessage());
			return ResponseEntity.badRequest().body(Map.of(
					"ok", false,
					"message", ex.getMessage()
			));
		} catch (Exception ex) {
			logger.error("Error in insertBillsOrderMaster", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
					"ok", false,
					"error", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
			));
		}
	}

	/**
	 * Check Invoice Numbers for a Company
	 * GET /api/bills-order/check-invoice-no/{companyId}
	 * 
	 * Retrieves all invoice numbers from BillsOrderMaster where Active != 2
	 * Returns a list of PaymentVoucherComboDto containing invoice numbers
	 * 
	 * @param companyId The Company Reference ID
	 * @return ResponseViewModel with invoice numbers list
	 */
	@GetMapping("/check-invoice-no/{companyId}")
	public ResponseEntity<ResponseViewModel> checkInvoiceNo(@PathVariable Integer companyId) {
		logger.info("Fetching invoice numbers for company: {}", companyId);
		
		try {
			List<PaymentVoucherComboDto> invoiceNumbers = service.checkInvoiceNo(companyId);
			
			ResponseViewModel response = ResponseViewModel.builder()
					.isSuccess(true)
					.statusCode(HttpStatus.OK.value())
					.message("Invoice numbers retrieved successfully")
					.data1(invoiceNumbers)
					.build();
			
			logger.info("Successfully retrieved {} invoice numbers for company: {}", 
					invoiceNumbers.size(), companyId);
			
			return ResponseEntity.ok(response);
			
		} catch (Exception ex) {
			logger.error("Error fetching invoice numbers for company: {}", companyId, ex);
			
			ResponseViewModel errorResponse = ResponseViewModel.builder()
					.isSuccess(false)
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(ex.getMessage() != null ? ex.getMessage() : "Error fetching invoice numbers")
					.data1("Api Details: BillsOrderMaster_CheckInvoiceNo")
					.build();
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Get Maximum Bills Order Master Number
	 * GET /api/bills-order/max-bills-order-no/{companyId}
	 *
	 * Generates the next bills order number based on the sequence number from SequenceNoMaster
	 * Format: "PO" + padded sequence number (9 digits with leading zeros)
	 * Example: "PO000000001", "PO000000002", etc.
	 *
	 * @param companyId The Company Reference ID
	 * @return ResponseViewModel with generated order number in data1
	 */
	@GetMapping("/max-bills-order-no/{companyId}")
	public ResponseEntity<ResponseViewModel> maxBillsOrderMasterNo(@PathVariable Integer companyId) {
		logger.info("Getting max bills order number for company: {}", companyId);

		try {
			ResponseViewModel response = service.maxBillsOrderMasterNo(companyId);

			if (response.isSuccess()) {
				logger.info("Successfully generated bills order number: {} for company: {}",
						response.getData1(), companyId);
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Failed to generate bills order number for company: {}", companyId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

		} catch (Exception ex) {
			logger.error("Error generating bills order number for company: {}", companyId, ex);

			ResponseViewModel errorResponse = ResponseViewModel.builder()
					.isSuccess(false)
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(ex.getMessage() != null ? ex.getMessage() : "Error generating bills order number")
					.data1("Api Details: BillsOrderMaster_MaxBillsOrderMasterNo")
					.build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Select Distinct Descriptions for a Company
	 * GET /api/bills-order/select-description/{companyId}
	 *
	 * Retrieves all distinct descriptions from BillsOrderMaster where Active != 2
	 * Descriptions are trimmed and filtered for empty/null values
	 * Results are sorted alphabetically
	 *
	 * @param companyId The Company Reference ID
	 * @return ResponseViewModel with list of descriptions in data1
	 */
	@GetMapping("/select-description/{companyId}")
	public ResponseEntity<ResponseViewModel> selectDescription(@PathVariable Integer companyId) {
		logger.info("Fetching distinct descriptions for company: {}", companyId);

		try {
			ResponseViewModel response = service.selectDescription(companyId);

			if (response.isSuccess()) {
				logger.info("Successfully retrieved {} descriptions for company: {}",
						((List<?>) response.getData1()).size(), companyId);
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Failed to retrieve descriptions for company: {}", companyId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

		} catch (Exception ex) {
			logger.error("Error fetching descriptions for company: {}", companyId, ex);

			ResponseViewModel errorResponse = ResponseViewModel.builder()
					.isSuccess(false)
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(ex.getMessage() != null ? ex.getMessage() : "Error fetching descriptions")
					.data1("Api Details: BillsOrderMaster_SelectDescription")
					.build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Select Distinct PayTo Values for a Company
	 * GET /api/bills-order/select-payment-to/{companyId}
	 *
	 * Retrieves all distinct PayTo values from BillsOrderMaster where Active != 2
	 * PayTo values are trimmed and filtered for empty/null values
	 * Results are sorted alphabetically
	 *
	 * @param companyId The Company Reference ID
	 * @return ResponseViewModel with list of PayTo values in data1
	 */
	@GetMapping("/select-payment-to/{companyId}")
	public ResponseEntity<ResponseViewModel> selectPaymentTo(@PathVariable Integer companyId) {
		logger.info("Fetching distinct PayTo values for company: {}", companyId);

		try {
			ResponseViewModel response = service.selectPaymentTo(companyId);

			if (response.isSuccess()) {
				logger.info("Successfully retrieved {} PayTo values for company: {}",
						((List<?>) response.getData1()).size(), companyId);
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Failed to retrieve PayTo values for company: {}", companyId);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

		} catch (Exception ex) {
			logger.error("Error fetching PayTo values for company: {}", companyId, ex);

			ResponseViewModel errorResponse = ResponseViewModel.builder()
					.isSuccess(false)
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(ex.getMessage() != null ? ex.getMessage() : "Error fetching PayTo values")
					.data1("Api Details: BillsOrderMaster_SelectPaymentTo")
					.build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Delete (Soft Delete) a Bills Order Master Record
	 * DELETE /api/bills-order/{id}
	 *
	 * Soft deletes a bills order by setting Active=2 flag
	 * Only deletes if the record is not locked (PStatus=0)
	 * This is equivalent to the .NET DeleteBillsOrderMaster method
	 *
	 * @param id The Bills Order Master ID to delete
	 * @param companyId The Company Reference ID (for validation)
	 * @return ResponseViewModel with success/failure status
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseViewModel> deleteBillsOrderMaster(
			@PathVariable Integer id,
			@RequestParam Integer companyId) {
		logger.info("Deleting bills order ID: {} for company: {}", id, companyId);

		try {
			ResponseViewModel response = service.deleteBillsOrderMaster(id, companyId);

			if (response.isSuccess()) {
				logger.info("Successfully deleted bills order ID: {} for company: {}", id, companyId);
				return ResponseEntity.ok(response);
			} else {
				logger.warn("Failed to delete bills order ID: {} - {}", id, response.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

		} catch (Exception ex) {
			logger.error("Error deleting bills order ID: {} for company: {}", id, companyId, ex);

			ResponseViewModel errorResponse = ResponseViewModel.builder()
					.isSuccess(false)
					.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(ex.getMessage() != null ? ex.getMessage() : "Error deleting bills order")
					.data1("Api Details: BillsOrderMaster_DeleteBillsOrderMaster")
					.build();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * Select Bills Order Master with Complex Filters
	 * GET /api/bills-order/select-bills-order
	 *
	 * Complex filtered search returning combined BillsOrderMaster and BillsOrderDetails data.
	 * Equivalent to .NET SelectBillsOrderMaster method.
	 *
	 * Request Parameters:
	 * - comid: Company ID (required)
	 * - fromdate: From date in yyyy-MM-dd format (required if no search)
	 * - todate: To date in yyyy-MM-dd format (required if no search)
	 * - id: Supplier ID (optional, 0 or null = all suppliers)
	 * - employeeid: Employee ID (optional, 0 or null = all employees)
	 * - search: Search keyword for CNumberDisplay or InvoiceNo (optional)
	 * - invoicecheck: 1 = use InvoiceDate, 0 = use SaleDate (optional, default 0)
	 * - status: Bill status filter - "Pending" or specific status (optional)
	 *
	 * @param comid Company ID (required)
	 * @param fromdate From date (required if no search)
	 * @param todate To date (required if no search)
	 * @param id Supplier ID (optional)
	 * @param employeeid Employee ID (optional)
	 * @param search Search keyword (optional)
	 * @param invoicecheck Invoice date flag (optional)
	 * @param status Bill status (optional)
	 * @return ResponseEntity with BillsOrderF5ViewDto or error message
	 */
	@GetMapping("/select-bills-order")
	public ResponseEntity<?> selectBillsOrderMaster(
			@RequestParam(value = "comid", required = false) Integer comid,
			@RequestParam(value = "fromdate", required = false) String fromdate,
			@RequestParam(value = "todate", required = false) String todate,
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "employeeid", required = false) Integer employeeid,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "invoicecheck", required = false) Integer invoicecheck,
			@RequestParam(value = "status", required = false) String status) {

		logger.info("API Call: selectBillsOrderMaster - comid: {}, fromdate: {}, todate: {}, id: {}, " +
				"employeeid: {}, search: {}, invoicecheck: {}, status: {}",
				comid, fromdate, todate, id, employeeid, search, invoicecheck, status);

		try {
			// Validate required parameters
			if (comid == null || comid <= 0) {
				logger.warn("Invalid request: comid is missing or invalid");
				return ResponseEntity.badRequest()
						.body(new Object() {
							public final boolean ok = false;
							public final String message = "Company ID (comid) is required and must be greater than 0";
						});
			}

			// Set default values
			if (invoicecheck == null) {
				invoicecheck = 0;
			}
			if (id == null) {
				id = 0;
			}
			if (employeeid == null) {
				employeeid = 0;
			}

			// Call service to fetch Bills Order Master and Details
			BillsOrderF5ViewDto resultData = service.selectBillsOrderMaster(
					comid, fromdate, todate, id, employeeid, search, invoicecheck, status);

			logger.info("Successfully retrieved {} master records and {} detail records",
					resultData.getBillsOrderMaster().size(),
					resultData.getBillsOrderDetails().size());

			// Return success response with data
			return ResponseEntity.ok(new Object() {
				public final boolean ok = true;
				public final BillsOrderF5ViewDto data = resultData;
			});

		} catch (IllegalArgumentException ex) {
			logger.warn("Invalid request: {}", ex.getMessage());
			return ResponseEntity.badRequest()
					.body(new Object() {
						public final boolean ok = false;
						public final String message = ex.getMessage();
					});

		} catch (Exception ex) {
			logger.error("Error in selectBillsOrderMaster endpoint", ex);
			return ResponseEntity.internalServerError()
					.body(new Object() {
						public final boolean ok = false;
						public final String message = "Error retrieving bills order details: " +
								(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					});
		}
	}

	/**
	 * Select Bills Order with Advanced Filtering
	 * POST /api/bills-order/select-bills-order-view
	 *
	 * Advanced filtered search for Bills Order Master with support for multiple filter criteria
	 * Returns combined BillsOrderMaster and BillsOrderDetails data (F5View)
	 * Equivalent to .NET SelectBillsOrderView method
	 *
	 * Supports filtering by:
	 * - billId: 1 = has bill, 2 = no bill, 0 = all
	 * - supplierId (Id): Filter by supplier
	 * - employeeId: Filter by employee
	 * - truckId (TId): Filter by truck
	 * - productId (DId): Filter by product
	 * - billStatus: Filter by bill status
	 * - offVesselName (Offvesselname): Filter by off-vessel name
	 * - search: Search in CNumberDisplay, InvoiceNo, SerialNo
	 * - vesselNameSearch: Filter by vessel name
	 * - fromDate: Date range start
	 * - toDate: Date range end
	 * - remarks: 1 = use InvoiceDate, 0 = use SaleDate
	 *
	 * @param filterModel SelectBillsOrderMasterRequestDto with filter criteria
	 * @return ResponseEntity with BillsOrderF5ViewDto containing master and detail records
	 */
	@PostMapping("/select-bills-order-view")
	public ResponseEntity<?> selectBillsOrderView(
			@Valid @RequestBody SelectBillsOrderMasterRequestDto filterModel) {

		logger.info("API Call: selectBillsOrderView - comid: {}, billId: {}, search: {}, supplier: {}, employee: {}",
				filterModel.getComid(), filterModel.getBillId(), filterModel.getSearch(),
				filterModel.getId(), filterModel.getEmployeeid());

		try {
			// Validate required parameters
			if (filterModel.getComid() == null || filterModel.getComid() <= 0) {
				logger.warn("Invalid request: comid is missing or invalid");
				return ResponseEntity.badRequest()
						.body(Map.of(
								"ok", false,
								"message", "Company ID (comid) is required and must be greater than 0"
						));
			}

			// Call service to fetch Bills Order Master and Details with complex filtering
			BillsOrderF5ViewDto resultData = service.selectBillsOrderView(filterModel);

			if (resultData != null) {
				logger.info("Successfully retrieved {} master records and {} detail records",
						resultData.getBillsOrderMaster().size(),
						resultData.getBillsOrderDetails().size());

				// Return success response with data
				return ResponseEntity.ok(Map.of(
						"ok", true,
						"message", "Success",
						"data", resultData
				));
			} else {
				logger.warn("No bills order records found for the given criteria");
				return ResponseEntity.ok(Map.of(
						"ok", false,
						"message", "No records found"
				));
			}

		} catch (IllegalArgumentException ex) {
			logger.warn("Invalid request in selectBillsOrderView: {}", ex.getMessage());
			return ResponseEntity.badRequest()
					.body(Map.of(
							"ok", false,
							"message", ex.getMessage()
					));

		} catch (Exception ex) {
			logger.error("Error in selectBillsOrderView endpoint", ex);
			Throwable realError = ex;
			while (realError.getCause() != null) {
				realError = realError.getCause();
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
							"ok", false,
							"message", "Error retrieving bills order data",
							"error", realError.getMessage()
					));
		}
	}

	/**
	 * Edit Bills Order Master Record
	 * GET /api/bills-order/edit-bills-order
	 *
	 * Retrieves a single bills order master record with all its details for editing.
	 * If BillsOrderMasterNo is provided, it will be used to fetch the ID.
	 * Equivalent to .NET EditBillsOrderMaster method.
	 *
	 * @param id Bills Order Master ID (optional if billsOrderMasterNo is provided)
	 * @param billsOrderMasterNo Bills Order Master Number/CNumber (optional)
	 * @param comid Company ID (required)
	 * @return ResponseEntity with BillsOrderMasterModel or error message
	 */
	@GetMapping("/edit-bills-order")
	public ResponseEntity<?> editBillsOrderMaster(
			@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "billsOrderMasterNo", required = false) Integer billsOrderMasterNo,
			@RequestParam(value = "comid", required = false) Integer comid) {

		logger.info("API Call: editBillsOrderMaster - id: {}, billsOrderMasterNo: {}, comid: {}",
				id, billsOrderMasterNo, comid);

		try {
			// Validate required parameters
			if (comid == null || comid <= 0) {
				logger.warn("Invalid request: comid is missing or invalid");
				return ResponseEntity.badRequest()
						.body(new Object() {
							public final boolean ok = false;
							public final String message = "Company ID (comid) is required and must be greater than 0";
						});
			}

			// Call service to fetch Bills Order Master record for editing
			ResponseViewModel result = service.editBillsOrderMaster(id, billsOrderMasterNo, comid);

			if (result.isSuccess()) {
				logger.info("Successfully retrieved bills order for editing");
				return ResponseEntity.ok(new Object() {
					public final boolean ok = true;
					public final String message = result.getMessage();
					public final Object data = result.getData1();
				});
			} else {
				logger.warn("Failed to retrieve bills order: {}", result.getMessage());
				return ResponseEntity.badRequest()
						.body(new Object() {
							public final boolean ok = false;
							public final String message = result.getMessage();
						});
			}

		} catch (Exception ex) {
			logger.error("Error in editBillsOrderMaster endpoint", ex);
			return ResponseEntity.internalServerError()
					.body(new Object() {
						public final boolean ok = false;
						public final String message = "Error retrieving bills order: " +
								(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
					});
		}
	}
}

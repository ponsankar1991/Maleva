package my.maleva.api.module.customer.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.dto.CustomerTinCheckRequest;
import my.maleva.api.module.customer.dto.CustomerTinCheckResult;
import my.maleva.api.module.customer.dto.response.CustomerOptionDto;
import my.maleva.api.module.customer.service.CustomerTinService;
import my.maleva.api.module.customer.dto.request.CustomerSelectRequest;
import my.maleva.api.module.customer.dto.response.CustomerSelectResult;
import my.maleva.api.module.customer.service.CustomerQneService;
import my.maleva.api.module.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@PermitAll
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerTinService tinService;
    private final CustomerQneService customerQneService;

    public CustomerController(CustomerService customerService,
                              CustomerQneService customerQneService,
                              CustomerTinService tinService) {
        this.customerService = customerService;
        this.customerQneService = customerQneService;
        this.tinService = tinService;
    }

    /* ================= QNE ================= */

    /**
     * Repair customers whose QNE code is set but whose QNE id (UpdateId) was
     * never stored — the Java port of legacy UpdateCustomerId1.
     * POST /api/customers/qne/backfill?companyId=1
     */
    @PostMapping("/qne/backfill")
    public ResponseEntity<?> qneBackfill(@RequestParam Integer companyId) {
        return QnePushResponses.toResponse(customerQneService.backfill(companyId));
    }

    /**
     * The customer list's "Push to QNE" — sends one customer the automatic push
     * after Save did not get into QNE. Create-once: a customer that already has
     * a QNE code is not sent again. Like every push endpoint, a QNE refusal
     * answers 200 with IsSuccess=false and QNE's own message; a missing or
     * deleted customer, a push already running, or QNE switched off are real
     * 4xx errors.
     * POST /api/customers/{id}/push-qne?companyId=6
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<?> pushToQne(@PathVariable Integer id, @RequestParam Integer companyId) {
        return QnePushResponses.toResponse(customerQneService.push(id, companyId));
    }

    /**
     * QNE-hosted customer statement URL for one month, addressed by the
     * customer's QNE id — gated by qne.report-view, the one QNE report gate
     * the legacy system shipped enabled.
     * GET /api/customers/{id}/qne-statement?year=2026&month=8
     */
    @GetMapping("/{id}/qne-statement")
    public ResponseEntity<?> qneStatement(
            @PathVariable Integer id,
            @RequestParam int year,
            @RequestParam int month) {
        return QnePushResponses.toResponse(customerQneService.statementUrl(id, year, month));
    }

    /**
     * The customer screen's TIN button — legacy {@code CheckCustomerTin}.
     *
     * <p>Always 200: the three outcomes (confirmed, no such taxpayer, LHDN
     * refused) are states of a completed lookup, not transport failures, and
     * the screen shows a different thing for each. Legacy answered a refusal
     * with {@code IsSuccess = false} but {@code StatusCode = Success}, which
     * agreed with neither reading.
     */
    @PostMapping("/tin-check")
    public ResponseEntity<ApiResponse<CustomerTinCheckResult>> checkTin(
            @RequestBody CustomerTinCheckRequest request,
            @RequestParam(required = false) Integer companyId
    ) {
        CustomerTinCheckResult result = tinService.check(request, companyId);
        return ResponseEntity.ok(ApiResponse.success(result.message(), result));
    }

    /**
     * Dropdown entries: active customers of the company, name-and-code
     * labelled, ordered by name. The legacy {@code GetCustomer}. Use this for
     * any combo — {@code /select} is the paged master list and is far too
     * heavy to fill one.
     */
    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<CustomerOptionDto>>> options(@RequestParam Integer companyId) {
        List<CustomerOptionDto> options = customerService.options(companyId);
        return ResponseEntity.ok(ApiResponse.success(options.size() + " customer(s)", options));
    }

    /* ================= CREATE ================= */

    /**
     * Creates a customer, then answers with the row as it stands once the QNE
     * push has run.
     *
     * <p>The re-read is the point. {@code create} commits and only then pushes
     * to QNE, so the object it returns cannot yet carry the QNE identity the
     * push writes back. Reading the row here — after the transactional call has
     * returned, and therefore after the after-commit push — means CompanyCode
     * and UpdateId are populated when the push worked and blank when it did
     * not, which is how the screen knows to warn.
     *
     * <p>Legacy answered a failed QNE push with {@code IsSuccess = false} and
     * the QNE message, for a customer it had already committed. The screen then
     * kept EditId at 0, so pressing Save again created a <i>second</i> customer.
     * That is deliberately not copied: the save succeeded and is reported as
     * such, with the QNE state visible in the response.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> create(
            @Valid @RequestBody CustomerDto dto
    ) {
        CustomerDto created = customerService.create(dto);
        CustomerDto saved = customerService.getById(created.getId());

        return ResponseEntity
                .created(URI.create("/api/customers/" + saved.getId()))
                .body(ApiResponse.success(
                        "Customer created successfully",
                        saved
                ));
    }

    /* ================= UPDATE ================= */

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> update(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerDto dto
    ) {
        CustomerDto updated = customerService.update(id, dto);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer updated successfully",
                        updated
                )
        );
    }



    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @PathVariable Integer id
    )
    {
        customerService.softDelete(id);
        return ResponseEntity.ok(
                ApiResponse.success("Customer deleted", null)
        );
    }

    /* ================= GET BY ID ================= */

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> get(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer fetched successfully",
                        customerService.getById(id)
                )
        );
    }

    /* ================= SIMPLE LIST ================= */

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto>>> list(
            @RequestParam(value = "name", required = false) String name
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer list fetched successfully",
                        customerService.findAll(name)
                )
        );
    }
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<?>> selectCustomer(
            @RequestBody CustomerSelectRequest request

    ) {
        CustomerSelectResult result = customerService.selectCustomer(request);

        return ResponseEntity.ok(ApiResponse.success("Customer fetched successfully", result.getCustomers(), Map.of("count", result.getTotalCount()))
        );
    }





}

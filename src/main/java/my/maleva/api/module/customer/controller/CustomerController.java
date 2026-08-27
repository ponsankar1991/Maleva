package my.maleva.api.module.customer.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.customer.dto.CustomerDto;
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
    private final CustomerQneService customerQneService;

    public CustomerController(CustomerService customerService, CustomerQneService customerQneService) {
        this.customerService = customerService;
        this.customerQneService = customerQneService;
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

    /* ================= CREATE ================= */

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> create(
            @Valid @RequestBody CustomerDto dto
    ) {
        CustomerDto created = customerService.create(dto);

        return ResponseEntity
                .created(URI.create("/api/customers/" + created.getId()))
                .body(ApiResponse.success(
                        "Customer created successfully",
                        created
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

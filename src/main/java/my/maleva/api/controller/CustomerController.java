package my.maleva.api.controller;

import my.maleva.api.common.ApiResponse;
import my.maleva.api.dto.CustomerDto;
import my.maleva.api.dto.request.CustomerSelectRequest;
import my.maleva.api.dto.response.CustomerInsertResult;
import my.maleva.api.dto.response.CustomerSelectResult;
import my.maleva.api.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
        CustomerSelectResult result =
                customerService.selectCustomer(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer fetched successfully",
                        result.getCustomers(),
                        Map.of("count", result.getTotalCount())
                )
        );
    }





}

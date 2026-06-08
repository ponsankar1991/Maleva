package my.maleva.api.module.qutation.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.qutation.dto.CustomerQuotationDto;
import my.maleva.api.module.qutation.service.CustomerQuotationService;
import my.maleva.api.common.dto.ResponseViewModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-quotations")
@Validated
@PermitAll
public class CustomerQuotationController {

    private final CustomerQuotationService service;

    public CustomerQuotationController(CustomerQuotationService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerQuotationDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerQuotationDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerQuotationDto> create(@Valid @RequestBody CustomerQuotationDto dto) {
        CustomerQuotationDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-quotations/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerQuotationDto update(@PathVariable Integer id, @Valid @RequestBody CustomerQuotationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint for the legacy SelectCustomerQuotationFormat2 logic
     * Example: GET /api/customer-quotations/format2?id=1&jobId=2&comid=3&port=ABC&quantity=1
     */
    @GetMapping("/format2")
    public ResponseEntity<ResponseViewModel> selectCustomerQuotationFormat2(
            @RequestParam("id") Integer id,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("comid") Integer comid,
            @RequestParam("port") String port,
            @RequestParam(value = "quantity", required = false) Integer quantity) {

        ResponseViewModel response = service.selectCustomerQuotationFormat2(id, jobId, comid, port, quantity == null ? 0 : quantity);
        return ResponseEntity.status(response.getStatusCode() != null ? response.getStatusCode() : HttpStatus.OK.value())
                .body(response);
    }
}

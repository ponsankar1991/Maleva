package my.maleva.api.module.fleet.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.PassEntryDetailDto;
import my.maleva.api.module.fleet.dto.PassEntryListResponse;
import my.maleva.api.module.fleet.dto.RtiOptionDto;
import my.maleva.api.module.fleet.dto.request.PassEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;
import my.maleva.api.module.fleet.service.PassEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * The seven endpoints a truck pass screen needs.
 *
 * Levi and auto pass entries had byte-identical legacy controllers, so the
 * routes are declared once here and each subclass supplies only its base path,
 * its service and its label. Spring maps the inherited handler methods onto
 * whatever {@code @RequestMapping} the concrete controller carries.
 *
 * Reads are GETs with query parameters rather than the legacy POSTs with a JSON
 * body, and the company id travels as a parameter instead of the {@code Comid}
 * header.
 *
 * Attachments are not handled here: both screens file them through
 * {@code /api/attachments}.
 */
public abstract class AbstractPassEntryController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPassEntryController.class);

    /** The screen's service. */
    protected abstract PassEntryService service();

    /** Human name used in response messages, e.g. "Levi entry". */
    protected abstract String documentLabel();

    /**
     * The list with its total.
     * Legacy equivalent: POST /&lt;Doc&gt;/Select&lt;Doc&gt;
     *
     * <p>A {@code search} value is an exact document number and overrides every
     * other filter, including the date range.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PassEntryListResponse>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) Integer driverRefId,
            @RequestParam(required = false) Integer rtiRefId,
            @RequestParam(required = false) Integer employeeRefId,
            @RequestParam(required = false) String enterLink,
            @RequestParam(required = false) String exitLink,
            @RequestParam(required = false) String search) {

        PassEntryListResponse data = service().search(PassEntrySearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(fromDate)
                .toDate(toDate)
                .truckRefId(truckRefId)
                .driverRefId(driverRefId)
                .rtiRefId(rtiRefId)
                .employeeRefId(employeeRefId)
                .enterLink(enterLink)
                .exitLink(exitLink)
                .search(search)
                .build());

        return ResponseEntity.ok(ApiResponse.success(data, documentLabel() + " list retrieved"));
    }

    /**
     * The next document number to show on a blank form.
     * Legacy equivalent: POST /&lt;Doc&gt;/Max&lt;Doc&gt;No
     */
    @GetMapping("/next-no")
    public ResponseEntity<ApiResponse<String>> nextNumber(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service().nextNumber(companyRefId), "Next " + documentLabel() + " number"));
    }

    /**
     * Options for the RTI dropdown.
     * Legacy equivalent: POST /&lt;Doc&gt;/SelectRTINo
     */
    @GetMapping("/rti-options")
    public ResponseEntity<ApiResponse<List<RtiOptionDto>>> rtiOptions(
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service().rtiOptions(companyRefId), "RTI options retrieved"));
    }

    /**
     * One entry for the edit form. Pass {@code documentNumber} to open by the
     * printed number instead of the id.
     * Legacy equivalent: POST /&lt;Doc&gt;/Edit&lt;Doc&gt;
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PassEntryDetailDto>> getForEdit(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer documentNumber) {

        return ResponseEntity.ok(
                ApiResponse.success(service().getForEdit(id, documentNumber, companyRefId),
                        documentLabel() + " retrieved"));
    }

    /**
     * The data behind the print action. Rendering still belongs to the .NET
     * report server, as it does for toll entries and RTI.
     * Legacy equivalent: POST /&lt;Doc&gt;/&lt;Doc&gt;VIEW
     */
    @GetMapping("/{id}/print-data")
    public ResponseEntity<ApiResponse<PassEntryDetailDto>> getForPrint(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        return ResponseEntity.ok(
                ApiResponse.success(service().getForPrint(id, companyRefId),
                        documentLabel() + " print data"));
    }

    /**
     * Creates when {@code id} is absent, updates when present.
     * Legacy equivalent: POST /&lt;Doc&gt;/Insert&lt;Doc&gt;
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PassEntryDetailDto>> save(
            @Valid @RequestBody PassEntrySaveRequest request,
            Authentication authentication) {

        PassEntryDetailDto saved = service().save(request, usernameOf(authentication));
        logger.info("Saved {} {} ({})", documentLabel(), saved.getId(), saved.getCNumberDisplay());
        return ResponseEntity.ok(ApiResponse.success(saved, documentLabel() + " saved"));
    }

    /**
     * Soft delete.
     * Legacy equivalent: POST /&lt;Doc&gt;/Delete&lt;Doc&gt;
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            Authentication authentication) {

        service().delete(id, companyRefId, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, documentLabel() + " deleted"));
    }

    private String usernameOf(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}

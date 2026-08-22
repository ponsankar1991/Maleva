package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.FuelEntryDetailDto;
import my.maleva.api.module.fleet.dto.FuelEntryListResponse;
import my.maleva.api.module.fleet.dto.request.FuelEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.FuelEntrySearchRequest;
import my.maleva.api.module.fleet.service.FuelEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Fuel entries.
 *
 * Replaces the legacy /FuelEntry/* MVC actions. Two shape changes worth
 * knowing when porting the screen:
 *
 * <ul>
 *   <li>reads are GET with query parameters, not POST with a JSON body;</li>
 *   <li>the company id travels as a parameter rather than the {@code Comid}
 *       header the legacy InsertFuelEntry action read.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/fuel-entries")
@Validated
@PermitAll
public class FuelEntryController {

    private static final Logger logger = LoggerFactory.getLogger(FuelEntryController.class);

    private final FuelEntryService service;

    public FuelEntryController(FuelEntryService service) {
        this.service = service;
    }

    /**
     * The fuel entry list with its totals.
     * Legacy equivalent: POST /FuelEntry/SelectFuelEntry
     *
     * <p>A {@code search} value looks the entry up by fuel number and ignores
     * the date range, which is how the legacy screen found a document by number.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<FuelEntryListResponse>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) Integer driverRefId,
            @RequestParam(required = false) Integer employeeRefId,
            @RequestParam(required = false) String search) {

        FuelEntryListResponse data = service.search(FuelEntrySearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(fromDate)
                .toDate(toDate)
                .truckRefId(truckRefId)
                .driverRefId(driverRefId)
                .employeeRefId(employeeRefId)
                .search(search)
                .build());

        return ResponseEntity.ok(ApiResponse.success(data, "Fuel entries retrieved"));
    }

    /**
     * The next fuel number to show on a blank form.
     * Legacy equivalent: POST /FuelEntry/MaxFuelEntryNo
     */
    @GetMapping("/next-no")
    public ResponseEntity<ApiResponse<String>> nextFuelNumber(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service.nextFuelNumber(companyRefId), "Next fuel number"));
    }

    /**
     * One entry with the GPS filling assigned to it, for the edit form.
     * Legacy equivalent: POST /FuelEntry/EditFuelEntry
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FuelEntryDetailDto>> getForEdit(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer fuelNumber) {

        FuelEntryDetailDto data = service.getForEdit(id, fuelNumber, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(data, "Fuel entry retrieved"));
    }

    /**
     * The data behind the print action. The rendering itself still belongs to
     * the .NET report server, as it does for RTI and Planning.
     * Legacy equivalent: POST /FuelEntry/FuelEntryVIEW
     */
    @GetMapping("/{id}/print-data")
    public ResponseEntity<ApiResponse<FuelEntryDetailDto>> getForPrint(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        return ResponseEntity.ok(
                ApiResponse.success(service.getForPrint(id, companyRefId), "Fuel entry print data"));
    }

    /**
     * Creates or updates an entry.
     * Legacy equivalent: POST /FuelEntry/InsertFuelEntry
     *
     * <p>The derived amounts are recomputed from the litres and the rate, so
     * anything the client sends for them is ignored.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FuelEntryDetailDto>> save(
            @Valid @RequestBody FuelEntrySaveRequest request,
            Authentication authentication) {

        FuelEntryDetailDto saved = service.save(request, usernameOf(authentication));
        logger.info("Saved fuel entry {} for company {}", saved.getId(), request.getCompanyRefId());
        return ResponseEntity.ok(ApiResponse.success(saved, "Fuel entry saved"));
    }

    /**
     * Soft delete.
     * Legacy equivalent: POST /FuelEntry/DeleteFuelEntry
     *
     * @param mobile true restricts the delete to driver-app rows
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            @RequestParam(defaultValue = "false") boolean mobile,
            Authentication authentication) {

        service.delete(id, companyRefId, mobile, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "Fuel entry deleted"));
    }

    private String usernameOf(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}

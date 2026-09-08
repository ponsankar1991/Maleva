package my.maleva.api.module.rti.batch.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchPreview;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchRequest;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchResult;
import my.maleva.api.module.rti.batch.service.PlanningRtiBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * "Create every RTI in this plan" — the preview, and the confirm.
 *
 * <p>Two calls on purpose. The preview writes nothing, so a planner can look at
 * what a click would do and close it again; the confirm writes everything in one
 * transaction. Splitting them is what makes a single click safe on a plan that
 * covers a dozen trucks.
 */
@RestController
@RequestMapping("/api/planing")
public class PlanningRtiBatchController {

    private static final Logger logger = LoggerFactory.getLogger(PlanningRtiBatchController.class);

    private final PlanningRtiBatchService service;

    public PlanningRtiBatchController(PlanningRtiBatchService service) {
        this.service = service;
    }

    /**
     * What one click would create. Read-only.
     *
     * <p>{@code GET /api/planing/{planningId}/rti-batch/preview?companyId=6}, and
     * optionally {@code &jobIds=101,102} when the planner ticked rows. Ticked or
     * not, the grouping is identical — one RTI per truck and driver.
     */
    @GetMapping("/{planningId}/rti-batch/preview")
    @PermitAll
    public ResponseEntity<ApiResponse<PlanningRtiBatchPreview>> preview(
            @PathVariable Integer planningId,
            @RequestParam Integer companyId,
            @RequestParam(required = false) java.util.List<Integer> jobIds,
            @RequestParam(required = false, defaultValue = "false") boolean includeExisting) {

        PlanningRtiBatchPreview preview = service.preview(planningId, companyId, jobIds, includeExisting);
        logger.info("PLAN_RTI_PREVIEW: plan {} -> {} RTI, {} job(s) to create, {} skipped",
                planningId, preview.groups().size(), preview.jobsToCreate(), preview.jobsSkipped());
        return ResponseEntity.ok(ApiResponse.success(preview, "RTI preview ready"));
    }

    /**
     * Creates the confirmed RTIs. All of them, or none.
     *
     * <p>{@code POST /api/planing/{planningId}/rti-batch}
     */
    @PostMapping("/{planningId}/rti-batch")
    @PermitAll
    public ResponseEntity<ApiResponse<PlanningRtiBatchResult>> create(
            @PathVariable Integer planningId,
            @Valid @RequestBody PlanningRtiBatchRequest request) {

        PlanningRtiBatchResult result = service.create(planningId, request);
        String message = result.created().size() + " RTI created covering "
                + result.jobsCreated() + " job(s)";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
}

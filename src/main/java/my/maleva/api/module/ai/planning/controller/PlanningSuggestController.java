package my.maleva.api.module.ai.planning.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestFeedbackRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestRequest;
import my.maleva.api.module.ai.planning.dto.PlanningSuggestResponse;
import my.maleva.api.module.ai.planning.service.PlanningSuggestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Truck and driver suggestions for the Planning grid. Company scope comes
 * from the body or the {@code Comid} header, like the planning API itself.
 */
@RestController
@RequestMapping("/api/ai/planning")
@RequiredArgsConstructor
@Tag(name = "AI - Planning", description = "Suggests trucks and drivers for planning rows from the company's history and flags conflicts")
public class PlanningSuggestController {

    private final PlanningSuggestService service;

    @PostMapping("/suggest")
    @Operation(summary = "Suggest truck and driver per row",
            description = "Ranks trucks and drivers from past plans (customer, lane, port, continuity, pairing) and the day's load; "
                    + "rows that already have both are skipped unless replaceExisting is true.")
    public ResponseEntity<ApiResponse<PlanningSuggestResponse>> suggest(
            @RequestBody PlanningSuggestRequest request,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        if (request.getCompanyRefId() == null) {
            request.setCompanyRefId(comid);
        }
        return ResponseEntity.ok(ApiResponse.success(service.suggest(request), "Suggestions ready - review before saving"));
    }

    @PostMapping("/suggest/feedback")
    @Operation(summary = "Record what the planner saved", description = "Stores the chosen truck and driver against the suggestion so the ranking can be tuned")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> feedback(
            @RequestBody PlanningSuggestFeedbackRequest request,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        if (request.getCompanyRefId() == null) {
            request.setCompanyRefId(comid);
        }
        int recorded = service.feedback(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("recorded", recorded), "Feedback recorded"));
    }
}

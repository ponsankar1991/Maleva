package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.master.dto.ClassificationDto;
import my.maleva.api.module.master.service.ClassificationService;
import my.maleva.api.common.dto.ApiResponse;
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

@RestController
@RequestMapping("/api/classifications")
@Validated
@PermitAll
public class ClassificationController {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationController.class);
    private final ClassificationService service;

    public ClassificationController(ClassificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClassificationDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ClassificationDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ClassificationDto> create(@Valid @RequestBody ClassificationDto dto) {
        ClassificationDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/classifications/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ClassificationDto update(@PathVariable Integer id, @Valid @RequestBody ClassificationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Select all classifications - ASP.NET SelectClasification() migration endpoint
     *
     * Maps to ASP.NET SelectClasification method from AccountsGroupMasterController
     * Returns all classifications wrapped in ApiResponse format
     *
     * HTTP Method: POST
     * Endpoint: /api/classifications/select
     *
     * Example Response:
     * {
     *   "IsSuccess": true,
     *   "StatusCode": 200,
     *   "Message": "Success",
     *   "Data1": [
     *     {
     *       "id": 1,
     *       "classificationCode": 100,
     *       "description": "Assets"
     *     }
     *   ]
     * }
     *
     * @return ResponseEntity with ApiResponse wrapper containing list of classifications
     */
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<List<ClassificationDto>>> selectClassification() {
        logger.info("API Call: SelectClassification - Retrieving all classifications");

        try {
            // Call service layer to fetch all classifications
            List<ClassificationDto> classifications = service.selectAllClassifications();

            logger.debug("Retrieved {} classifications from database", classifications.size());

            // Return success response wrapped in ApiResponse
            ApiResponse<List<ClassificationDto>> response = ApiResponse.success(
                classifications,
                "Success"
            );

            logger.info("Returning {} classifications to client", classifications.size());
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

        } catch (Exception ex) {
            // Extract innermost exception message (matching ASP.NET pattern)
            Exception realError = ex;
            while (realError.getCause() instanceof Exception) {
                realError = (Exception) realError.getCause();
            }

            String errorMessage = realError.getMessage() != null ? realError.getMessage() : "Unknown error";
            logger.error("Error in selectClassification: {}", errorMessage, ex);

            // Return error response
            ApiResponse<List<ClassificationDto>> errorResponse = ApiResponse.error(
                "Error retrieving classifications: " + errorMessage,
                500
            );

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}

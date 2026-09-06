package my.maleva.api.module.saleorder.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.saleorder.dto.DoConvertResult;
import my.maleva.api.module.saleorder.service.SaleOrderDoConvertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prepare a delivery order from a sale order.
 * {@code POST /api/sale-orders/{id}/do-convert?companyId=1}
 *
 * <p>Replaces the legacy {@code /SaleOrder/DoConvert}. A DO that could not
 * be prepared answers 200 with {@code IsSuccess=false} and the reason, the
 * same mixed signal the other action endpoints use, so the screen shows the
 * message instead of a generic failure.
 */
@RestController
@RequestMapping("/api/sale-orders")
@PermitAll
@RequiredArgsConstructor
public class SaleOrderDoConvertController {

    private final SaleOrderDoConvertService doConvertService;

    @PostMapping("/{id}/do-convert")
    public ResponseEntity<ApiResponse<DoConvertResult>> doConvert(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        DoConvertResult result = doConvertService.convert(id, companyId);
        if (!result.ok()) {
            ApiResponse<DoConvertResult> body = ApiResponse.error(result.message(), 200);
            body.setData1(result);
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(ApiResponse.success(result, result.message()));
    }
}

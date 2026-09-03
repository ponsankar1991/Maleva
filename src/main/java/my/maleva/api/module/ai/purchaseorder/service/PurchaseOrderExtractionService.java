package my.maleva.api.module.ai.purchaseorder.service;

import my.maleva.api.module.ai.purchaseorder.dto.PurchaseOrderExtractionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PurchaseOrderExtractionService {

    /**
     * Reads a supplier quotation, proforma, invoice or delivery order (PDF or
     * image) with the company's chosen AI provider and resolves supplier,
     * payment terms, GL accounts, truck, driver and store items against the
     * company's masters.
     *
     * @param providerKey optional explicit provider (used by the settings screen to test one)
     */
    PurchaseOrderExtractionResponse extract(Integer companyRefId, MultipartFile file, String providerKey);
}

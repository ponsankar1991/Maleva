package my.maleva.api.module.ai.billextraction.service;

import my.maleva.api.module.ai.billextraction.dto.BillExtractionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BillExtractionService {

    /**
     * Reads a supplier bill (PDF or image) with the company's chosen AI
     * provider and resolves supplier, payment terms and GL accounts against
     * the company's masters.
     *
     * @param providerKey optional explicit provider (used by the settings screen to test one)
     */
    BillExtractionResponse extract(Integer companyRefId, MultipartFile file, String providerKey);
}

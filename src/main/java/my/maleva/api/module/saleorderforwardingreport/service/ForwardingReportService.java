package my.maleva.api.module.saleorderforwardingreport.service;

import my.maleva.api.module.saleorderforwardingreport.dto.ExcelImportResultDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingDateUpdateRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportSearchRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingS1OptionsDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ZbReportRowDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/** The sale order forwarding report screen: search, filters, re-date, import. */
public interface ForwardingReportService {

    /** The forwarding grid — one row per populated leg of each matching order. */
    List<ForwardingReportRowDto> searchForwarding(ForwardingReportSearchRequest request);

    /** The ZB grid — one row per matching order. */
    List<ZbReportRowDto> searchZb(ForwardingReportSearchRequest request);

    /** Options for the six S1/S2 filter dropdowns. */
    ForwardingS1OptionsDto getS1Options(Integer comId);

    /** Vessel names seen on either side of the company's orders. */
    List<String> getVesselNames(Integer comId);

    /**
     * Re-date one forwarding leg.
     *
     * @return true when a row was actually updated; false when the job id and
     *         company did not match any order
     */
    boolean updateForwardingDate(ForwardingDateUpdateRequest request);

    /** Apply a customs acknowledgement spreadsheet; see {@link #importExcel}. */
    ExcelImportResultDto importExcel(Integer comId, MultipartFile file);

    /** Stream overload, for tests that do not want a MultipartFile. */
    ExcelImportResultDto importExcel(Integer comId, InputStream stream, String originalFilename);
}

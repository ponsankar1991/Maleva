package my.maleva.api.module.paymentrecept;

import my.maleva.api.module.paymentrecept.dto.ReceiptEditDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptMailInfoDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptSearchRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptViewDto;
import my.maleva.api.module.paymentrecept.mail.ReceiptMailService;
import my.maleva.api.module.paymentrecept.print.ReceiptPdfService;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Developer tool, not a test: runs the Receipt screen's read paths (view
 * grid, edit load, voucher PDF, mail window info) for one real receipt
 * through the full Spring context against the configured database, so the
 * ported queries are proven on live rows before the screen is used.
 *
 * <pre>
 * mvn -o -q test -Dtest=ReceiptLiveTool -Dsurefire.failIfNoSpecifiedTests=false \
 *     -Dlive.receipt.company=6 [-Dlive.receipt.id=2284] [-Dlive.receipt.out=C:/tmp]
 * </pre>
 *
 * Without {@code live.receipt.id} the newest receipt of the company is used.
 * Nothing is written to the database and no mail is sent.
 */
@SpringBootTest
class ReceiptLiveTool {

    @Autowired
    private ReceiptService receiptService;
    @Autowired
    private ReceiptPdfService pdfService;
    @Autowired
    private ReceiptMailService mailService;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @EnabledIfSystemProperty(named = "live.receipt.company", matches = "\\d+")
    void run() throws Exception {
        int companyId = Integer.parseInt(System.getProperty("live.receipt.company"));
        String idProperty = System.getProperty("live.receipt.id", "");
        Map<String, Object> picked = idProperty.isBlank()
                ? jdbc.queryForMap("SELECT TOP 1 Id, CNumberDisplay, FORMAT(ReceiptDate,'yyyy-MM-dd') AS D FROM Receipt WITH (NOLOCK) WHERE CompanyRefId = ? ORDER BY Id DESC", companyId)
                : jdbc.queryForMap("SELECT Id, CNumberDisplay, FORMAT(ReceiptDate,'yyyy-MM-dd') AS D FROM Receipt WITH (NOLOCK) WHERE Id = ?", Integer.parseInt(idProperty));
        int receiptId = ((Number) picked.get("Id")).intValue();
        String date = String.valueOf(picked.get("D"));
        System.out.println("=== receipt " + receiptId + " " + picked.get("CNumberDisplay") + " dated " + date);

        // 1. the view grid over that day, then the exact-number search
        ReceiptViewDto byDate = receiptService.search(ReceiptSearchRequest.builder()
                .companyId(companyId).fromDate(date).toDate(date).customerId(0).employeeId(0).search("").build());
        System.out.println("=== view by date: " + byDate.getCount() + " receipt(s), total " + byDate.getTotalAmount()
                + ", " + byDate.getReceiptDetails().size() + " detail line(s)");
        byDate.getReceiptMaster().stream().limit(5).forEach(r -> System.out.println("    " + r));
        ReceiptViewDto byNo = receiptService.search(ReceiptSearchRequest.builder()
                .companyId(companyId).search(String.valueOf(picked.get("CNumberDisplay"))).build());
        System.out.println("=== view by number: " + byNo.getCount() + " receipt(s), total " + byNo.getTotalAmount());
        byNo.getReceiptDetails().forEach(d -> System.out.println("    " + d));

        // 2. edit load: the customer's outstanding list with this receipt's amounts merged in
        ReceiptEditDto edit = receiptService.edit(companyId, receiptId, null).orElseThrow();
        System.out.println("=== edit: " + edit.getCNumberDisplay() + " customer " + edit.getCustomerRefId()
                + " amount " + edit.getAmount() + " rate " + edit.getCurrencyValue()
                + " rows " + edit.getReceiptDetails().size());
        edit.getReceiptDetails().stream().filter(r -> r.getAmount() != null && r.getAmount().signum() != 0)
                .forEach(r -> System.out.println("    settles " + r.getBillNo() + " balance " + r.getBalance() + " amount " + r.getAmount()));

        // 3. the voucher PDF
        ReceiptPdfService.RenderedReceipt rendered = pdfService.render(receiptId, companyId).orElseThrow();
        System.out.println("=== rendered " + rendered.fileName() + " (" + rendered.pdf().length + " bytes)");
        String out = System.getProperty("live.receipt.out");
        if (out != null && !out.isBlank()) {
            Path target = Path.of(out, rendered.fileName());
            Files.write(target, rendered.pdf());
            System.out.println("=== written " + target);
        }

        // 4. the mail window prefill (no mail is sent)
        ReceiptMailInfoDto info = mailService.info(receiptId, companyId).orElseThrow();
        System.out.println("=== mail info: " + info);
    }
}

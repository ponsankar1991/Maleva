package my.maleva.api.module.invoice.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the request binding of the sale-invoice save.
 *
 * <p>The React screens still speak the legacy PascalCase names the .NET pages
 * used, so the whole save depends on the mapper matching {@code CompanyRefId}
 * to {@code companyRefId}. On 2026-09-09 it did not: {@code JacksonConfig}
 * enables ACCEPT_CASE_INSENSITIVE_PROPERTIES on a <em>Jackson 2</em> mapper,
 * while Spring Boot 4 binds request bodies with <em>Jackson 3</em>. Nothing
 * bound, so every save came back "Company Reference ID is required" over a
 * payload that plainly contained it.
 *
 * <p>The fix is {@code spring.jackson.mapper.accept-case-insensitive-properties}
 * in application.yaml. This test is what proves it is still in force: it binds
 * with the container's real mapper, not a hand-built one, so deleting the
 * property breaks the test rather than production.
 */
@JsonTest
class SaleInvoiceRequestBindingTest {

    /**
     * The application class carries {@code @EnableCaching}, which a JSON slice
     * does not give a cache manager. Nothing here caches; this only lets the
     * slice start.
     */
    @TestConfiguration
    static class CacheStub {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private ObjectMapper mapper;

    /** Trimmed from a payload that failed for real on 2026-09-09. */
    private static final String LEGACY_PAYLOAD = """
            {
              "Id": 0,
              "CompanyRefId": 6,
              "UserRefId": null,
              "EmployeeRefId": 14,
              "CustomerRefId": 138,
              "JobMasterRefId": 8,
              "AgentCompanyRefId": 18,
              "AgentMasterRefId": 149,
              "SaleDate": "2026-09-09",
              "BillType": "MY",
              "SaleOrderMasterNo": 19730,
              "ClientRequestId": "0131d34b-6a60-47ce-be48-d9d6b1178822",
              "SymbolRefId": 4,
              "JStatus": 12,
              "Amount": 1,
              "GrossAmount": 1,
              "ActualNetAmount": 1,
              "CurrencyValue": 1,
              "TaxAmount": 0,
              "AWBNo": " 54 7628 9770",
              "BLCopy": "",
              "SCN": "",
              "SPort": "WESTPORT-B18",
              "OPort": "",
              "DODescription": "",
              "Offvesselname": "",
              "Loadingvesselname": "MTT HAIPHONG",
              "OVessel": "",
              "Vessel": "CONTAINER VESSEL",
              "Commodity": "SHIP SPARE",
              "Cargo": "NOT ARRIVED",
              "Origin": "WESTPORT",
              "Destination": "WESTPORT",
              "Quantity": "1 PKG",
              "TotalWeight": "4 KG",
              "ForkliftbyRefid": null,
              "SealbyRefid": null,
              "BoardingOfficerRefid": null,
              "ForwardingSMKNo": "",
              "ETA": null,
              "OETA": null,
              "PickupDate": null,
              "Zb": "",
              "ZbRef": "",
              "PTW": "",
              "CNumber": 0,
              "CNumberDisplay": "INV000044007",
              "SaleType": "CREDIT",
              "details": [
                {
                  "itemMasterRefId": 54,
                  "itemQty": 1,
                  "salesRate": 1,
                  "taxRefId": 0,
                  "saleOrderMasterRefId": 19730
                }
              ],
              "saleOrderRefIds": [19730]
            }
            """;

    @Test
    void theLegacyPascalCasePayloadBinds() {
        SaleInvoiceRequestDTO request = mapper.readValue(LEGACY_PAYLOAD, SaleInvoiceRequestDTO.class);

        // The four @NotNull fields the 2026-09-09 failure reported as null.
        assertThat(request.getCompanyRefId()).isEqualTo(6);
        assertThat(request.getCustomerRefId()).isEqualTo(138);
        assertThat(request.getJobMasterRefId()).isEqualTo(8);
        assertThat(request.getSaleDate()).isEqualTo(LocalDate.of(2026, 9, 9));
    }

    @Test
    void theNamesThatDifferByMoreThanTheFirstLetterBindToo() {
        SaleInvoiceRequestDTO request = mapper.readValue(LEGACY_PAYLOAD, SaleInvoiceRequestDTO.class);

        // The risky ones: the legacy spelling is not simply the camelCase name
        // with a capital first letter.
        assertThat(request.getLoadingVesselName()).isEqualTo("MTT HAIPHONG");   // Loadingvesselname
        assertThat(request.getAwbNo()).isEqualTo(" 54 7628 9770");              // AWBNo
        assertThat(request.getJStatus()).isEqualTo(12);                         // JStatus
        assertThat(request.getSaleOrderMasterNo()).isEqualTo(19730);            // SaleOrderMasterNo
        assertThat(request.getBillType()).isEqualTo("MY");
    }

    @Test
    void theDuplicateSaveGuardKeyBinds() {
        SaleInvoiceRequestDTO request = mapper.readValue(LEGACY_PAYLOAD, SaleInvoiceRequestDTO.class);

        // Without this the InvoiceSaveGuard is silently skipped and a second
        // click writes a second invoice.
        assertThat(request.getClientRequestId()).isEqualTo("0131d34b-6a60-47ce-be48-d9d6b1178822");
    }

    @Test
    void theNamesTheFrontEndAlreadyTranslatesStillBind() {
        SaleInvoiceRequestDTO request = mapper.readValue(LEGACY_PAYLOAD, SaleInvoiceRequestDTO.class);

        assertThat(request.getSaleOrderRefIds()).isEqualTo(List.of(19730));
        assertThat(request.getDetails()).hasSize(1);
        assertThat(request.getDetails().get(0).getItemMasterRefId()).isEqualTo(54);
    }
}

package my.maleva.api.module.supplier.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the JSON seam between the React supplier screen and the server, with
 * the container's own Jackson 3 mapper.
 *
 * <p>SupplierDto is a Lombok class, so {@code getOEmail()} is the property
 * {@code oemail}, not {@code oEmail}. Requests only bind because
 * {@code spring.jackson.mapper.accept-case-insensitive-properties} is on, and
 * responses go out with the lower-cased names — which is why the screen's
 * {@code fromSupplierDto} matches keys case-insensitively. Six of the boxes
 * that ride on these names hold a TIN, an SST number, an MSIC code and a bank
 * account, so a silent miss here blanks real data on the next Save.
 */
@JsonTest
class SupplierRequestBindingTest {

    /** The application class carries {@code @EnableCaching}; the JSON slice has no cache manager. */
    @TestConfiguration
    static class CacheStub {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private ObjectMapper mapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /** Exactly the keys and value shapes supplier.contract.ts {@code toSupplierDto} emits. */
    private static final String SCREEN_PAYLOAD = """
            {
              "id": 402,
              "companyRefId": 6,
              "supplierName": "O'Neill Haulage",
              "supplierType": "ALL",
              "personId": "roc-12",
              "address1": "Lot 5, Jalan Kem",
              "address2": "",
              "address3": "",
              "city": "Ms Tan",
              "supplierCity": "Klang",
              "state": "Selangor",
              "zipcode": "42000",
              "country": "10",
              "symbolRefid": 2,
              "paymentTermsRefid": 3,
              "gstNo": "",
              "email": "",
              "mobileNo": "012-3456789",
              "userName": "oneill",
              "password": "Pw-7",
              "aEmail": "accounts@oneill.example",
              "aName": "Raj",
              "aEmail1": "C2584563222",
              "aPhone": "W10-1808-32000123",
              "oEmail": "49301",
              "oEmail1": "02",
              "oName": "MAYBANK",
              "oPhone": "514011223344",
              "selfBilled": 0,
              "tinType": "Company (Malaysia)",
              "supplierTin": "C2584563222",
              "msicCodeRefId": 0,
              "taxExemptionNo": "",
              "expiryDate": "2027-03-31",
              "taxExemptionDetails": "",
              "registrationNo": "201901012345",
              "bankName": "Maybank",
              "accountNo": "5140",
              "tinNo": "C123",
              "sstNo": "SST-1",
              "msicCode": "49301",
              "serviceTaxType": "01",
              "active": 1
            }
            """;

    @Test
    void theLombokCasedContactColumnsBind() {
        SupplierDto dto = mapper.readValue(SCREEN_PAYLOAD, SupplierDto.class);

        // The boxes labelled TIN NO, SST REG NO, MSIC CODE, SERVICE TAX TYPE, BANK NAME, ACCOUNT NUMBER.
        assertThat(dto.getAEmail1()).isEqualTo("C2584563222");
        assertThat(dto.getAPhone()).isEqualTo("W10-1808-32000123");
        assertThat(dto.getOEmail()).isEqualTo("49301");
        assertThat(dto.getOEmail1()).isEqualTo("02");
        assertThat(dto.getOName()).isEqualTo("MAYBANK");
        assertThat(dto.getOPhone()).isEqualTo("514011223344");
        assertThat(dto.getAEmail()).isEqualTo("accounts@oneill.example");
        assertThat(dto.getAName()).isEqualTo("Raj");
    }

    @Test
    void everyOtherScreenFieldBinds() {
        SupplierDto dto = mapper.readValue(SCREEN_PAYLOAD, SupplierDto.class);

        assertThat(dto.getCompanyRefId()).isEqualTo(6);
        assertThat(dto.getSupplierName()).isEqualTo("O'Neill Haulage");
        assertThat(dto.getCity()).isEqualTo("Ms Tan");
        assertThat(dto.getSupplierCity()).isEqualTo("Klang");
        assertThat(dto.getCountry()).isEqualTo("10");
        assertThat(dto.getSymbolRefid()).isEqualTo(2);
        assertThat(dto.getPaymentTermsRefid()).isEqualTo(3);
        assertThat(dto.getSelfBilled()).isZero();
        assertThat(dto.getMsicCodeRefId()).isZero();
        assertThat(dto.getExpiryDate()).isEqualTo(LocalDate.of(2027, 3, 31));
        assertThat(dto.getSstNo()).isEqualTo("SST-1");
        assertThat(dto.getTinType()).isEqualTo("Company (Malaysia)");
        assertThat(dto.getActive()).isEqualTo(1);
    }

    /**
     * The DTO used to demand CNumber, CNumberDisplay and AccountRefid (which
     * the server assigns) and a valid email in OEmail/OEmail1/AEmail1 (which
     * hold an MSIC code, a service tax type and a TIN). The screen's payload
     * sends none of the first and non-emails in the second.
     */
    @Test
    void theScreenPayloadPassesBeanValidation() {
        SupplierDto dto = mapper.readValue(SCREEN_PAYLOAD, SupplierDto.class);

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void aSupplierWithoutNameOrTypeIsStillRefused() {
        SupplierDto dto = mapper.readValue(SCREEN_PAYLOAD, SupplierDto.class);
        dto.setSupplierName(" ");
        dto.setSupplierType("");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("supplierName", "supplierType");
    }

    /** The response side: what {@code fromSupplierDto} has to find, whatever the casing. */
    @Test
    void theSaveResponseCarriesEveryKeyTheScreenReads() {
        SupplierDto dto = mapper.readValue(SCREEN_PAYLOAD, SupplierDto.class);
        dto.setCNumberDisplay("SU000000319");
        dto.setQneCode("400-P001");
        SupplierSaveResponse response = new SupplierSaveResponse(dto, SupplierQneOutcome.pushed("guid-1", "400-P001"));

        JsonNode tree = mapper.readTree(mapper.writeValueAsString(response));

        assertThat(tree.get("qne").get("status").asString()).isEqualTo("PUSHED");
        assertThat(tree.get("qne").get("qneCode").asString()).isEqualTo("400-P001");

        Set<String> supplierKeys = new HashSet<>();
        tree.get("supplier").properties().forEach(entry -> supplierKeys.add(entry.getKey().toLowerCase(Locale.ROOT)));
        assertThat(supplierKeys).contains(
                "id", "cnumberdisplay", "qnecode", "suppliername", "suppliertype", "city", "suppliercity",
                "country", "symbolrefid", "paymenttermsrefid", "oemail", "oemail1", "oname", "ophone",
                "aemail", "aemail1", "aname", "aphone", "selfbilled", "msiccoderefid", "expirydate",
                "tinno", "sstno", "msiccode", "servicetaxtype", "bankname", "accountno", "password", "active");
    }
}

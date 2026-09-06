package my.maleva.api.integration.myinvois.ubl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The leaf value types of an LHDN UBL-JSON document.
 *
 * <p>UBL-JSON writes every value as a one-element array of an object whose
 * text lives under the key {@code "_"} and whose XML attributes
 * ({@code currencyID}, {@code unitCode}, {@code schemeID}, …) sit beside it:
 * <pre>
 * "PayableAmount": [ { "_": 634.80, "currencyID": "MYR" } ]
 * </pre>
 * The factory methods here build exactly that shape and return {@code null}
 * for a blank input — the document mapper omits null properties, which is how
 * an optional element is left out. A method never returns an empty list,
 * because an empty list would be serialised as {@code []} and LHDN would
 * reject the element as present-but-empty.
 *
 * <p>Amounts are {@link BigDecimal} and are written exactly as given; the
 * caller decides the scale (see {@code EInvoiceMoney}). This class does no
 * rounding of its own.
 */
public final class UblValues {

    private UblValues() {
    }

    // ────────────────────────────────────────────────────────────── records ──

    /** {@code {"_": "INV000000123"}}, optionally with a scheme, e.g. {@code schemeID: "TIN"}. */
    public record Id(@JsonProperty("_") String value,
                     @JsonProperty("schemeID") String schemeId,
                     @JsonProperty("schemeAgencyName") String schemeAgencyName) {
    }

    /** {@code {"_": "some text"}}. */
    public record Text(@JsonProperty("_") String value) {
    }

    /** {@code {"_": 12.34, "currencyID": "MYR"}}. */
    public record Amount(@JsonProperty("_") BigDecimal value,
                         @JsonProperty("currencyID") String currencyId) {
    }

    /** {@code {"_": 2.00, "unitCode": "C62"}}. */
    public record Quantity(@JsonProperty("_") BigDecimal value,
                           @JsonProperty("unitCode") String unitCode) {
    }

    /** {@code {"_": 6.00}} — a bare number such as a tax percentage. */
    public record Numeric(@JsonProperty("_") BigDecimal value) {
    }

    /** {@code {"_": "MYR"}}. */
    public record Currency(@JsonProperty("_") String code) {
    }

    /** {@code {"_": "01", "listVersionID": "1.0"}} — the invoice type code. */
    public record TypeCode(@JsonProperty("_") String value,
                           @JsonProperty("listVersionID") String listVersionId) {
    }

    /** {@code {"_": "MYS", "listID": "ISO3166-1", "listAgencyID": "6"}} — a country code. */
    public record CountryCode(@JsonProperty("_") String value,
                              @JsonProperty("listID") String listId,
                              @JsonProperty("listAgencyID") String listAgencyId) {
    }

    /** {@code {"_": "022", "listID": "CLASS"}} — an item classification code. */
    public record ClassificationCode(@JsonProperty("_") String value,
                                     @JsonProperty("listID") String listId) {
    }

    /** {@code {"_": "52299", "name": "Other transportation support activities n.e.c."}}. */
    public record IndustryCode(@JsonProperty("_") String value,
                               @JsonProperty("name") String name) {
    }

    // ──────────────────────────────────────────────────────────── factories ──

    public static List<Id> id(String value) {
        return isBlank(value) ? null : List.of(new Id(value, null, null));
    }

    public static List<Id> id(String value, String schemeId) {
        return isBlank(value) ? null : List.of(new Id(value, schemeId, null));
    }

    public static List<Id> id(String value, String schemeId, String schemeAgencyName) {
        return isBlank(value) ? null : List.of(new Id(value, schemeId, schemeAgencyName));
    }

    public static List<Text> text(String value) {
        return isBlank(value) ? null : List.of(new Text(value));
    }

    /** Null when the currency is blank: an amount without a currency is meaningless to LHDN. */
    public static List<Amount> amount(BigDecimal value, String currencyId) {
        return value == null || isBlank(currencyId) ? null : List.of(new Amount(value, currencyId));
    }

    public static List<Quantity> quantity(BigDecimal value, String unitCode) {
        return value == null || isBlank(unitCode) ? null : List.of(new Quantity(value, unitCode));
    }

    public static List<Numeric> numeric(BigDecimal value) {
        return value == null ? null : List.of(new Numeric(value));
    }

    public static List<Currency> currency(String code) {
        return isBlank(code) ? null : List.of(new Currency(code));
    }

    public static List<TypeCode> typeCode(String value, String listVersionId) {
        return List.of(new TypeCode(value, listVersionId));
    }

    /** ISO 3166-1 alpha-3 country, agency 6 = UN/ECE. */
    public static List<CountryCode> country(String alpha3) {
        return isBlank(alpha3) ? null : List.of(new CountryCode(alpha3, "ISO3166-1", "6"));
    }

    public static List<ClassificationCode> classification(String code) {
        return isBlank(code) ? null : List.of(new ClassificationCode(code, "CLASS"));
    }

    public static List<IndustryCode> industry(String code, String name) {
        return isBlank(code) ? null : List.of(new IndustryCode(code, name));
    }

    /** LHDN wants the issue date as the UTC calendar date, {@code yyyy-MM-dd}. */
    public static List<Text> utcDate(Instant instant) {
        return List.of(new Text(DateTimeFormatter.ISO_LOCAL_DATE.format(instant.atOffset(ZoneOffset.UTC))));
    }

    /** …and the issue time as UTC wall-clock with a literal Z, {@code HH:mm:ssZ}. */
    public static List<Text> utcTime(Instant instant) {
        return List.of(new Text(DateTimeFormatter.ofPattern("HH:mm:ss'Z'").format(instant.atOffset(ZoneOffset.UTC))));
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

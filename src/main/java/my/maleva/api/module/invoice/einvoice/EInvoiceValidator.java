package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.config.MyInvoisProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Decides whether an invoice may be sent to the government.
 *
 * <p>Runs before a single byte goes to LHDN and before the document is even
 * built. Two families of rules:
 * <ul>
 *   <li><b>Completeness</b> — the fields LHDN requires exist (buyer TIN,
 *       address, country, a classification on every item). Legacy discovered
 *       these one at a time, after building the document, and reported a
 *       property path. Here they are all reported together, in plain words.</li>
 *   <li><b>Money</b> — every line's stored figures agree with Qty × Rate and
 *       the tax rate, and the header agrees with the sum of the lines, to
 *       within one sen ({@link EInvoiceMoney#TOLERANCE}; one sen per line for
 *       the tax sum). Legacy never checked any of this: it trusted whatever
 *       the row held. If the ledger and the document disagree, the customer
 *       pays one figure and the government records another, and the taxpayer
 *       answers for the difference.</li>
 * </ul>
 *
 * <p>The validator never changes a value. It only says whether the stored
 * values are fit to send; the builder then sends the stored values.
 */
@Component
public class EInvoiceValidator {

    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern ISO_3166_ALPHA3 = Pattern.compile("^[A-Z]{3}$");

    private final MyInvoisProperties properties;

    public EInvoiceValidator(MyInvoisProperties properties) {
        this.properties = properties;
    }

    /** All the reasons the snapshot cannot be sent; empty means go. */
    public List<EInvoiceProblem> validate(EInvoiceSnapshot snapshot) {
        List<EInvoiceProblem> problems = new ArrayList<>(snapshot.loadProblems());
        EInvoiceSnapshot.Header h = snapshot.header();
        String inv = "Invoice " + h.invoiceNo();

        if (!h.active()) {
            problems.add(EInvoiceProblem.of("invoice.inactive", inv + " is cancelled and cannot be e-invoiced"));
        }
        if (h.invoiceNo() == null || h.invoiceNo().isBlank()) {
            problems.add(EInvoiceProblem.of("invoice.number.missing", "Invoice " + h.invoiceId() + " has no invoice number"));
        }

        validateCustomer(snapshot.customer(), inv, problems);
        validateLines(snapshot.lines(), inv, problems);
        validateMoney(snapshot, inv, problems);

        return List.copyOf(problems);
    }

    // ───────────────────────────────────────────────────────────── customer ──

    private void validateCustomer(EInvoiceSnapshot.Customer c, String inv, List<EInvoiceProblem> problems) {
        if (c == null) {
            return; // the loader already reported the missing customer
        }
        String who = "customer " + orUnnamed(c.name());

        if (isBlank(c.name())) {
            problems.add(EInvoiceProblem.of("customer.name.missing", inv + ": the customer has no name"));
        }
        if (isBlank(c.tin())) {
            // Legacy would send the document without a TIN and let LHDN reject it.
            problems.add(EInvoiceProblem.of("customer.tin.missing",
                    inv + ": " + who + " has no TIN — LHDN rejects a buyer without one; "
                            + "update the customer master and push again"));
        }
        if (isBlank(c.registrationNo())) {
            problems.add(EInvoiceProblem.of("customer.registration.missing",
                    inv + ": " + who + " has no registration (BRN) number"));
        }
        if (isBlank(c.phone())) {
            problems.add(EInvoiceProblem.of("customer.phone.missing", inv + ": " + who + " has no office phone"));
        }
        if (isBlank(c.address1())) {
            problems.add(EInvoiceProblem.of("customer.address.missing", inv + ": " + who + " has no address"));
        }
        if (isBlank(c.city())) {
            problems.add(EInvoiceProblem.of("customer.city.missing", inv + ": " + who + " has no city"));
        }
        if (isBlank(c.postalZone())) {
            problems.add(EInvoiceProblem.of("customer.postcode.missing", inv + ": " + who + " has no postcode"));
        }
        if (isBlank(c.countryCode())) {
            problems.add(EInvoiceProblem.of("customer.country.missing",
                    inv + ": " + who + " has no country — LHDN needs the ISO country code"));
        } else if (!ISO_3166_ALPHA3.matcher(c.countryCode()).matches()) {
            problems.add(EInvoiceProblem.of("customer.country.invalid",
                    inv + ": " + who + " has country code '" + c.countryCode()
                            + "', which is not a 3-letter ISO code"));
        }
        if (MalaysianStateCodes.subentityCode(c.countryCode(), c.state()).isEmpty()) {
            problems.add(EInvoiceProblem.of("customer.state.unknown",
                    inv + ": " + who + " has state '" + orBlank(c.state())
                            + "', which is blank or not a recognised Malaysian state"));
        }

        if (isBlank(c.currencyCode())) {
            // the loader reported the missing currency row; nothing to add
        } else if (!ISO_4217.matcher(c.currencyCode()).matches()) {
            problems.add(EInvoiceProblem.of("customer.currency.invalid",
                    inv + ": " + who + " is on currency '" + c.currencyCode()
                            + "', which is not an ISO 4217 code (expected e.g. MYR, SGD, USD)"));
        } else if (!"MYR".equals(c.currencyCode()) && !properties.isAllowForeignCurrency()) {
            problems.add(EInvoiceProblem.of("currency.foreign.disabled",
                    inv + ": " + who + " is invoiced in " + c.currencyCode()
                            + "; foreign-currency e-invoices are not enabled (myinvois.allow-foreign-currency)"));
        }
    }

    // ──────────────────────────────────────────────────────────────── lines ──

    private void validateLines(List<EInvoiceSnapshot.Line> lines, String inv, List<EInvoiceProblem> problems) {
        for (EInvoiceSnapshot.Line line : lines) {
            String where = inv + " " + line.label();

            requireValue(line.quantity(), "quantity", where, problems);
            requireValue(line.unitPrice(), "unit price", where, problems);
            requireValue(line.taxPercent(), "tax %", where, problems);
            requireValue(line.taxAmount(), "tax amount", where, problems);
            requireValue(line.amount(), "amount", where, problems);

            if (isBlank(line.remarks()) && isBlank(line.productName())) {
                problems.add(EInvoiceProblem.of("line.description.missing", where + ": has no description"));
            }
            if (line.itemMasterRefId() != null && line.productCode() != null
                    && (line.classificationCode() == null || line.classificationCode() <= 0)) {
                // Legacy sent "000" here; 898 of the 935 documents LHDN marked
                // Invalid contained such a line, and each is stuck for good.
                problems.add(EInvoiceProblem.of("line.classification.missing",
                        where + ": product has no valid Sale Classification — set it on the item master and push again"));
            }
        }
    }

    private static void requireValue(BigDecimal value, String field, String where, List<EInvoiceProblem> problems) {
        if (value == null) {
            problems.add(EInvoiceProblem.of("line.value.missing", where + ": " + field + " is empty"));
        } else if (EInvoiceMoney.isNegative(value)) {
            problems.add(EInvoiceProblem.of("line.value.negative", where + ": " + field + " is negative (" + value.toPlainString() + ")"));
        }
    }

    // ──────────────────────────────────────────────────────────────── money ──

    /**
     * The arithmetic the Sale Invoice screen used when it saved the rows,
     * re-run on what was stored:
     * <pre>
     *   lineTax  = round2(qty × rate × pct / 100)
     *   amount   = round2(qty × rate + qty × rate × pct / 100)   (tax-inclusive)
     *   header.amount    = Σ amount
     *   header.taxAmount = Σ lineTax
     * </pre>
     * Any figure off by more than one sen (one sen per line for the tax sum)
     * stops the push with the expected and actual values in the message.
     * Figures at or above {@link EInvoiceMoney#FLOAT32_SEN_LIMIT} are refused
     * outright: the column cannot hold their sen, so agreement would prove
     * nothing.
     */
    private void validateMoney(EInvoiceSnapshot snapshot, String inv, List<EInvoiceProblem> problems) {
        EInvoiceSnapshot.Header h = snapshot.header();
        List<EInvoiceSnapshot.Line> lines = snapshot.lines();

        if (h.amount() == null || h.taxAmount() == null) {
            problems.add(EInvoiceProblem.of("header.amount.missing", inv + ": the header amount or tax amount is empty"));
            return;
        }
        if (EInvoiceMoney.isNegative(h.amount()) || EInvoiceMoney.isNegative(h.taxAmount())) {
            problems.add(EInvoiceProblem.of("header.amount.negative", inv + ": the header amount or tax amount is negative"));
            return;
        }
        if (EInvoiceMoney.exceedsFloat32SenPrecision(h.amount()) || EInvoiceMoney.exceedsFloat32SenPrecision(h.taxAmount())) {
            problems.add(EInvoiceProblem.of("header.amount.imprecise", imprecise(inv, h.amount())));
        }

        BigDecimal sumAmount = EInvoiceMoney.zero();
        BigDecimal sumTax = EInvoiceMoney.zero();
        Set<BigDecimal> ratesInUse = new TreeSet<>();
        int skipped = 0;

        for (EInvoiceSnapshot.Line line : lines) {
            if (line.quantity() == null || line.unitPrice() == null || line.taxPercent() == null
                    || line.taxAmount() == null || line.amount() == null) {
                skipped++;
                continue; // reported above; the totals check is meaningless without them
            }
            String where = inv + " " + line.label();

            if (EInvoiceMoney.exceedsFloat32SenPrecision(line.amount())
                    || EInvoiceMoney.exceedsFloat32SenPrecision(line.unitPrice())
                    || EInvoiceMoney.exceedsFloat32SenPrecision(line.taxAmount())) {
                problems.add(EInvoiceProblem.of("line.amount.imprecise", imprecise(where, line.amount())));
            }

            BigDecimal expectedTax = EInvoiceMoney.lineTax(line.quantity(), line.unitPrice(), line.taxPercent());
            BigDecimal expectedAmount = EInvoiceMoney.lineInclusive(line.quantity(), line.unitPrice(), line.taxPercent());

            if (!line.isTaxed() && line.taxAmount().signum() != 0) {
                problems.add(EInvoiceProblem.of("line.tax.without.rate",
                        where + ": tax % is 0 but a tax of " + money(line.taxAmount()) + " is stored"));
            } else if (!EInvoiceMoney.agrees(expectedTax, line.taxAmount())) {
                problems.add(EInvoiceProblem.of("line.tax.mismatch",
                        where + ": stored tax " + money(line.taxAmount()) + " ≠ " + line.quantity().toPlainString()
                                + " × " + money(line.unitPrice()) + " × " + line.taxPercent().stripTrailingZeros().toPlainString()
                                + "% = " + money(expectedTax)));
            }
            if (!EInvoiceMoney.agrees(expectedAmount, line.amount())) {
                problems.add(EInvoiceProblem.of("line.amount.mismatch",
                        where + ": stored amount " + money(line.amount()) + " ≠ Qty × Rate + tax = " + money(expectedAmount)));
            }
            if (line.isTaxed()) {
                ratesInUse.add(line.taxPercent().stripTrailingZeros());
            }
            sumAmount = sumAmount.add(line.amount());
            sumTax = sumTax.add(line.taxAmount());
        }

        if (!lines.isEmpty() && skipped == 0) {
            // The header amount is a rounded sum of rounded lines: one sen of
            // slack. The header tax is a sum of UNROUNDED line taxes: up to one
            // sen per line.
            if (!EInvoiceMoney.agrees(sumAmount, h.amount())) {
                problems.add(EInvoiceProblem.of("header.amount.mismatch",
                        inv + ": header amount " + money(h.amount()) + " ≠ sum of " + lines.size() + " lines "
                                + money(sumAmount) + " (difference " + money(h.amount().subtract(sumAmount)) + ")"));
            }
            if (!EInvoiceMoney.agrees(sumTax, h.taxAmount(), EInvoiceMoney.sumTolerance(lines.size()))) {
                problems.add(EInvoiceProblem.of("header.tax.mismatch",
                        inv + ": header tax " + money(h.taxAmount()) + " ≠ sum of line taxes " + money(sumTax)
                                + " (difference " + money(h.taxAmount().subtract(sumTax)) + ")"));
            }
        }
        if (h.grossAmount() != null && !EInvoiceMoney.agrees(h.grossAmount(), h.amount())) {
            // The screen always stores GrossAmount == Amount; a difference means
            // another writer changed the row and the figures are not trustworthy.
            problems.add(EInvoiceProblem.of("header.gross.mismatch",
                    inv + ": header gross " + money(h.grossAmount()) + " ≠ amount " + money(h.amount())
                            + " — the invoice was changed outside the Sale Invoice screen"));
        }
        if (ratesInUse.size() > 1) {
            // The document format used here carries one taxed subtotal; legacy
            // silently merged different rates into it.
            problems.add(EInvoiceProblem.of("lines.tax.rates.mixed",
                    inv + ": lines carry more than one tax rate (" + ratesInUse.stream()
                            .map(BigDecimal::toPlainString).reduce((a, b) -> a + "%, " + b).orElse("")
                            + "%); the e-invoice format supports one rate per document"));
        }
    }

    private static String imprecise(String where, BigDecimal value) {
        return where + ": stored amount " + money(value) + " is at or above "
                + EInvoiceMoney.FLOAT32_SEN_LIMIT.toPlainString()
                + ", which this database cannot hold to the sen; verify the figure and enter it as separate lines below that limit";
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    private static String money(BigDecimal value) {
        return value == null ? "?" : value.toPlainString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String orUnnamed(String value) {
        return isBlank(value) ? "(unnamed)" : value.trim();
    }

    private static String orBlank(String value) {
        return value == null ? "" : value.trim();
    }
}

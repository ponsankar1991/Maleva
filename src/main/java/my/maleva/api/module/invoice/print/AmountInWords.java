package my.maleva.api.module.invoice.print;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * The "RM   ONE THOUSAND SEVEN HUNDRED ONLY" line on the invoice — the port
 * of the Crystal {@code Amountinwords} formula, reproduced word for word:
 *
 * <pre>
 * stringvar InWords := {SymbolName} + "  ";
 * crores : towords(n,0) + " Crore" / " Crores"
 * lakhs  : towords(n,0) + " Lakhs"            (legacy wrote "Lakhs" for one as well)
 * rest   : towords(truncate(Amt),0)
 * cents  : " and " + towords(cents,0) + " Cents only"   else " Only"
 * UPPERCASE(InWords)
 * </pre>
 *
 * <p>Crystal's {@code ToWords} hyphenates compound numbers ("THIRTY-SIX") and
 * writes no "and" inside the number; both are kept, since the customer's
 * copy has always looked that way. The one legacy slip not copied is the
 * missing space in {@code ToWords(RmVal,0) + "Lakhs"}, which printed
 * "TWOLAKHS".
 *
 * <p>Examples: 1700.00 → {@code RM   ONE THOUSAND SEVEN HUNDRED ONLY};
 * 536.17 → {@code SGD   FIVE HUNDRED THIRTY-SIX AND SEVENTEEN CENTS ONLY}.
 */
public final class AmountInWords {

    private static final long CRORE = 10_000_000L;
    private static final long LAKH = 100_000L;

    private static final String[] ONES = {
            "", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN",
            "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN"};
    private static final String[] TENS = {
            "", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY"};

    private AmountInWords() {
    }

    /**
     * @param currencySymbol the prefix, e.g. {@code RM}; Crystal followed it with two spaces
     * @param amount         the amount; null or negative prints as zero
     */
    public static String of(String currencySymbol, BigDecimal amount) {
        BigDecimal value = amount == null || amount.signum() < 0
                ? BigDecimal.ZERO
                : amount.setScale(2, RoundingMode.HALF_UP);
        long whole = value.longValue();
        int cents = value.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        // Crystal: SymbolName + "  ", then every part is appended as " " + part,
        // which is why the printed line has a wide gap after the symbol.
        StringBuilder words = new StringBuilder(currencySymbol == null ? "" : currencySymbol.trim()).append("  ");

        long crores = whole / CRORE;
        if (crores > 0) {
            words.append(' ').append(below100000(crores)).append(crores == 1 ? " CRORE" : " CRORES");
        }
        whole %= CRORE;

        long lakhs = whole / LAKH;
        if (lakhs > 0) {
            words.append(' ').append(below100000(lakhs)).append(" LAKHS");
        }
        whole %= LAKH;

        if (whole > 0) {
            words.append(' ').append(below100000(whole));
        }

        if (cents > 0) {
            words.append(" AND ").append(below100000(cents)).append(" CENTS ONLY");
        } else {
            words.append(" ONLY");
        }
        return words.toString().toUpperCase(Locale.ROOT);
    }

    /** 1 – 99,999 the way Crystal's ToWords writes it: no "and", hyphenated tens. */
    static String below100000(long n) {
        StringBuilder sb = new StringBuilder();
        long thousands = n / 1000;
        long rest = n % 1000;
        if (thousands > 0) {
            sb.append(below1000(thousands)).append(" THOUSAND");
        }
        if (rest > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(below1000(rest));
        }
        return sb.toString();
    }

    private static String below1000(long n) {
        StringBuilder sb = new StringBuilder();
        long hundreds = n / 100;
        long rest = n % 100;
        if (hundreds > 0) {
            sb.append(ONES[(int) hundreds]).append(" HUNDRED");
        }
        if (rest > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (rest < 20) {
                sb.append(ONES[(int) rest]);
            } else {
                sb.append(TENS[(int) (rest / 10)]);
                if (rest % 10 > 0) {
                    sb.append('-').append(ONES[(int) (rest % 10)]);
                }
            }
        }
        return sb.toString();
    }
}

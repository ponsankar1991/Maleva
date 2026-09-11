package my.maleva.api.module.customer.dto;

/**
 * The outcome of a TIN check, in the three states the screen has to tell
 * apart. Legacy collapsed the last two into one message ("Valid Tin No" was
 * shown both when a TIN came back empty and when it did not), so an operator
 * could not tell "LHDN confirmed it" from "LHDN has never heard of it".
 *
 * @param status  which of the three happened
 * @param tin     the confirmed or discovered TIN; null unless status is VALID
 * @param message what to show the operator
 */
public record CustomerTinCheckResult(Status status, String tin, String message) {

    public enum Status {
        /** LHDN confirmed the TIN, or found one. */
        VALID,
        /** LHDN answered, and holds no TIN for this taxpayer. */
        NOT_FOUND,
        /** LHDN refused the request, or could not be reached. */
        INVALID
    }

    public static CustomerTinCheckResult valid(String tin, String message) {
        return new CustomerTinCheckResult(Status.VALID, tin, message);
    }

    public static CustomerTinCheckResult notFound(String message) {
        return new CustomerTinCheckResult(Status.NOT_FOUND, null, message);
    }

    public static CustomerTinCheckResult invalid(String message) {
        return new CustomerTinCheckResult(Status.INVALID, null, message);
    }
}

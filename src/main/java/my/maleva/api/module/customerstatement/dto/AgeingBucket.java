package my.maleva.api.module.customerstatement.dto;

import java.math.BigDecimal;

/**
 * One cell of the ageing grid at the foot of the statement.
 *
 * <p>The grid is twelve rolling months ending in the current month. Legacy
 * keyed the cells by month number alone ("Jan".."Dec"), which is unambiguous
 * only because a rolling year visits each month once; the label here carries
 * the year too, as the newer legacy model's {@code Jan_Label..Dec_Label}
 * fields were reaching for.
 *
 * @param year      four-digit year of the bucket
 * @param month     1..12
 * @param monthName "Jan".."Dec", what the printed report shows today
 * @param label     "Jan 26" — month and two-digit year
 * @param amount    outstanding from invoices dated in that month
 */
public record AgeingBucket(int year, int month, String monthName, String label, BigDecimal amount) {
}

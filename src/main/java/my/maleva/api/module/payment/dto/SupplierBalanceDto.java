package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A supplier's running balance as at a date — the Java shape of the
 * {@code SupplierBalance}/{@code SupplierBalance_Single} table functions.
 *
 * <p>Balance = opening balance + purchases + bills − payments, all up to and
 * including {@code tillDate}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBalanceDto {

    private Integer id;

    private String supplierName;

    private String mobileNo;

    private String address1;

    private String address2;

    private String city;

    private String zipcode;

    private BigDecimal balance;
}

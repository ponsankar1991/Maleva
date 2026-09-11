package my.maleva.api.module.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Integer id;

    /**
     * The tenant every write is scoped to. The controller marks the body
     * {@code @Valid} but nothing on this class was constrained, so the
     * annotation did nothing and a payload with no company reached the service.
     */
    @NotNull(message = "Company is required")
    @Positive(message = "Company must be greater than zero")
    private Integer companyRefId;

    /**
     * The one field the legacy screen actually required — everything else in
     * its {@code emptycheck()} is commented out. Length matches the column.
     */
    @NotBlank(message = "Customer name is required")
    @Size(max = 500, message = "Customer name cannot exceed 500 characters")
    private String customerName;
    private String cNumberDisplay;
    private Integer cNumber;
    private String address1;
    private String address2;
    private String city;
    private String zipcode;
    private String country;
    private Integer symbolRefid;
    private Integer paymentTermsRefid;
    private String gstNo;
    private String email;
    private String mobileNo;
    private String userName;
    private String password;
    private String latitude;
    private String longitude;
    private String tokenId;
    private String oEmail;
    private String oName;
    private String oPhone;
    private String aEmail;
    private String aName;
    private String aPhone;
    private Integer active;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private String aEmail1;
    private String oEmail1;
    private String state;
    private String address3;
    private String personId;
    private BigDecimal openingBalance;
    private Integer accountRefid;
    private String tinNo;
    private String sstNo;
    private String msicCode;
    private String serviceTaxType;
    private String bankName;
    private String accountNo;
    private String companyCode;
    private String updateId;
    private String tintype;
    private String customerTin;
    private String eInvoice;
    private String exemptionNo;
    private LocalDate expiryDate;
    private String exemptionDetails;
    private String registrationNo;
    private String customerCity;
    private Integer countryId;
}

package my.maleva.api.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSelectDto {

    // =========================
    // Core Customer fields
    // =========================
    private Integer id;
    private Integer companyRefId;
    private String customerName;
    private String cNumberDisplay;
    private Integer cNumber;
    private String mobileNo;
    private String email;
    private Integer active;
    private LocalDateTime createdDate;

    // =========================
    // Foreign key references (useful for UI logic)
    // =========================
    private Integer symbolRefid;
    private Integer paymentTermsRefid;
    private Integer countryId;

    // =========================
    // Joined / display fields
    // =========================
    private String sName;        // SymbolMaster.SName
    private String termsName;    // PaymentTermsMaster.TermsName
    private String accountCode;  // AccountsGroupMaster.AccountCode
    private String country;      // CountryMaster.Country (CMName)
}

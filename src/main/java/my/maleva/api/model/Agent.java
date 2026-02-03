package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for Agent table
 */
@Entity
@Table(name = "Agent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "AgentCompanyRefId", nullable = false)
    private Integer agentCompanyRefId;

    @Column(name = "AgentName", length = 500, nullable = false)
    private String agentName;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Address1", length = 300)
    private String address1;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "MobileNo", length = 50)
    private String mobileNo;

    @Column(name = "UserName", length = 50)
    private String userName;

    @Column(name = "Password", length = 50)
    private String password;

    @Column(name = "TokenId", length = 500)
    private String tokenId;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "AccountRefid", nullable = false)
    private Integer accountRefid;

    @Column(name = "TinNo", length = 100)
    private String tinNo;

    @Column(name = "SSTNo", length = 100)
    private String sstNo;

    @Column(name = "MsicCode", length = 100)
    private String msicCode;

    @Column(name = "ServiceTaxType", length = 100)
    private String serviceTaxType;

    @Column(name = "BankName", length = 100)
    private String bankName;

    @Column(name = "AccountNo", length = 100)
    private String accountNo;
}

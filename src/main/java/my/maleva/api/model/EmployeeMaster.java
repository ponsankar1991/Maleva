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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity mapping for the EmployeeMaster table.
 */
@Entity
@Table(name = "EmployeeMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "EmployeeName", length = 500, nullable = false)
    private String employeeName;

    @Column(name = "EmployeeType", length = 100, nullable = false)
    private String employeeType;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Address1", length = 300)
    private String address1;

    @Column(name = "Address2", length = 300)
    private String address2;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "Zipcode", length = 50)
    private String zipcode;

    @Column(name = "Country", length = 50)
    private String country;

    @Column(name = "GSTNO", length = 100)
    private String gstNo;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "MobileNo", length = 50)
    private String mobileNo;

    @Column(name = "UserName", length = 50)
    private String userName;

    @Column(name = "Password", length = 50)
    private String password;

    @Column(name = "Latitude", length = 50)
    private String latitude;

    // note: schema had lowercase 'longitude'
    @Column(name = "longitude", length = 50)
    private String longitude;

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

    @Column(name = "State", length = 100)
    private String state;

    @Column(name = "Address3", length = 300)
    private String address3;

    @Column(name = "PersonId", length = 100)
    private String personId;

    @Column(name = "AccountRefid", nullable = false)
    private Integer accountRefid;

    @Column(name = "role_id", nullable = false, columnDefinition = "numeric(4) default 100")
    private Integer roleId;

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

    @Column(name = "RulesType", length = 50)
    private String rulesType;

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

    @Column(name = "JoiningDate")
    private LocalDate joiningDate;

    @Column(name = "LeavingDate")
    private LocalDate leavingDate;

    @Column(name = "EmergencyNo", length = 50)
    private String emergencyNo;

    @Column(name = "AppPassword", length = 100)
    private String appPassword;

    @Column(name = "Employeecurrency", length = 255)
    private String employeecurrency;
}

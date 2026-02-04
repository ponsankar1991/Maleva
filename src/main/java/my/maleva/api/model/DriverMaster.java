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
 * JPA entity for DriverMaster table
 */
@Entity
@Table(name = "DriverMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "DriverName", length = 500, nullable = false)
    private String driverName;

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
    private String gstno;

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

    @Column(name = "licenseNo", length = 50)
    private String licenseNo;

    @Column(name = "licenseExp")
    private LocalDate licenseExp;

    @Column(name = "GDLNo", length = 50)
    private String gdlNo;

    @Column(name = "GDLExp")
    private LocalDate gdlExp;

    @Column(name = "State", length = 100)
    private String state;

    @Column(name = "Address3", length = 300)
    private String address3;

    @Column(name = "PersonId", length = 100)
    private String personId;

    @Column(name = "KuantanPort")
    private LocalDate kuantanPort;

    @Column(name = "NorthportPort")
    private LocalDate northportPort;

    @Column(name = "PkfzPort")
    private LocalDate pkfzPort;

    @Column(name = "KliaPort")
    private LocalDate kliaPort;

    @Column(name = "PguPort")
    private LocalDate pguPort;

    @Column(name = "TanjungPort")
    private LocalDate tanjungPort;

    @Column(name = "PenangPort")
    private LocalDate penangPort;

    @Column(name = "PtpPort")
    private LocalDate ptpPort;

    @Column(name = "WestportPort")
    private LocalDate westportPort;

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

    @Column(name = "TruckRefId")
    private Integer truckRefId;

    @Column(name = "KuantanPortStatus", length = 300)
    private String kuantanPortStatus;

    @Column(name = "NorthportPortStatus", length = 300)
    private String northportPortStatus;

    @Column(name = "PkfzPortStatus", length = 300)
    private String pkfzPortStatus;

    @Column(name = "KliaPortStatus", length = 300)
    private String kliaPortStatus;

    @Column(name = "PguPortStatus", length = 300)
    private String pguPortStatus;

    @Column(name = "TanjungPortStatus", length = 300)
    private String tanjungPortStatus;

    @Column(name = "PenangPortStatus", length = 300)
    private String penangPortStatus;

    @Column(name = "PtpPortStatus", length = 300)
    private String ptpPortStatus;

    @Column(name = "WestportPortStatus", length = 300)
    private String westportPortStatus;

    @Column(name = "JoiningDate")
    private LocalDate joiningDate;

    @Column(name = "LeavingDate")
    private LocalDate leavingDate;

    @Column(name = "LumutPort", length = 500)
    private String lumutPort;

    @Column(name = "LumutPortStatus", length = 500)
    private String lumutPortStatus;

    @Column(name = "KemamanPort", length = 500)
    private String kemamanPort;

    @Column(name = "KemamanPortStatus", length = 500)
    private String kemamanPortStatus;
}

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
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for Company table
 */
@Entity
@Table(name = "Company")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CCode", nullable = false)
    private Integer cCode;

    @Column(name = "CName", length = 200, nullable = false)
    private String cName;

    @Column(name = "Cstatus", nullable = false)
    private Boolean cstatus;

    @Column(name = "Active", nullable = false)
    private Boolean active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "ComputerName", length = 100)
    private String computerName;

    @Column(name = "BranchName", length = 200)
    private String branchName;

    @Column(name = "FromDate", nullable = false)
    private LocalDate fromDate;

    @Column(name = "ToDate", nullable = false)
    private LocalDate toDate;

    @Column(name = "MComid")
    private Integer mComid;

    @Column(name = "MobileNo", length = 50)
    private String mobileNo;

    @Column(name = "ParentId")
    private Integer parentId;

    @Column(name = "Username", length = 50)
    private String username;

    @Column(name = "WebCode", columnDefinition = "uniqueidentifier")
    private UUID webCode;

    @Column(name = "TokenId", length = 500)
    private String tokenId;

    @Column(name = "WebId")
    private Integer webId;

    @Column(name = "OwnerName", length = 70)
    private String ownerName;

    @Column(name = "Address1", length = 70)
    private String address1;

    @Column(name = "Address2", length = 70)
    private String address2;

    @Column(name = "City", length = 70)
    private String city;

    @Column(name = "LandMark", length = 70)
    private String landMark;

    @Column(name = "Pincode", length = 10)
    private String pincode;

    @Column(name = "MobileNo1", length = 20)
    private String mobileNo1;

    @Column(name = "MobileNo2", length = 20)
    private String mobileNo2;

    @Column(name = "Latitude", length = 100)
    private String latitude;

    @Column(name = "Longitude", length = 100)
    private String longitude;

    @Column(name = "Margin")
    private Float margin;

    @Column(name = "AreaName", length = 100)
    private String areaName;

    @Column(name = "Email", length = 50)
    private String email;

    @Column(name = "ImagePath", length = 100)
    private String imagePath;

    @Column(name = "UpiId", length = 100)
    private String upiId;

    @Column(name = "Password", length = 50)
    private String password;
}

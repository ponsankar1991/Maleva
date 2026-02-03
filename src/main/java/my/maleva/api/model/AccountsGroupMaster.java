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
 * JPA entity for AccountsGroupMaster table
 */
@Entity
@Table(name = "AccountsGroupMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsGroupMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "AccountName", length = 500, nullable = false)
    private String accountName;

    @Column(name = "ParentId")
    private Integer parentId;

    @Column(name = "Editmode", nullable = false)
    private Boolean editmode;

    @Column(name = "NoChild")
    private Integer noChild;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 200, nullable = false)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "AccountCode", length = 20, nullable = false)
    private String accountCode;

    @Column(name = "UpdateId", length = 50)
    private String updateId;

    @Column(name = "QNECode", length = 50)
    private String qneCode;
}

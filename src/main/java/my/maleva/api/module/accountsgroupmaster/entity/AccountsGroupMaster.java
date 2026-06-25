package my.maleva.api.module.accountsgroupmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "AccountCode")
    private String accountCode;

    @Column(name = "AccountName")
    private String accountName;

    @Column(name = "ParentId")
    private Integer parentId;

    @Column(name = "Editmode")
    private Integer editmode;

    @Column(name = "NoChild")
    private Integer noChild;

    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By")
    private String modifiedBy;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "QNECode")
    private String qneCode;

    @Column(name = "UpdateId")
    private String updateId;
}


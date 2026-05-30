package my.maleva.api.module.accountsgroupmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    @Column(name = "Classification")
    private Integer classification;

    @Column(name = "AccountCode")
    private String accountCode;

    @Column(name = "ClassificationName")
    private String classificationName;

    @Column(name = "AccountName")
    private String accountName;

    @Transient
    private String accountName1;

    @Column(name = "ParentName")
    private String parentName;

    @Column(name = "ParentId")
    private UUID parentId;

    @Column(name = "RootId")
    private UUID rootId;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "QNECode")
    private String qneCode;

    @Column(name = "UpdateId")
    private String updateId;
}


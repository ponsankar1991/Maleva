package my.maleva.api.module.accountsgroupmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "GLAccounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GLAccount {

    @Id
    @Column(name = "Id")
    private UUID id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "ParentId")
    private UUID parentId;

    @Column(name = "GLAccountCode")
    private String glAccountCode;

    @Column(name = "AccountId")
    private UUID accountId;

    @Column(name = "Description")
    private String description;

    @Column(name = "DRCR")
    private String drcr;

    @Column(name = "IsActive")
    private Integer isActive;

    @Column(name = "Classification")
    private Integer classification;

    @Column(name = "Notes")
    private String notes;

    @Transient
    private String accountName1;
}


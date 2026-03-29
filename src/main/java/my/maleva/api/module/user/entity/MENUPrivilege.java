package my.maleva.api.module.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "MENUPrivilege")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MENUPrivilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "UserRefId", nullable = false)
    private Integer userRefId;

    @Column(name = "FormText", length = 100, nullable = false)
    private String formText;

    @Column(name = "FormName", length = 100)
    private String formName;

    @Column(name = "ParentId")
    private Integer parentId;

    @Column(name = "ExeName", length = 100, nullable = false)
    private String exeName;

    @Column(name = "EditPassword", nullable = false)
    private Integer editPassword;

    @Column(name = "PageAdd", nullable = false)
    private Integer pageAdd;

    @Column(name = "PageEdit", nullable = false)
    private Integer pageEdit;

    @Column(name = "PageDelete", nullable = false)
    private Integer pageDelete;

    @Column(name = "PageView", nullable = false)
    private Integer pageView;

    @Column(name = "PageActive", nullable = false)
    private Integer pageActive;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "Show", nullable = false)
    private Integer show;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "PView", nullable = false)
    private Integer pView;

    @Column(name = "Icon", length = 20)
    private String icon;

    @Column(name = "MobileApp")
    private Integer mobileApp;
}

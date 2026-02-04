package my.maleva.api.agentcompany.dto;

import java.time.LocalDateTime;

public class AgentCompanyMasterDTO {

    private Long id;
    private Integer companyRefId;
    private String name;
    private Integer dFlag;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Integer active;

    // ...existing code...
    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCompanyRefId() {
        return companyRefId;
    }

    public void setCompanyRefId(Integer companyRefId) {
        this.companyRefId = companyRefId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDFlag() {
        return dFlag;
    }

    public void setDFlag(Integer dFlag) {
        this.dFlag = dFlag;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }
}

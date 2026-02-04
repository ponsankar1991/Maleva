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
 * JPA entity for Cashier table
 */
@Entity
@Table(name = "Cashier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cashier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Cashier_Name", length = 50, nullable = false)
    private String cashierName;

    @Column(name = "Pswd", length = 50, nullable = false)
    private String pswd;

    @Column(name = "Status", nullable = false)
    private Boolean status;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "DeleteRow", nullable = false)
    private Boolean deleteRow;

    @Column(name = "DeleteReason", nullable = false)
    private Boolean deleteReason;
}

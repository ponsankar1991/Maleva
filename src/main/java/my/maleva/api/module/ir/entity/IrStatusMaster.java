package my.maleva.api.module.ir.entity;

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
 * Where an IR is in its life. A table rather than a varchar on IRMaster so a
 * status can be added without a code change.
 *
 * <p>There is no "finished" flag on the row, so the codes that mean finished
 * are named once in {@code IrServiceImpl.FINISHED_STATUS_CODES}. A status added
 * later that ends the workflow has to be listed there too, or it will keep
 * counting as open.
 */
@Entity
@Table(name = "IRStatusMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrStatusMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "StatusCode", nullable = false, length = 50)
    private String statusCode;

    @Column(name = "StatusName", nullable = false, length = 100)
    private String statusName;

    @Column(name = "ColorCode", length = 20)
    private String colorCode;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50)
    private String createdBy;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;
}

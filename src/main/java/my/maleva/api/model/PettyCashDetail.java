package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PettyCashDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Items", length = 100)
    private String items;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "Notes", length = 255)
    private String notes;

    @Column(name = "PettyCashMasterRefId", nullable = false)
    private Integer pettyCashMasterRefId;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;
}

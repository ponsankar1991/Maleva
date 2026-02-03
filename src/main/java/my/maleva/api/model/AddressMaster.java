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
 * JPA entity for AddressMaster table
 */
@Entity
@Table(name = "AddressMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "Address", length = 1000)
    private String address;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;
}

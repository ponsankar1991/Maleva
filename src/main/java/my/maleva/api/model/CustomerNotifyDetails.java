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
 * JPA entity for CustomerNotifyDetails table
 */
@Entity
@Table(name = "CustomerNotifyDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerNotifyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerMasterRefId", nullable = false)
    private Integer customerMasterRefId;

    @Column(name = "Name", length = 200)
    private String name;

    @Column(name = "Whatsapp", length = 200)
    private String whatsapp;

    @Column(name = "Email", length = 200)
    private String email;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;
}

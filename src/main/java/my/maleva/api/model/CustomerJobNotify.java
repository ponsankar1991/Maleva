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

/**
 * JPA entity for CustomerJobNotify table
 */
@Entity
@Table(name = "CustomerJobNotify")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJobNotify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerDetailRefId", nullable = false)
    private Integer customerDetailRefId;

    @Column(name = "SaleOrderRefId", nullable = false)
    private Integer saleOrderRefId;

    @Column(name = "Whatsapp", nullable = false)
    private Integer whatsapp;

    @Column(name = "Email", nullable = false)
    private Integer email;

    @Column(name = "Phone", length = 200)
    private String phone;
}

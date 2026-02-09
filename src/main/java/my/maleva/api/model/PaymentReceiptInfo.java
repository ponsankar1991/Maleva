package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payment_Receipt_Info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyName", length = 255, nullable = false)
    private String companyName;

    @Column(name = "RegistrationNo", length = 100)
    private String registrationNo;

    @Column(name = "GSTRegNo", length = 100)
    private String gstRegNo;

    @Column(name = "Address", length = 500)
    private String address;

    @Column(name = "Phone", length = 50)
    private String phone;

    @Column(name = "SelfBilledType", length = 100)
    private String selfBilledType;

    @Column(name = "TinType", length = 100)
    private String tinType;

    @Column(name = "SupplierTIN", length = 100)
    private String supplierTIN;

    @Column(name = "MSICCode", length = 100)
    private String msicCode;

    @Column(name = "Address1", length = 500)
    private String address1;

    @Column(name = "ZipCode", length = 20)
    private String zipCode;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "State", length = 100)
    private String state;

    @Column(name = "Country", length = 100)
    private String country;

    @Column(name = "SalesTaxRegNo", length = 100)
    private String salesTaxRegNo;

    @Column(name = "ServiceTaxRegNo", length = 100)
    private String serviceTaxRegNo;

    @Column(name = "TourismTaxRegNo", length = 100)
    private String tourismTaxRegNo;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "eInvoice", nullable = false)
    private Integer eInvoice;

    @Column(name = "StateCode", length = 50)
    private String stateCode;

    @Column(name = "CountryId")
    private Integer countryId;

    @Column(name = "MSICCode_Int")
    private Integer msicCodeInt;

    @Column(name = "Email", length = 100)
    private String email;
}

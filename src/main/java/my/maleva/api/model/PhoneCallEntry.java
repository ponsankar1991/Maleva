package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PhoneCallEntry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneCallEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CallDate", nullable = false)
    private LocalDateTime callDate;

    @Column(name = "PhoneNo", length = 50, nullable = false)
    private String phoneNo;

    @Column(name = "Remarks", length = 1500, nullable = false)
    private String remarks;
}

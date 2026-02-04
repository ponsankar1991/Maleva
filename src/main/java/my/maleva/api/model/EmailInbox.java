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
 * JPA entity for EmailInbox table
 */
@Entity
@Table(name = "EmailInbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "EmailID", length = 200)
    private String emailId;

    @Column(name = "Subject", length = 500)
    private String subject;

    @Column(name = "Sender", length = 255, nullable = false)
    private String sender;

    @Column(name = "ReceivedDate", nullable = false)
    private LocalDateTime receivedDate;

    @Column(name = "IsUnread", nullable = false)
    private Integer isUnread;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "IsReplied", nullable = false)
    private Integer isReplied;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;
}

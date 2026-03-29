package my.maleva.api.module.communication.repository;

import my.maleva.api.module.communication.entity.EmailInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailInboxRepository extends JpaRepository<EmailInbox, Integer> {
    List<EmailInbox> findByEmployeeRefId(Integer employeeRefId);
    List<EmailInbox> findByIsUnread(Integer isUnread);
}

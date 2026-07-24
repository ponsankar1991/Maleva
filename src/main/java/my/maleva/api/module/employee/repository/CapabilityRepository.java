package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.entity.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapabilityRepository extends JpaRepository<Capability, Integer> {
    List<Capability> findByIsActiveTrue();
}

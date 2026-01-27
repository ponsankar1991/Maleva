package my.maleva.api.repo;

import my.maleva.api.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByCustomerNameContainingIgnoreCase(String name);
}

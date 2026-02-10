package my.maleva.api.service.impl;

import my.maleva.api.dto.CustomerDto;
import my.maleva.api.dto.request.CustomerSelectRequest;
import my.maleva.api.dto.response.CustomerSelectDto;
import my.maleva.api.dto.response.CustomerSelectResult;
import my.maleva.api.model.Customer;
import my.maleva.api.repo.CustomerQueryRepository;
import my.maleva.api.repo.CustomerRepository;
import my.maleva.api.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private  final CustomerQueryRepository queryRepository;
    public CustomerServiceImpl(CustomerRepository repository, CustomerQueryRepository queryRepository) {
        this.repository = repository;
        this.queryRepository = queryRepository;
    }

    @Override
    public CustomerDto create(CustomerDto dto) {
        Customer entity = toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        Customer saved = repository.save(entity);
        return toDto(saved);
    }

    @Override
    public CustomerDto update(Integer id, CustomerDto dto) {
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        copyDtoToEntity(dto, existing);
        existing.setModifiedDate(LocalDateTime.now());

        Customer saved = repository.save(existing);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getById(Integer id) {
        Customer c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toDto(c);
    }

    @Override
    @Transactional(readOnly = true)

    public List<CustomerDto> findAll(String name) {
        List<Customer> list;
        if (name == null || name.isBlank()) {
            list = repository.findAll();
        } else {
            list = repository.findByCustomerNameContainingIgnoreCase(name);
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CustomerSelectResult selectCustomer(CustomerSelectRequest request) {

        long count = queryRepository.countCustomers(request);

        List<CustomerSelectDto> customers =
                queryRepository.findCustomers(request);

        return new CustomerSelectResult(customers, count);
    }


    @Override
    public void softDelete(Integer customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setActive(2);
        repository.save(customer);
    }



    @Override
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Customer not found: " + id);
        }
        repository.deleteById(id);
    }

    /* ===================== MAPPING ===================== */

    private CustomerDto toDto(Customer c) {
        if (c == null) return null;
        return CustomerDto.builder()
                .id(c.getId())
                .companyRefId(c.getCompanyRefId())
                .customerName(c.getCustomerName())
                .email(c.getEmail())
                .mobileNo(c.getMobileNo())
                .active(c.getActive())
                .createdDate(c.getCreatedDate())
                .modifiedDate(c.getModifiedDate())
                .build();
    }

    private Customer toEntity(CustomerDto d) {
        if (d == null) return null;
        return Customer.builder()
                .id(d.getId())
                .companyRefId(d.getCompanyRefId())
                .customerName(d.getCustomerName())
                .email(d.getEmail())
                .mobileNo(d.getMobileNo())
                .active(d.getActive())
                .createdDate(d.getCreatedDate())
                .modifiedDate(d.getModifiedDate())
                .build();
    }

    private void copyDtoToEntity(CustomerDto d, Customer c) {
        c.setCompanyRefId(d.getCompanyRefId());
        c.setCustomerName(d.getCustomerName());
        c.setEmail(d.getEmail());
        c.setMobileNo(d.getMobileNo());
        c.setActive(d.getActive());
    }
}

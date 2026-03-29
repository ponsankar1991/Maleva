package my.maleva.api.module.customer.service.impl;

import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.dto.request.CustomerSelectRequest;
import my.maleva.api.module.customer.dto.response.CustomerSelectDto;
import my.maleva.api.module.customer.dto.response.CustomerSelectResult;
import my.maleva.api.module.customer.mapper.CustomerMapper;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerQueryRepository;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.customer.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerQueryRepository queryRepository;
    private final CustomerMapper mapper;

    public CustomerServiceImpl(CustomerRepository repository, CustomerQueryRepository queryRepository, CustomerMapper mapper) {
        this.repository = repository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    @Override
    public CustomerDto create(CustomerDto dto) {
        Customer entity = mapper.toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        Customer saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public CustomerDto update(Integer id, CustomerDto dto) {
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));

        mapper.updateFromDto(dto, existing);
        existing.setModifiedDate(LocalDateTime.now());

        Customer saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getById(Integer id) {
        Customer c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return mapper.toDto(c);
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
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
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
}

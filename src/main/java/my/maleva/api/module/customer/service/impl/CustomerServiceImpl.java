package my.maleva.api.module.customer.service.impl;

import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.dto.request.CustomerSelectRequest;
import my.maleva.api.module.customer.dto.response.CustomerOptionDto;
import my.maleva.api.module.customer.dto.response.CustomerSelectDto;
import my.maleva.api.module.customer.dto.response.CustomerSelectResult;
import my.maleva.api.module.customer.mapper.CustomerMapper;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerQueryRepository;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.customer.service.CustomerQneService;
import my.maleva.api.module.customer.service.CustomerService;
import my.maleva.api.module.customer.service.CustomerWriter;
import my.maleva.api.common.exception.InvalidRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final CustomerQneService qneService;
    private final CustomerWriter writer;

    public CustomerServiceImpl(CustomerRepository repository, CustomerQueryRepository queryRepository,
                               CustomerMapper mapper, CustomerQneService qneService,
                               CustomerWriter writer) {
        this.repository = repository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
        this.qneService = qneService;
        this.writer = writer;
    }

    /**
     * Creates a customer the way {@code SP_Customer} does — account row first,
     * then the customer that points at it — and pushes it to QNE once the
     * insert commits.
     */
    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public CustomerDto create(CustomerDto dto) {
        Integer companyId = requireCompany(dto);
        Customer saved = writer.insert(dto, companyId);
        qneService.pushCreatedAfterCommit(saved);
        return mapper.toDto(saved);
    }

    /**
     * Updates a customer and renames its account row.
     *
     * <p>One deliberate divergence from the procedure: it updates
     * {@code where Id=@Id} with no company in the predicate, so an id from
     * another tenant would be written. The row is loaded scoped to the company
     * here, and an id that does not belong to it is rejected rather than
     * silently edited.
     *
     * <p><b>An edit pushes to QNE too.</b> Legacy branched on
     * {@code Id == 0 || CompanyCode is blank}, so saving a customer that has
     * never reached QNE — including an <i>edit</i> of one — sent a create. The
     * push is a no-op once a QNE code exists, which is the same guard.
     *
     * <p>Legacy also built a {@code Type = 3} update payload here and then
     * dropped it: the only dispatch in {@code CustomerServices.InsertCustomer}
     * is guarded by {@code if (QNEM.Type == 2)}, with no branch for 3. Renaming
     * a customer has therefore never reached QNE, and adding that would be new
     * behaviour rather than a port, so it is deliberately not done here.
     */
    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public CustomerDto update(Integer id, CustomerDto dto) {
        Integer companyId = requireCompany(dto);
        Customer existing = repository.findByIdAndCompanyRefId(id, companyId)
                .orElseThrow(() -> new InvalidRequestException(
                        "Customer " + id + " was not found for this company."));

        Customer saved = writer.update(existing, dto, companyId);
        qneService.pushCreatedAfterCommit(saved);
        return mapper.toDto(saved);
    }

    /**
     * The tenant every write is scoped to. Legacy read it from the {@code Comid}
     * request header; it travels in the payload here, and a save without it is
     * rejected rather than landing under company 0.
     */
    private Integer requireCompany(CustomerDto dto) {
        Integer companyId = dto.getCompanyRefId();
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Company is required to save a customer.");
        }
        return companyId;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "customers", key = "'id_' + #id")
    public CustomerDto getById(Integer id) {
        Customer c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return mapper.toDto(c);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "customers", key = "'name_' + (#name != null ? #name : 'ALL')")
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
    @Transactional(readOnly = true)
    @Cacheable(value = "customers", key = "'options_' + #companyId")
    public List<CustomerOptionDto> options(Integer companyId) {
        return repository.findOptions(companyId);
    }

    @Override
    public CustomerSelectResult selectCustomer(CustomerSelectRequest request) {

        long count = queryRepository.countCustomers(request);

        List<CustomerSelectDto> customers =
                queryRepository.findCustomers(request);

        return new CustomerSelectResult(customers, count);
    }


    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public void softDelete(Integer customerId) {
        Customer customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setActive(2);
        repository.save(customer);
    }



    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Customer not found: " + id);
        }
        repository.deleteById(id);
    }
}

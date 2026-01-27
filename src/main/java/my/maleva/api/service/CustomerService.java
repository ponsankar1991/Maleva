package my.maleva.api.service;

import my.maleva.api.dto.CustomerDto;
import my.maleva.api.model.Customer;
import my.maleva.api.repo.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public CustomerDto create(CustomerDto dto) {
        Customer entity = toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setModifiedDate(now);
        Customer saved = repository.save(entity);
        return toDto(saved);
    }

    public CustomerDto update(Integer id, CustomerDto dto) {
        Customer existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        // copy updatable fields from dto to entity (skip id and createdDate)
        copyDtoToEntity(dto, existing);
        existing.setModifiedDate(LocalDateTime.now());
        Customer saved = repository.save(existing);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public CustomerDto getById(Integer id) {
        Customer c = repository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toDto(c);
    }

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

    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Customer not found: " + id);
        }
        repository.deleteById(id);
    }

    // -- mapping helpers
    private CustomerDto toDto(Customer c) {
        if (c == null) return null;
        return CustomerDto.builder()
                .id(c.getId())
                .companyRefId(c.getCompanyRefId())
                .customerName(c.getCustomerName())
                .cNumberDisplay(c.getCNumberDisplay())
                .cNumber(c.getCNumber())
                .address1(c.getAddress1())
                .address2(c.getAddress2())
                .city(c.getCity())
                .zipcode(c.getZipcode())
                .country(c.getCountry())
                .symbolRefid(c.getSymbolRefid())
                .paymentTermsRefid(c.getPaymentTermsRefid())
                .gstNo(c.getGstNo())
                .email(c.getEmail())
                .mobileNo(c.getMobileNo())
                .userName(c.getUserName())
                .password(c.getPassword())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .tokenId(c.getTokenId())
                .oEmail(c.getOEmail())
                .oName(c.getOName())
                .oPhone(c.getOPhone())
                .aEmail(c.getAEmail())
                .aName(c.getAName())
                .aPhone(c.getAPhone())
                .active(c.getActive())
                .createdDate(c.getCreatedDate())
                .modifiedDate(c.getModifiedDate())
                .modifiedBy(c.getModifiedBy())
                .aEmail1(c.getAEmail1())
                .oEmail1(c.getOEmail1())
                .state(c.getState())
                .address3(c.getAddress3())
                .personId(c.getPersonId())
                .openingBalance(c.getOpeningBalance())
                .accountRefid(c.getAccountRefid())
                .tinNo(c.getTinNo())
                .sstNo(c.getSstNo())
                .msicCode(c.getMsicCode())
                .serviceTaxType(c.getServiceTaxType())
                .bankName(c.getBankName())
                .accountNo(c.getAccountNo())
                .companyCode(c.getCompanyCode())
                .updateId(c.getUpdateId())
                .tintype(c.getTintype())
                .customerTin(c.getCustomerTin())
                .eInvoice(c.getEInvoice())
                .exemptionNo(c.getExemptionNo())
                .expiryDate(c.getExpiryDate())
                .exemptionDetails(c.getExemptionDetails())
                .registrationNo(c.getRegistrationNo())
                .customerCity(c.getCustomerCity())
                .countryId(c.getCountryId())
                .build();
    }

    private Customer toEntity(CustomerDto d) {
        if (d == null) return null;
        Customer c = Customer.builder()
                .id(d.getId())
                .companyRefId(d.getCompanyRefId())
                .customerName(d.getCustomerName())
                .cNumberDisplay(d.getCNumberDisplay())
                .cNumber(d.getCNumber())
                .address1(d.getAddress1())
                .address2(d.getAddress2())
                .city(d.getCity())
                .zipcode(d.getZipcode())
                .country(d.getCountry())
                .symbolRefid(d.getSymbolRefid())
                .paymentTermsRefid(d.getPaymentTermsRefid())
                .gstNo(d.getGstNo())
                .email(d.getEmail())
                .mobileNo(d.getMobileNo())
                .userName(d.getUserName())
                .password(d.getPassword())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .tokenId(d.getTokenId())
                .oEmail(d.getOEmail())
                .oName(d.getOName())
                .oPhone(d.getOPhone())
                .aEmail(d.getAEmail())
                .aName(d.getAName())
                .aPhone(d.getAPhone())
                .active(d.getActive())
                .createdDate(d.getCreatedDate())
                .modifiedDate(d.getModifiedDate())
                .modifiedBy(d.getModifiedBy())
                .aEmail1(d.getAEmail1())
                .oEmail1(d.getOEmail1())
                .state(d.getState())
                .address3(d.getAddress3())
                .personId(d.getPersonId())
                .openingBalance(d.getOpeningBalance())
                .accountRefid(d.getAccountRefid())
                .tinNo(d.getTinNo())
                .sstNo(d.getSstNo())
                .msicCode(d.getMsicCode())
                .serviceTaxType(d.getServiceTaxType())
                .bankName(d.getBankName())
                .accountNo(d.getAccountNo())
                .companyCode(d.getCompanyCode())
                .updateId(d.getUpdateId())
                .tintype(d.getTintype())
                .customerTin(d.getCustomerTin())
                .eInvoice(d.getEInvoice())
                .exemptionNo(d.getExemptionNo())
                .expiryDate(d.getExpiryDate())
                .exemptionDetails(d.getExemptionDetails())
                .registrationNo(d.getRegistrationNo())
                .customerCity(d.getCustomerCity())
                .countryId(d.getCountryId())
                .build();
        return c;
    }

    private void copyDtoToEntity(CustomerDto d, Customer c) {
        // copy mutable fields
        c.setCompanyRefId(d.getCompanyRefId());
        c.setCustomerName(d.getCustomerName());
        c.setCNumberDisplay(d.getCNumberDisplay());
        c.setCNumber(d.getCNumber());
        c.setAddress1(d.getAddress1());
        c.setAddress2(d.getAddress2());
        c.setCity(d.getCity());
        c.setZipcode(d.getZipcode());
        c.setCountry(d.getCountry());
        c.setSymbolRefid(d.getSymbolRefid());
        c.setPaymentTermsRefid(d.getPaymentTermsRefid());
        c.setGstNo(d.getGstNo());
        c.setEmail(d.getEmail());
        c.setMobileNo(d.getMobileNo());
        c.setUserName(d.getUserName());
        c.setPassword(d.getPassword());
        c.setLatitude(d.getLatitude());
        c.setLongitude(d.getLongitude());
        c.setTokenId(d.getTokenId());
        c.setOEmail(d.getOEmail());
        c.setOName(d.getOName());
        c.setOPhone(d.getOPhone());
        c.setAEmail(d.getAEmail());
        c.setAName(d.getAName());
        c.setAPhone(d.getAPhone());
        c.setActive(d.getActive());
        c.setModifiedBy(d.getModifiedBy());
        c.setAEmail1(d.getAEmail1());
        c.setOEmail1(d.getOEmail1());
        c.setState(d.getState());
        c.setAddress3(d.getAddress3());
        c.setPersonId(d.getPersonId());
        c.setOpeningBalance(d.getOpeningBalance());
        c.setAccountRefid(d.getAccountRefid());
        c.setTinNo(d.getTinNo());
        c.setSstNo(d.getSstNo());
        c.setMsicCode(d.getMsicCode());
        c.setServiceTaxType(d.getServiceTaxType());
        c.setBankName(d.getBankName());
        c.setAccountNo(d.getAccountNo());
        c.setCompanyCode(d.getCompanyCode());
        c.setUpdateId(d.getUpdateId());
        c.setTintype(d.getTintype());
        c.setCustomerTin(d.getCustomerTin());
        c.setEInvoice(d.getEInvoice());
        c.setExemptionNo(d.getExemptionNo());
        c.setExpiryDate(d.getExpiryDate());
        c.setExemptionDetails(d.getExemptionDetails());
        c.setRegistrationNo(d.getRegistrationNo());
        c.setCustomerCity(d.getCustomerCity());
        c.setCountryId(d.getCountryId());
    }
}

package my.maleva.api.module.customer.service;

import my.maleva.api.module.customer.dto.CustomerDto;
import my.maleva.api.module.customer.dto.request.CustomerSelectRequest;
import my.maleva.api.module.customer.dto.response.CustomerSelectResult;

import java.util.List;

public interface CustomerService {

    CustomerDto create(CustomerDto dto);

    CustomerDto update(Integer id, CustomerDto dto);

    CustomerDto getById(Integer id);

    List<CustomerDto> findAll(String name);


    void delete(Integer id);
    void softDelete(Integer customerId);
    CustomerSelectResult selectCustomer(CustomerSelectRequest request);


}

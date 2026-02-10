package my.maleva.api.service;

import my.maleva.api.dto.CustomerDto;
import my.maleva.api.dto.request.CustomerSelectRequest;
import my.maleva.api.dto.response.CustomerSelectResult;

import java.util.List;

public interface CustomerService {

    CustomerDto create(CustomerDto dto);

    CustomerDto update(Integer id, CustomerDto dto);

    CustomerDto getById(Integer id);

    List<CustomerDto> findAll(String name);

    void delete(Integer id);

    CustomerSelectResult selectCustomer(CustomerSelectRequest request);
}

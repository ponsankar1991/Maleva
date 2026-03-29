package my.maleva.api.module.communication.service;

import my.maleva.api.module.communication.dto.PhoneCallEntryDto;

import java.util.List;

public interface PhoneCallEntryService {
    PhoneCallEntryDto create(PhoneCallEntryDto dto);
    PhoneCallEntryDto getById(Integer id);
    List<PhoneCallEntryDto> listAll();
    PhoneCallEntryDto update(Integer id, PhoneCallEntryDto dto);
    void delete(Integer id);
}

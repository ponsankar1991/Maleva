package my.maleva.api.service;

import my.maleva.api.dto.PhoneCallEntryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PhoneCallEntryService {
    PhoneCallEntryDto create(PhoneCallEntryDto dto);
    PhoneCallEntryDto getById(Integer id);
    List<PhoneCallEntryDto> listAll();
    PhoneCallEntryDto update(Integer id, PhoneCallEntryDto dto);
    void delete(Integer id);
}

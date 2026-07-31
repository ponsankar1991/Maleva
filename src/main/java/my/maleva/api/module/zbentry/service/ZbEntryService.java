package my.maleva.api.module.zbentry.service;

import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZbEntryService {

    Page<ZbEntryResponse> searchZbEntries(ZbEntrySearchRequest request, Pageable pageable);

}

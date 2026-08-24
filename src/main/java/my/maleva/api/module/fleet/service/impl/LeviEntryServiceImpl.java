package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.fleet.entity.LeviEntry;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.LeviEntryRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.LeviEntryService;
import my.maleva.api.module.rti.repository.RTIMasterRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Levi entries. The behaviour is {@link AbstractPassEntryService}; this only
 * names the procedure, sequence and prefix that make it a levi entry.
 */
@Service
public class LeviEntryServiceImpl extends AbstractPassEntryService<LeviEntry>
        implements LeviEntryService {

    public LeviEntryServiceImpl(LeviEntryRepository repository,
                                TruckMasterRepository truckMasterRepository,
                                DriverMasterRepository driverMasterRepository,
                                RTIMasterRepository rtiMasterRepository,
                                JdbcTemplate jdbcTemplate,
                                ObjectMapper objectMapper) {
        super(repository, truckMasterRepository, driverMasterRepository,
                rtiMasterRepository, jdbcTemplate, objectMapper);
    }

    @Override
    protected String storedProcedureName() {
        return "SP_LeviEntry";
    }

    @Override
    protected String sequenceName() {
        return "LeviEntry";
    }

    @Override
    protected String numberPrefix() {
        return "LE";
    }

    @Override
    protected String documentLabel() {
        return "levi entry";
    }
}

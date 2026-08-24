package my.maleva.api.module.fleet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.fleet.entity.AutoPassEntry;
import my.maleva.api.module.fleet.repository.AutoPassEntryRepository;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.fleet.service.AutoPassEntryService;
import my.maleva.api.module.rti.repository.RTIMasterRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Auto pass entries. The behaviour is {@link AbstractPassEntryService}; this
 * only names the procedure, sequence and prefix that make it an auto pass.
 */
@Service
public class AutoPassEntryServiceImpl extends AbstractPassEntryService<AutoPassEntry>
        implements AutoPassEntryService {

    public AutoPassEntryServiceImpl(AutoPassEntryRepository repository,
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
        return "SP_AutoPassEntry";
    }

    @Override
    protected String sequenceName() {
        return "AutoPassEntry";
    }

    @Override
    protected String numberPrefix() {
        return "AP";
    }

    @Override
    protected String documentLabel() {
        return "auto pass entry";
    }
}

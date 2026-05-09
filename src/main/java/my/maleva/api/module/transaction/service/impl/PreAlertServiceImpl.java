package my.maleva.api.module.transaction.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.PreAlertDto;
import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import my.maleva.api.module.transaction.mapper.PreAlertMapper;
import my.maleva.api.module.transaction.repository.PreAlertRepository;
import my.maleva.api.module.transaction.repository.PreAlertReportRepository;
import my.maleva.api.module.transaction.service.PreAlertService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PreAlert Service Implementation
 * Handles both report operations and CRUD operations for PreAlert entities
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreAlertServiceImpl implements PreAlertService {

    private final PreAlertRepository preAlertRepository;
    private final PreAlertReportRepository preAlertReportRepository;
    private final PreAlertMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * REPORT OPERATIONS
     */

    @Override
    public List<PreAlertReportModel> getPreAlertReport(PreAlertSearchModel searchModel) {
        log.info("Fetching pre-alert report for comId={}, customerId={}, jobId={}",
                searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId());

        try {
            // Validate input
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model or comId provided");
                return List.of();
            }

            return preAlertReportRepository.getPreAlertReportData(searchModel);

        } catch (Exception e) {
            log.error("Error fetching pre-alert report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch pre-alert report", e);
        }
    }

    @Override
    public List<PreAlertReportModel> getPreAlertReportPaginated(PreAlertSearchModel searchModel) {
        log.info("Fetching paginated pre-alert report for comId={}",
                searchModel.getComId());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model or comId provided");
                return List.of();
            }

            return preAlertReportRepository.getPreAlertReportData(searchModel);

        } catch (Exception e) {
            log.error("Error fetching paginated pre-alert report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch paginated pre-alert report", e);
        }
    }

    @Override
    public long getPreAlertReportCount(PreAlertSearchModel searchModel) {
        log.info("Counting pre-alert records for comId={}", searchModel.getComId());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model or comId provided");
                return 0;
            }

            return preAlertReportRepository.getPreAlertReportData(searchModel).size();

        } catch (Exception e) {
            log.error("Error counting pre-alert records: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count pre-alert records", e);
        }
    }

    @Override
    public String exportPreAlertReportToCSV(PreAlertSearchModel searchModel) {
        log.info("Exporting pre-alert report to CSV for comId={}", searchModel.getComId());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model or comId provided");
                return "";
            }

            List<PreAlertReportModel> data = preAlertReportRepository.getPreAlertReportData(searchModel);

            // Simple CSV generation - in real implementation, use a proper CSV library
            StringBuilder csv = new StringBuilder();
            csv.append("SaleDate,JobNo,Vessel,Commodity,Port,Destination,PickupDate,DeliveryDate,ETA,ETB,ETD,OETA,OETB,OETD,CustomerName,EmployeeName,AgentName,AgentPhone,Remarks\n");

            for (PreAlertReportModel item : data) {
                csv.append(item.getSaleDate()).append(",")
                   .append(item.getJobNo()).append(",")
                   .append(item.getVessel()).append(",")
                   .append(item.getCommodity()).append(",")
                   .append(item.getSPort()).append(",")
                   .append(item.getDestination()).append(",")
                   .append(item.getPickupDate()).append(",")
                   .append(item.getDeliveryDate()).append(",")
                   .append(item.getEta()).append(",")
                   .append(item.getEtb()).append(",")
                   .append(item.getEtd()).append(",")
                   .append(item.getOeta()).append(",")
                   .append(item.getOetb()).append(",")
                   .append(item.getOetd()).append(",")
                   .append(item.getCustomerName()).append(",")
                   .append(item.getEmployeeName()).append(",")
                   .append(item.getAgentName()).append(",")
                   .append(item.getAgentPhone()).append(",")
                   .append(item.getRemarks()).append("\n");
            }

            return csv.toString();

        } catch (Exception e) {
            log.error("Error exporting pre-alert report to CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export pre-alert report to CSV", e);
        }
    }

    /**
     * CRUD OPERATIONS FOR PREALERT ENTITIES
     */

    @Override
    public List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId) {
        log.info("Fetching PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.findByPreAlertMasterRefId(preAlertMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByPreAlertMasterId(Integer preAlertMasterRefId) {
        log.info("Counting PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.countByPreAlertMasterRefId(preAlertMasterRefId);
    }
}

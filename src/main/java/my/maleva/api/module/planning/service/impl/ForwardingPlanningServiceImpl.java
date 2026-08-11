package my.maleva.api.module.planning.service.impl;

import my.maleva.api.module.planning.dto.ForwardingPlanningReportDto;
import my.maleva.api.module.planning.service.ForwardingPlanningService;
import my.maleva.api.module.rti.repository.RTIRouteActivitiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForwardingPlanningServiceImpl implements ForwardingPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(ForwardingPlanningServiceImpl.class);

    @Autowired
    private RTIRouteActivitiesRepository routeActivitiesRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ForwardingPlanningReportDto> getForwardingPlanningReport(Integer companyRefId, LocalDate fromDate, LocalDate toDate) {
        logger.info("Generating Forwarding Planning Report for company: {}, dates: {} to {}", companyRefId, fromDate, toDate);
        
        // Start of day to end of day
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        List<Object[]> rawResults = routeActivitiesRepository.getForwardingPlanningReport(companyRefId, start, end);
        List<ForwardingPlanningReportDto> report = new ArrayList<>();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Object[] row : rawResults) {
            String lorryNo = row[0] != null ? row[0].toString() : null;
            String driverName = row[1] != null ? row[1].toString() : null;
            String driverNumber = row[2] != null ? row[2].toString() : null;
            String agentName = row[3] != null ? row[3].toString() : null;
            String contact = row[4] != null ? row[4].toString() : null;
            String fromLocation = row[5] != null ? row[5].toString() : null;
            
            Object etaObj = row[6];
            LocalDateTime eta = null;
            if (etaObj instanceof Timestamp) {
                eta = ((Timestamp) etaObj).toLocalDateTime();
            } else if (etaObj instanceof LocalDateTime) {
                eta = (LocalDateTime) etaObj;
            }

            String etaStr = "";
            if (eta != null) {
                etaStr = eta.format(timeFormatter); // e.g. "2026-08-01 12:23:00"
            }

            String jobType = row[7] != null ? row[7].toString() : null;
            String port = row[8] != null ? row[8].toString() : null;
            String remarks = row[9] != null ? row[9].toString() : null;
            String fullRoute = row[10] != null ? row[10].toString() : null;
            Integer marqisStatus = row[11] != null ? Integer.valueOf(row[11].toString()) : null;
            
            Integer cNumber = row[12] != null ? Integer.valueOf(row[12].toString()) : null;
            String rtinumber = row[13] != null ? row[13].toString() : null;
            // row[14] is saleDate (skip for now)
            Integer rtiId = row[15] != null ? Integer.valueOf(row[15].toString()) : null;

            report.add(ForwardingPlanningReportDto.builder()
                    .lorryNo(lorryNo)
                    .driverName(driverName)
                    .driverNumber(driverNumber)
                    .agentName(agentName)
                    .contact(contact)
                    .fromLocation(fromLocation)
                    .eta(etaStr)
                    .jobType(jobType)
                    .port(port)
                    .remarks(remarks)
                    .fullRoute(fullRoute)
                    .marqisStatus(marqisStatus)
                    .cNumber(cNumber)
                    .rtinumber(rtinumber)
                    .rtiId(rtiId)
                    .build());
        }

        logger.info("Generated {} report rows.", report.size());
        return report;
    }
}

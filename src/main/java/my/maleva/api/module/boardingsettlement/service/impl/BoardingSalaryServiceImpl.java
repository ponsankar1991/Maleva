package my.maleva.api.module.boardingsettlement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.boardingsettlement.dto.BoardingSalaryReportDto;
import my.maleva.api.module.boardingsettlement.entity.BoardingEvent;
import my.maleva.api.module.boardingsettlement.repository.BoardingEventRepository;
import my.maleva.api.module.boardingsettlement.service.BoardingSalaryService;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BoardingSalaryServiceImpl implements BoardingSalaryService {

    private final BoardingEventRepository boardingEventRepository;
    private final EmployeeMasterRepository employeeMasterRepository;
    private final SaleOrderMasterRepository saleOrderMasterRepository;

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<BoardingSalaryReportDto> calculateMonthlySalary(String fromDateStr, String toDateStr, Integer employeeId, String portName) {
        log.info("Calculating Boarding Salary Report - fromDate: {}, toDate: {}, employeeId: {}, portName: {}", fromDateStr, toDateStr, employeeId, portName);

        LocalDateTime fromDate = parseDateStartOfDay(fromDateStr);
        LocalDateTime toDate = parseDateEndOfDay(toDateStr);

        List<BoardingEvent> events;
        if (fromDate != null && toDate != null) {
            events = boardingEventRepository.findByBoardingDateBetweenOrderByBoardingDateAsc(fromDate, toDate);
        } else {
            events = boardingEventRepository.findAllByOrderByBoardingDateAsc();
        }

        // Apply portName filter if provided
        if (portName != null && !portName.trim().isEmpty()) {
            final String filterPort = portName.trim().toLowerCase();
            events = events.stream()
                    .filter(e -> e.getPortName() != null && e.getPortName().toLowerCase().contains(filterPort))
                    .collect(Collectors.toList());
        }

        if (events.isEmpty()) {
            log.info("No boarding events found for the specified date range.");
            return Collections.emptyList();
        }

        // 1. Prepare fast lookup maps for Employee Names and Job Numbers
        Set<Integer> empIds = events.stream()
                .map(BoardingEvent::getEmployeeRefId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Integer, String> empNameMap = new HashMap<>();
        if (!empIds.isEmpty()) {
            employeeMasterRepository.findAllById(empIds).forEach(emp -> 
                    empNameMap.put(emp.getId(), emp.getEmployeeName() != null ? emp.getEmployeeName() : "Officer #" + emp.getId())
            );
        }

        Set<Integer> jobIds = events.stream()
                .map(BoardingEvent::getSaleOrderMasterRefId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Integer, String> jobNumMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            saleOrderMasterRepository.findAllById(jobIds).forEach(job -> {
                String num = job.getCNumberDisplay();
                if (num == null || num.trim().isEmpty()) {
                    num = (job.getCNumber() != null) ? String.valueOf(job.getCNumber()) : null;
                }
                if (num == null || num.trim().isEmpty()) {
                    num = String.valueOf(job.getId());
                }
                jobNumMap.put(job.getId(), num);
            });
        }

        // 2. Group events by VesselName, Date (yyyy-MM-dd), and TagType (LOADING/OFFLOADING) to separate tasks
        Map<String, List<BoardingEvent>> groupedByVesselAndDate = events.stream()
                .collect(Collectors.groupingBy(e -> {
                    String vName = e.getVesselName() != null ? e.getVesselName().trim().toUpperCase() : "UNKNOWN_VESSEL";
                    String dStr = e.getBoardingDate() != null ? e.getBoardingDate().toLocalDate().toString() : "UNKNOWN_DATE";
                    String tag = e.getTagType() != null ? e.getTagType().trim().toUpperCase() : "UNKNOWN_TAG";
                    return vName + "||" + dStr + "||" + tag;
                }));

        List<BoardingSalaryReportDto> reportList = new ArrayList<>();

        for (Map.Entry<String, List<BoardingEvent>> entry : groupedByVesselAndDate.entrySet()) {
            List<BoardingEvent> vesselDayEvents = entry.getValue();

            // Count distinct officers who boarded this vessel on this day
            long distinctOfficers = vesselDayEvents.stream()
                    .map(BoardingEvent::getEmployeeRefId)
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .count();

            // Apply rate rule: 1 officer -> RM50, 2 officers -> RM30, 3+ officers -> RM20
            double calculatedRate = 20.0;
            if (distinctOfficers == 1) {
                calculatedRate = 50.0;
            } else if (distinctOfficers == 2) {
                calculatedRate = 30.0;
            }

            // Group events in this vessel/day by Officer (so multiple jobs for same officer collapse into 1 row)
            Map<Integer, List<BoardingEvent>> eventsByOfficer = vesselDayEvents.stream()
                    .filter(e -> e.getEmployeeRefId() != null && e.getEmployeeRefId() > 0)
                    .collect(Collectors.groupingBy(BoardingEvent::getEmployeeRefId));

            for (Map.Entry<Integer, List<BoardingEvent>> officerEntry : eventsByOfficer.entrySet()) {
                Integer empRefId = officerEntry.getKey();

                // Apply frontend employee filter AFTER rate calculation
                if (employeeId != null && employeeId > 0 && !empRefId.equals(employeeId)) {
                    continue;
                }

                List<BoardingEvent> officerEvents = officerEntry.getValue();
                BoardingEvent firstEvent = officerEvents.get(0);

                String employeeName = empNameMap.getOrDefault(empRefId, "Officer #" + empRefId);
                String vesselName = firstEvent.getVesselName();
                String boardingDateStr = firstEvent.getBoardingDate() != null ? firstEvent.getBoardingDate().toLocalDate().toString() : "";

                // Combine all related job numbers into a clean comma-separated string
                String relatedJobs = officerEvents.stream()
                        .map(e -> jobNumMap.getOrDefault(e.getSaleOrderMasterRefId(), String.valueOf(e.getSaleOrderMasterRefId())))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(", "));

                // Combine tag types
                String tagTypes = officerEvents.stream()
                        .map(BoardingEvent::getTagType)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(", "));

                // Combine port names
                String portNames = officerEvents.stream()
                        .map(BoardingEvent::getPortName)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(p -> !p.isEmpty())
                        .distinct()
                        .collect(Collectors.joining(", "));

                BoardingSalaryReportDto row = BoardingSalaryReportDto.builder()
                        .employeeRefId(empRefId)
                        .employeeName(employeeName)
                        .vesselName(vesselName)
                        .boardingDate(boardingDateStr)
                        .calculatedRate(calculatedRate)
                        .relatedJobs(relatedJobs)
                        .tagTypes(tagTypes)
                        .portName(portNames)
                        .build();

                reportList.add(row);
            }
        }

        // 3. Sort cleanly by boardingDate ASC, then vesselName ASC, then employeeName ASC
        reportList.sort(Comparator.comparing(BoardingSalaryReportDto::getBoardingDate, Comparator.nullsLast(String::compareTo))
                .thenComparing(BoardingSalaryReportDto::getVesselName, Comparator.nullsLast(String::compareTo))
                .thenComparing(BoardingSalaryReportDto::getEmployeeName, Comparator.nullsLast(String::compareTo)));

        log.info("Generated {} salary report rows after filtering.", reportList.size());
        return reportList;
    }

    private LocalDateTime parseDateStartOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.of(2020, 1, 1, 0, 0);
        }
        try {
            String clean = dateStr.trim();
            if (clean.contains("T")) {
                return LocalDateTime.parse(clean);
            }
            if (clean.contains("/")) {
                return LocalDate.parse(clean, DD_MM_YYYY).atStartOfDay();
            }
            return LocalDate.parse(clean, YYYY_MM_DD).atStartOfDay();
        } catch (Exception e) {
            log.warn("Could not parse fromDate: {}. Using default start date.", dateStr);
            return LocalDateTime.of(2020, 1, 1, 0, 0);
        }
    }

    private LocalDateTime parseDateEndOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        }
        try {
            String clean = dateStr.trim();
            if (clean.contains("T")) {
                return LocalDateTime.parse(clean);
            }
            if (clean.contains("/")) {
                return LocalDate.parse(clean, DD_MM_YYYY).atTime(LocalTime.MAX);
            }
            return LocalDate.parse(clean, YYYY_MM_DD).atTime(LocalTime.MAX);
        } catch (Exception e) {
            log.warn("Could not parse toDate: {}. Using default end date.", dateStr);
            return LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        }
    }
}

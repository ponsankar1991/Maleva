package my.maleva.api.module.boardingsettlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.boardingsettlement.entity.BoardingEvent;
import my.maleva.api.module.boardingsettlement.repository.BoardingEventRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardingEventSyncService {

    private final BoardingEventRepository boardingEventRepository;
    private final SaleOrderMasterRepository saleOrderMasterRepository;

    @Transactional
    public int syncAllHistoricalJobs() {
        log.info("Starting bulk sync of all historical SaleOrderMaster jobs to BoardingEvent...");
        List<SaleOrderMaster> allJobs = saleOrderMasterRepository.findAll();
        int count = 0;
        for (SaleOrderMaster job : allJobs) {
            syncEventsForJob(job);
            count++;
        }
        log.info("Completed bulk sync. Synced {} jobs.", count);
        return count;
    }

    @Transactional
    public void syncEventsForJob(SaleOrderMaster job) {
        if (job == null || job.getId() == null) {
            return;
        }

        log.info("Syncing BoardingEvents for SaleOrderMaster ID: {}", job.getId());

        // 1. Delete existing events to prevent duplicates
        boardingEventRepository.deleteBySaleOrderMasterRefId(job.getId());

        LocalDateTime now = LocalDateTime.now();

        // Determine Loading and Offloading Dates (Fallback from ETA to ETB)
        LocalDateTime loadingDate = (job.getEta() != null) ? job.getEta() : job.getEtb();
        
        LocalDateTime offloadingDate = (job.getOeta() != null) ? job.getOeta() : job.getOetb();
        if (offloadingDate == null) {
            offloadingDate = loadingDate; // Fallback to primary ETA if no OETA exists
        }

        // Determine Vessel Names (Fallback to generic Vessel column if specific ones are missing)
        String loadingVessel = (job.getLoadingvesselname() != null && !job.getLoadingvesselname().trim().isEmpty()) 
                ? job.getLoadingvesselname() : job.getVessel();
                
        String offloadingVessel = (job.getOffvesselname() != null && !job.getOffvesselname().trim().isEmpty()) 
                ? job.getOffvesselname() : job.getVessel();

        // ONLY sync Loading and Offloading officers. General officers are excluded as per requirements.

        // 2. Create Loading Boarding Officers
        String loadingPort = job.getSPort();
        boolean hasLoadingOfficer = (job.getLBoardingOfficerRefid() != null && job.getLBoardingOfficerRefid() > 0) ||
                                    (job.getLBoardingOfficer1Refid() != null && job.getLBoardingOfficer1Refid() > 0) ||
                                    (job.getLBoardingOfficer2Refid() != null && job.getLBoardingOfficer2Refid() > 0);
        
        if (!hasLoadingOfficer) {
            createEventIfPresent(job.getId(), 0, "0", "LOADING", loadingVessel, loadingDate, now, loadingPort);
        } else {
            createEventIfPresent(job.getId(), job.getLBoardingOfficerRefid(), job.getLBoardingAmount(), "LOADING", loadingVessel, loadingDate, now, loadingPort);
            createEventIfPresent(job.getId(), job.getLBoardingOfficer1Refid(), job.getLBoardingAmount1(), "LOADING", loadingVessel, loadingDate, now, loadingPort);
            createEventIfPresent(job.getId(), job.getLBoardingOfficer2Refid(), job.getLBoardingAmount2(), "LOADING", loadingVessel, loadingDate, now, loadingPort);
        }

        // 3. Create Offloading Boarding Officers
        String offloadingPort = job.getOPort();
        boolean hasOffloadingOfficer = (job.getOBoardingOfficerRefid() != null && job.getOBoardingOfficerRefid() > 0) ||
                                       (job.getOBoardingOfficer1Refid() != null && job.getOBoardingOfficer1Refid() > 0) ||
                                       (job.getOBoardingOfficer2Refid() != null && job.getOBoardingOfficer2Refid() > 0);
        
        if (!hasOffloadingOfficer) {
            createEventIfPresent(job.getId(), 0, "0", "OFFLOADING", offloadingVessel, offloadingDate, now, offloadingPort);
        } else {
            createEventIfPresent(job.getId(), job.getOBoardingOfficerRefid(), job.getOBoardingAmount(), "OFFLOADING", offloadingVessel, offloadingDate, now, offloadingPort);
            createEventIfPresent(job.getId(), job.getOBoardingOfficer1Refid(), job.getOBoardingAmount1(), "OFFLOADING", offloadingVessel, offloadingDate, now, offloadingPort);
            createEventIfPresent(job.getId(), job.getOBoardingOfficer2Refid(), job.getOBoardingAmount2(), "OFFLOADING", offloadingVessel, offloadingDate, now, offloadingPort);
        }
    }

    private void createEventIfPresent(Integer jobId, Integer employeeRefId, Double amount, String tagType, String vesselName, LocalDateTime boardingDate, LocalDateTime now, String portName) {
        String amountStr = (amount != null) ? String.valueOf(amount) : null;
        createEventIfPresent(jobId, employeeRefId, amountStr, tagType, vesselName, boardingDate, now, portName);
    }

    private void createEventIfPresent(Integer jobId, Integer employeeRefId, String amount, String tagType, String vesselName, LocalDateTime boardingDate, LocalDateTime now, String portName) {
        // Change <= 0 to < 0 so we can intentionally pass employeeRefId = 0
        if (employeeRefId == null || employeeRefId < 0) {
            return;
        }
        if (vesselName == null || vesselName.trim().isEmpty()) {
            log.debug("Skipping BoardingEvent creation for Job ID {}, Employee ID {}, Tag {}: Vessel name is null or empty", jobId, employeeRefId, tagType);
            return;
        }
        if (boardingDate == null) {
            log.info("Skipping BoardingEvent creation for Job ID {}, Employee ID {}, Tag {} (Vessel: {}): Boarding date (ETA/OETA) is null", jobId, employeeRefId, tagType, vesselName);
            return;
        }
        BoardingEvent event = BoardingEvent.builder()
                .saleOrderMasterRefId(jobId)
                .employeeRefId(employeeRefId)
                .amount(amount)
                .tagType(tagType)
                .vesselName(vesselName)
                .boardingDate(boardingDate)
                .createdDate(now)
                .portName(portName)
                .build();
        boardingEventRepository.save(event);
    }
}

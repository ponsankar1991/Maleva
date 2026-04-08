package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.user.repository.AppUserRepository;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;
import my.maleva.api.module.vessalplanning.entity.VesselPlanningDetails;
import my.maleva.api.module.vessalplanning.entity.VesselPlanningMaster;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningDetailsRepository;
import my.maleva.api.module.vessalplanning.repository.VesselPlanningMasterRepository;
import my.maleva.api.module.vessalplanning.service.IVesselPlanningSaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VesselPlanningSaveService implements IVesselPlanningSaveService {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningSaveService.class);

    private static final String SEQUENCE_NAME = "VESSELPLANINGMaster";
    private static final String PREFIX = "VPL";
    private static final int SEQUENCE_PADDING = 9;

    private final VesselPlanningMasterRepository masterRepository;
    private final VesselPlanningDetailsRepository detailsRepository;
    private final EmployeeMasterRepository employeeRepository;
    private final AppUserRepository appUserRepository;
    private final SequenceNoMasterRepository sequenceRepository;

    public VesselPlanningSaveService(
            VesselPlanningMasterRepository masterRepository,
            VesselPlanningDetailsRepository detailsRepository,
            EmployeeMasterRepository employeeRepository,
            AppUserRepository appUserRepository,
            SequenceNoMasterRepository sequenceRepository) {
        this.masterRepository = masterRepository;
        this.detailsRepository = detailsRepository;
        this.employeeRepository = employeeRepository;
        this.appUserRepository = appUserRepository;
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional(readOnly = true)
    public String getMaxVesselPlanningNo(Integer companyId) {
        validateCompanyId(companyId);
        Integer currentMax = sequenceRepository.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        if (currentMax == null) {
            currentMax = 0;
        }
        return formatSequenceNumber(currentMax + 1);
    }

    @Transactional
    public List<VesselPlanningLegacyDtos.SaveResponse> saveAll(
            List<VesselPlanningLegacyDtos.SaveRequest> requests,
            Integer companyId) {

        validateCompanyId(companyId);
        if (requests == null || requests.isEmpty()) {
            throw new InvalidRequestException("At least one vessel planning record is required");
        }

        List<VesselPlanningLegacyDtos.SaveResponse> results = new ArrayList<>();
        for (VesselPlanningLegacyDtos.SaveRequest request : requests) {
            try {
                results.add(processSingle(request, companyId));
            } catch (Exception ex) {
                logger.error("Error saving vessel planning request", ex);
                results.add(VesselPlanningLegacyDtos.SaveResponse.error(resolveMessage(ex)));
            }
        }
        return results;
    }

    @Transactional
    public VesselPlanningLegacyDtos.SaveResponse delete(Integer id, Integer companyId) {
        validateCompanyId(companyId);
        if (id == null || id <= 0) {
            throw new InvalidRequestException("Valid vessel planning id is required");
        }

        VesselPlanningMaster existing = masterRepository.findByIdAndCompanyRefId(id, companyId)
                .orElseThrow(() -> new InvalidRequestException("Vessel planning not found with id: " + id));

        existing.setActive(2);
        existing.setModifiedDate(LocalDateTime.now());
        existing.setModifiedBy(getCurrentUser());
        masterRepository.save(existing);

        return VesselPlanningLegacyDtos.SaveResponse.builder()
                .ok(true)
                .message("Vessel planning deleted successfully")
                .id(id)
                .build();
    }

    private VesselPlanningLegacyDtos.SaveResponse processSingle(
            VesselPlanningLegacyDtos.SaveRequest request,
            Integer companyId) {


        validateEmployee(request.getEmployeeRefId(), companyId);

        request.setCompanyRefId(companyId);

        LocalDateTime now = LocalDateTime.now();
        Integer savedId;
        String billNoDisplay;

        if (request.getId() != null && request.getId() > 0) {
            detailsRepository.deleteByVesselPlanningMasterRefId(request.getId());
        }

        if (request.getId() == null || request.getId() == 0) {
            VesselPlanningMaster master = VesselPlanningMaster.builder()
                    .companyRefId(companyId)
                    .userRefId(normalizeZeroToNull(request.getUserRefId()))
                    .employeeRefId(normalizeZeroToNull(request.getEmployeeRefId()))
                    .lastEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()))
                    .fDate(request.getFDate() != null ? request.getFDate() : now.toLocalDate())
                    .tDate(request.getTDate() != null ? request.getTDate() : now.toLocalDate())
                    .saleDate(toStartOfDay(request.getSaleDate(), now))
                    .search(request.getSearch())
                    .remarks(request.getRemarks())
                    .active(1)
                    .createdDate(now)
                    .createdBy(getCurrentUser())
                    .modifiedDate(now)
                    .modifiedBy(getCurrentUser())
                    .cNumberDisplay(request.getCNumberDisplay() != null ? request.getCNumberDisplay() : "")
                    .cNumber(request.getCNumber() != null ? request.getCNumber() : 0)
                    .build();

            VesselPlanningMaster saved = masterRepository.save(master);
            savedId = saved.getId();
            insertDetails(request.getSaleDetails(), savedId, now);
            billNoDisplay = generateSequenceNumber(companyId, savedId);
        } else {
            VesselPlanningMaster existing = masterRepository.findByIdAndCompanyRefId(request.getId(), companyId)
                    .orElseThrow(() -> new InvalidRequestException("Vessel planning not found with id: " + request.getId()));

            existing.setLastEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()));
            existing.setUserRefId(normalizeZeroToNull(request.getUserRefId()));
            existing.setEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()));
            existing.setFDate(request.getFDate() != null ? request.getFDate() : (existing.getFDate() != null ? existing.getFDate() : now.toLocalDate()));
            existing.setTDate(request.getTDate() != null ? request.getTDate() : (existing.getTDate() != null ? existing.getTDate() : now.toLocalDate()));
            existing.setSaleDate(toStartOfDay(request.getSaleDate(), existing.getSaleDate() != null ? existing.getSaleDate() : now));
            existing.setSearch(request.getSearch());
            existing.setRemarks(request.getRemarks());
            existing.setModifiedDate(now);
            existing.setModifiedBy(getCurrentUser());

            VesselPlanningMaster updated = masterRepository.save(existing);
            savedId = updated.getId();
            billNoDisplay = updated.getCNumberDisplay();
            insertDetails(request.getSaleDetails(), savedId, now);
        }

        return VesselPlanningLegacyDtos.SaveResponse.success(billNoDisplay, savedId);
    }

    private void insertDetails(List<VesselPlanningLegacyDtos.SaveDetailRequest> details, Integer masterId, LocalDateTime now) {
        if (details == null || details.isEmpty()) {
            return;
        }

        for (VesselPlanningLegacyDtos.SaveDetailRequest item : details) {
            if (item.getSaleOrderMasterRefId() == null || item.getSaleOrderMasterRefId() <= 0) {
                throw new InvalidRequestException("SaleOrderMasterRefId is required for all vessel planning rows");
            }

            VesselPlanningDetails detail = VesselPlanningDetails.builder()
                    .vesselPlanningMasterRefId(masterId)
                    .saleOrderMasterRefId(item.getSaleOrderMasterRefId())
                    .remarks(item.getRemarks())
                    .createdDate(now)
                    .modifiedDate(now)
                    .build();

            detailsRepository.save(detail);
        }
    }

    private String generateSequenceNumber(Integer companyId, Integer masterId) {
        Integer currentMax = sequenceRepository.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        if (currentMax == null) {
            currentMax = 0;
        }

        int nextSequenceNo = currentMax + 1;
        updateOrCreateSequence(companyId, nextSequenceNo);
        String formattedNumber = formatSequenceNumber(nextSequenceNo);
        masterRepository.updateCNumberDisplay(masterId, formattedNumber, nextSequenceNo);
        return formattedNumber;
    }

    private void updateOrCreateSequence(Integer companyId, Integer sequenceNo) {
        var existing = sequenceRepository.findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME);
        LocalDateTime now = LocalDateTime.now();

        if (existing.isPresent()) {
            SequenceNoMaster sequence = existing.get();
            sequence.setSequenceNo(sequenceNo);
            sequence.setSequenceDate(now);
            sequence.setSequenceYear(now.getYear());
            sequence.setSequenceMonth(now.getMonthValue());
            sequenceRepository.save(sequence);
            return;
        }

        SequenceNoMaster created = SequenceNoMaster.builder()
                .companyRefId(companyId)
                .sequenceName(SEQUENCE_NAME)
                .sequenceNo(sequenceNo)
                .sequenceDate(now)
                .sequenceYear(now.getYear())
                .sequenceMonth(now.getMonthValue())
                .build();
        sequenceRepository.save(created);
    }

    private void validateUser(Integer userId, Integer companyId) {
        if (userId == null || userId == 0) {
            return;
        }

        boolean exists = appUserRepository.existsByIdAndCompanyRefIdAndActive(userId, companyId, 1);
        if (!exists) {
            throw new InvalidRequestException("Login user not found, id: " + userId);
        }
    }

    private void validateEmployee(Integer employeeId, Integer companyId) {
        if (employeeId == null || employeeId == 0) {
            return;
        }

        boolean exists = employeeRepository.existsByIdAndCompanyRefIdAndActive(employeeId, companyId, 1);
        if (!exists) {
            throw new InvalidRequestException("Employee not found, id: " + employeeId);
        }
    }

    private void validateCompanyId(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new InvalidRequestException("Valid company ID is required");
        }
    }

    private Integer normalizeZeroToNull(Integer value) {
        return value == null || value == 0 ? null : value;
    }

    private LocalDateTime toStartOfDay(LocalDate value, LocalDateTime fallback) {
        return value != null ? value.atStartOfDay() : fallback;
    }

    private String getCurrentUser() {
        return "SYSTEM";
    }

    private String formatSequenceNumber(int value) {
        return String.format("%s%0" + SEQUENCE_PADDING + "d", PREFIX, value);
    }

    private String resolveMessage(Exception ex) {
        return ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
    }
}


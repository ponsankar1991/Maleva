package my.maleva.api.module.rti.service;

import my.maleva.api.module.rti.dto.RtiEmployeeAssignmentRequest;
import my.maleva.api.module.rti.dto.RtiEmployeeAssignmentResponse;
import my.maleva.api.module.rti.dto.RtiJobWiseViewRequest;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;

import java.util.List;

public interface RtiJobWiseService {
    List<RtiJobWiseViewResponse> getJobWiseView(RtiJobWiseViewRequest request);
    
    List<RtiEmployeeAssignmentResponse> getEmployeeAssignments(RtiEmployeeAssignmentRequest request);
}

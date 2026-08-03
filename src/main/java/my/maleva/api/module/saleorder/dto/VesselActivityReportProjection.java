package my.maleva.api.module.saleorder.dto;

import java.util.Date;

public interface VesselActivityReportProjection {
    Date getActivityDate();
    String getVesselName();
    String getActivityType();
    Integer getJobCount();
    String getCNumbers();
    String getPortName();
    java.util.Date getEta();
    java.util.Date getEtb();
    java.util.Date getOeta();
    java.util.Date getOetb();
    String getBoardingOfficers();
}

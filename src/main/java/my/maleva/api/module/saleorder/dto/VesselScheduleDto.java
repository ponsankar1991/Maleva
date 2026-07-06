package my.maleva.api.module.saleorder.dto;

import java.util.Date;

public interface VesselScheduleDto {
    Date getEtaDate();
    String getVesselName();
    String getVesselType();
    String getBoardingOfficer1();
    String getBoardingOfficer2();
    Integer getTotalJobs();
}
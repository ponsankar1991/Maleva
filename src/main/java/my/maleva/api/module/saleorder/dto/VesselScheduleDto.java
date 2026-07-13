package my.maleva.api.module.saleorder.dto;


public interface VesselScheduleDto {
    java.time.LocalDate getEtaDate();
    String getVesselName();
    String getVesselType();
    String getJobNumbers();
    String getBoardingOfficer1();
    String getBoardingOfficer2();
    Integer getTotalJobs();
}
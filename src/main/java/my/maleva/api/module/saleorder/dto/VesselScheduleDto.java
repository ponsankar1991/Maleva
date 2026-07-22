package my.maleva.api.module.saleorder.dto;


public interface VesselScheduleDto {
    java.time.LocalDate getEtaDate();
    String getVesselName();
    String getVesselType();
    String getJobNumbers();
    String getBoardingOfficer1Name();
    String getBoardingOfficer2Name();
    Double getBoardingOfficer1Amount();
    Double getBoardingOfficer2Amount();
    Double getTotalBoardingAmount();
    Integer getTotalJobs();
}
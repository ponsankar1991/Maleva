package my.maleva.api.module.saleorder.dto;


public interface VesselScheduleDto {
    java.time.LocalDate getEtaDate();
    String getVesselName();
    String getVesselType();
    String getJobNumbers();
    Integer getTotalJob();
    String getOfficer1Name();
    Double getOfficer1Amount();
    String getOfficer2Name();
    Double getOfficer2Amount();
    String getOfficer3Name();
    Double getOfficer3Amount();
    String getOfficer4Name();
    Double getOfficer4Amount();
    String getOfficer5Name();
    Double getOfficer5Amount();
    Double getOfficer6Name();
    Double getOfficer6Amount();
    Double getTotalAmount();
}
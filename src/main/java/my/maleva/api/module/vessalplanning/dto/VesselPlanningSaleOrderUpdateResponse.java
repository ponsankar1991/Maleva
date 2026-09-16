package my.maleva.api.module.vessalplanning.dto;

import lombok.Builder;
import lombok.Data;

/**
 * What the job holds after a Vessel Planning update, in the grid's formats, so the screen can
 * update its row without reloading the plan.
 */
@Data
@Builder
public class VesselPlanningSaleOrderUpdateResponse {
    private boolean ok;
    private String message;
    private Integer saleOrderId;

    private Integer jobStatusId;
    private String jobStatus;
    private String cargo;
    private String ptw;

    /** yyyy-MM-dd HH:mm:ss, or "" when empty - the same text the search puts in SETA etc. */
    private String seta;
    private String setb;
    private String setd;
    private String soeta;
    private String soetb;
    private String soetd;

    private Integer loadingOfficer1;
    private Integer loadingOfficer2;
    private Integer loadingOfficer3;
    private Double loadingAmount1;
    private Double loadingAmount2;
    private Double loadingAmount3;

    private Integer offOfficer1;
    private Integer offOfficer2;
    private Integer offOfficer3;
    private Double offAmount1;
    private Double offAmount2;
    private Double offAmount3;
}

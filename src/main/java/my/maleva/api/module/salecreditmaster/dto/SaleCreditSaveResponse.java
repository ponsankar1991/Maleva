package my.maleva.api.module.salecreditmaster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The answer to a save.
 *
 * <p>Carries the legacy envelope the old screen read
 * ({@code ok}, {@code message}, {@code Name}, {@code Id}) and the
 * {@code IsSuccess / StatusCode / Message / Data1} envelope every other
 * migrated screen reads, so one response serves both front ends. The fields
 * are {@code Boolean}, not {@code boolean}, so the generated accessors are
 * {@code getOk()} rather than {@code isOk()} and Jackson emits each property
 * exactly once.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditSaveResponse {

    @JsonProperty("ok")
    private Boolean ok;

    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    @JsonProperty("message")
    private String message;

    /** The credit note number, e.g. CN000000012 — legacy's {@code Name}. */
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("Data1")
    public String getData1() {
        return name;
    }

    @JsonProperty("Data2")
    public Integer getData2() {
        return id;
    }

    @JsonProperty("IsSuccess")
    public Boolean getIsSuccessUpper() {
        return isSuccess;
    }

    @JsonProperty("StatusCode")
    public Integer getStatusCode() {
        return Boolean.TRUE.equals(isSuccess) ? 200 : 400;
    }

    @JsonProperty("Message")
    public String getMessageUpper() {
        return message;
    }

    public static SaleCreditSaveResponse failure(String message) {
        return SaleCreditSaveResponse.builder().ok(false).isSuccess(false).message(message).build();
    }

    public static SaleCreditSaveResponse success(Integer id, String creditNoteNo, String message) {
        return SaleCreditSaveResponse.builder()
                .ok(true).isSuccess(true).id(id).name(creditNoteNo).message(message).build();
    }
}

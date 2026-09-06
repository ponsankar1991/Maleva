package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ReceiptSaveResponseDto
 * Returns saved receipt details matching legacy .NET response:
 * { ok = true, message = ro.Message, Name = ro.Data1, Id = ro.Data2 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSaveResponseDto {

    @JsonProperty("ok")
    private Boolean ok;

    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    @JsonProperty("message")
    private String message;

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
}

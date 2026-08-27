package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE stock (product) insert request. Legacy: ProductInsetQNE (typo is legacy's).
 * Note: "Volumn" is the legacy wire spelling and is preserved.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneStockRequest {
    private int autoCode;
    private String stockCode;
    private String stockName;
    private String stockName2;
    private String description;
    private String baseUom;
    private double minQty;
    private double maxQty;
    private double reorderLevel;
    private double reorderQty;
    private double listPrice;
    private double minPrice;
    private String salesDiscount;
    private double purchasePrice;
    private String purchaseDiscount;
    private String barCode;
    private double weight;
    private double volumn;
    @JsonProperty("IsBundled")
    private boolean isBundled;
    private boolean stockControl;
    private boolean useSerialNo;
    private String serialNoPrefix;
    private String serialNoSuffix;
    private String remark1;
    private String remark2;
    private String remark3;
    private String remark4;
    private String remark5;
    private boolean useBatchNo;
    private String accountPreset;
    private String category;
    private String group;
    @JsonProperty("Class")
    private String stockClass;
    private String defaultInputTaxCode;
    private String defaultOutputTaxCode;
}

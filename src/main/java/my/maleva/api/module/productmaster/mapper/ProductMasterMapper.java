package my.maleva.api.module.productmaster.mapper;

import my.maleva.api.module.productmaster.dto.ProductMasterDto;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMasterMapper {

    /**
     * Convert ProductMaster entity to DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "prodCode", target = "prodCode")
    @Mapping(source = "pcodeDigits", target = "pcodeDigits")
    @Mapping(source = "pname", target = "pname")
    @Mapping(source = "printName", target = "printName")
    @Mapping(source = "secondPCode", target = "secondPCode")
    @Mapping(source = "hsnCode", target = "hsnCode")
    @Mapping(source = "taxCode", target = "taxCode")
    @Mapping(source = "uomCode", target = "uomCode")
    @Mapping(source = "mrp", target = "mrp")
    @Mapping(source = "purchaseRate", target = "purchaseRate")
    @Mapping(source = "landingCost", target = "landingCost")
    @Mapping(source = "salesRate", target = "salesRate")
    @Mapping(source = "saleRateType", target = "saleRateType")
    @Mapping(source = "remarks", target = "remarks")
    @Mapping(source = "activestatus", target = "activestatus")
    @Mapping(source = "sorting", target = "sorting")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    @Mapping(source = "isProduct", target = "isProduct")
    ProductMasterDto toDto(ProductMaster entity);

    /**
     * Convert ProductMaster DTO to entity
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "prodCode", target = "prodCode")
    @Mapping(source = "pcodeDigits", target = "pcodeDigits")
    @Mapping(source = "pname", target = "pname")
    @Mapping(source = "printName", target = "printName")
    @Mapping(source = "secondPCode", target = "secondPCode")
    @Mapping(source = "hsnCode", target = "hsnCode")
    @Mapping(source = "taxCode", target = "taxCode")
    @Mapping(source = "uomCode", target = "uomCode")
    @Mapping(source = "mrp", target = "mrp")
    @Mapping(source = "purchaseRate", target = "purchaseRate")
    @Mapping(source = "landingCost", target = "landingCost")
    @Mapping(source = "salesRate", target = "salesRate")
    @Mapping(source = "saleRateType", target = "saleRateType")
    @Mapping(source = "remarks", target = "remarks")
    @Mapping(source = "activestatus", target = "activestatus")
    @Mapping(source = "sorting", target = "sorting")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    @Mapping(source = "isProduct", target = "isProduct")
    ProductMaster toEntity(ProductMasterDto dto);

    /**
     * Update entity from DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "prodCode", target = "prodCode")
    @Mapping(source = "pcodeDigits", target = "pcodeDigits")
    @Mapping(source = "pname", target = "pname")
    @Mapping(source = "printName", target = "printName")
    @Mapping(source = "secondPCode", target = "secondPCode")
    @Mapping(source = "hsnCode", target = "hsnCode")
    @Mapping(source = "taxCode", target = "taxCode")
    @Mapping(source = "uomCode", target = "uomCode")
    @Mapping(source = "mrp", target = "mrp")
    @Mapping(source = "purchaseRate", target = "purchaseRate")
    @Mapping(source = "landingCost", target = "landingCost")
    @Mapping(source = "salesRate", target = "salesRate")
    @Mapping(source = "saleRateType", target = "saleRateType")
    @Mapping(source = "remarks", target = "remarks")
    @Mapping(source = "activestatus", target = "activestatus")
    @Mapping(source = "sorting", target = "sorting")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    @Mapping(source = "isProduct", target = "isProduct")
    void updateEntityFromDto(ProductMasterDto dto, @MappingTarget ProductMaster entity);
}


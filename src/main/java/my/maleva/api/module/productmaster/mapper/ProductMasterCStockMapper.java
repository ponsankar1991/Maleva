package my.maleva.api.module.productmaster.mapper;

import my.maleva.api.module.productmaster.dto.ProductMasterCStockDto;
import my.maleva.api.module.productmaster.entity.ProductMasterCStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMasterCStockMapper {

    /**
     * Convert ProductMasterCStock entity to DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "productRefId", target = "productRefId")
    @Mapping(source = "cstock", target = "cstock")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    ProductMasterCStockDto toDto(ProductMasterCStock entity);

    /**
     * Convert ProductMasterCStock DTO to entity
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "productRefId", target = "productRefId")
    @Mapping(source = "cstock", target = "cstock")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    ProductMasterCStock toEntity(ProductMasterCStockDto dto);

    /**
     * Update entity from DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "companyRefId", target = "companyRefId")
    @Mapping(source = "productRefId", target = "productRefId")
    @Mapping(source = "cstock", target = "cstock")
    @Mapping(source = "modifiedBy", target = "modifiedBy")
    void updateEntityFromDto(ProductMasterCStockDto dto, @MappingTarget ProductMasterCStock entity);
}


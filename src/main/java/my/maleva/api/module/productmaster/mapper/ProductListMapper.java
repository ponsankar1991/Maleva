package my.maleva.api.module.productmaster.mapper;

import my.maleva.api.module.productmaster.dto.ProductListDto;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductListMapper {

    @Mapping(source = "PName", target = "productName")
    @Mapping(source = "salesRate", target = "saleRate")
    @Mapping(source = "purchaseRate", target = "purRate")
    @Mapping(source = "prodCode", target = "productCode")
    @Mapping(source = "mrp", target = "mrp")
    @Mapping(source = "id", target = "id")
    ProductListDto toDto(ItemMaster itemMaster);
}


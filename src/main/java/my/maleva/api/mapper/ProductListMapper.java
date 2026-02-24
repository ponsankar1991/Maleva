package my.maleva.api.mapper;

import my.maleva.api.dto.ProductListDto;
import my.maleva.api.model.ItemMaster;
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


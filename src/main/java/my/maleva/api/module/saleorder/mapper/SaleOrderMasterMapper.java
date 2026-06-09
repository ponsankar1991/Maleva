package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.invoice.dto.SaleDetailsDto;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.saleorder.dto.*;
import my.maleva.api.module.saleorder.entity.SaleOrderDelivery;
import my.maleva.api.module.saleorder.entity.SaleOrderForwarding;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * SaleOrderMasterMapper - MapStruct mapper for SaleOrderMaster
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface SaleOrderMasterMapper {
    SaleOrderMasterDto toDto(SaleOrderMaster entity);
    SaleOrderMaster toEntity(SaleOrderMasterDto dto);
    SaleOrderMaster toEntity(SaleOrderDTO dto);
    SaleOrderDTO toSaleOrderDto(SaleOrderMasterDto dto);
    List<SaleDetails> toSaleDetailsentity(List<SaleDetailsDto> dto);
    
    @org.mapstruct.Mapping(source = "pickupWeaight", target = "pickupWeight")
    SaleOrderPickup toSaleOrderPickup(PickupDetailDTO dto);
    
    List<SaleOrderPickup> toSaleOrderPickupentity(List<PickupDetailDTO> dto);
    
    @org.mapstruct.Mapping(source = "pickupWeight", target = "pickupWeaight")
    PickupDetailDTO toPickupDetailDTO(SaleOrderPickup entity);
    
    DeliveryDetailDTO toDeliveryDetailDTO(SaleOrderDelivery entity);
    ForwardingDetailDTO toForwardingDetailDTO(SaleOrderForwarding entity);
    
    List<SaleOrderDelivery> toSaleOrderDeliveryentity(List<DeliveryDetailDTO> dto);
    List<SaleOrderForwarding> toSaleOrderForwardingentity(List<ForwardingDetailDTO> dto);
    void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
    void updateEntityFromDto(SaleOrderDTO dto, @MappingTarget SaleOrderMaster entity);
}

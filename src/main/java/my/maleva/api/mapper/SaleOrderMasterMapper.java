package my.maleva.api.mapper;

import my.maleva.api.dto.*;
import my.maleva.api.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * SaleOrderMasterMapper - MapStruct mapper for SaleOrderMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderMasterMapper {
    SaleOrderMasterDto toDto(SaleOrderMaster entity);
    SaleOrderMaster toEntity(SaleOrderMasterDto dto);
    SaleOrderMaster toEntity(SaleOrderDTO dto);
    List<SaleDetails> toSaleDetailsentity(List<SaleDetailDTO> dto);
    List<SaleOrderPickup> toSaleOrderPickupentity(List<PickupDetailDTO> dto);
    List<SaleOrderDelivery> toSaleOrderDeliveryentity(List<DeliveryDetailDTO> dto);
    List<SaleOrderForwarding> toSaleOrderForwardingentity(List<ForwardingDetailDTO> dto);
    void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
}


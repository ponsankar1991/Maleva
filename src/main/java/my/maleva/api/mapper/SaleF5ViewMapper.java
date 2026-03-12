package my.maleva.api.mapper;

import my.maleva.api.dto.SaleDetailsViewModel;
import my.maleva.api.dto.SaleMasterViewModel;
import my.maleva.api.dto.SaleF5View;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

/**
 * SaleF5ViewMapper - MapStruct mapper for SaleF5View
 * Handles mapping between ViewModels for SelectSaleOrder response
 */
@Mapper(componentModel = "spring")
public interface SaleF5ViewMapper {

    /**
     * Create SaleF5View from separate lists of master and detail records
     * @param saleMasterList list of SaleMasterViewModel records
     * @param saleDetailsList list of SaleDetailsViewModel records
     * @return combined SaleF5View object
     */
    @Named("createSaleF5View")
    default SaleF5View createSaleF5View(List<SaleMasterViewModel> saleMasterList,
                                        List<SaleDetailsViewModel> saleDetailsList) {
        return SaleF5View.builder()
                .salemaster(saleMasterList)
                .saledetails(saleDetailsList)
                .build();
    }

    /**
     * Map SaleF5View to itself (useful for transformations if needed)
     */
    SaleF5View mapSaleF5View(SaleF5View source);

    /**
     * Map list of SaleMasterViewModel
     */
    List<SaleMasterViewModel> mapSaleMasterViewModels(List<SaleMasterViewModel> source);

    /**
     * Map list of SaleDetailsViewModel
     */
    List<SaleDetailsViewModel> mapSaleDetailsViewModels(List<SaleDetailsViewModel> source);
}


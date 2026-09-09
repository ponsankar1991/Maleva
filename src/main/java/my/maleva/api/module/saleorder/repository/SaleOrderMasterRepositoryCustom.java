package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.invoice.dto.SaleDetailsViewModel;
import my.maleva.api.module.invoice.dto.SaleMasterViewModel;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SaleOrderMasterRepositoryCustom {

    List<SaleOrderInvoiceCheckDto> checkSaleOrderInvoice(SaleOrderInvoiceCheckRequest request);

    /**
     * Ids of the sale orders matching the search filter.
     *
     * Selects the Id column only. The previous implementation called
     * {@code findAll(specification)} and read {@code getId()} off every result, which
     * hydrated the whole ~200-column SaleOrderMaster entity — plus its persistence
     * context copy — for every row in the date range just to throw it away.
     */
    List<Integer> findFilteredIds(Specification<SaleOrderMaster> specification);

    /**
     * Master rows for the SelectSaleOrder screen, for the given order ids.
     *
     * The id list is sent in batches so the query never exceeds SQL Server's
     * 2,100-parameter ceiling; see {@link SaleOrderSearchQueries#ID_BATCH_SIZE}.
     */
    List<SaleMasterViewModel> findSaleMasterRows(Integer companyId, List<Integer> orderIds);

    /**
     * Detail rows for the given order ids, ordered by SaleOrderDetails.Id as the
     * legacy query was, across batches.
     */
    List<SaleDetailsViewModel> findSaleDetailRows(Integer companyId, List<Integer> orderIds);
}

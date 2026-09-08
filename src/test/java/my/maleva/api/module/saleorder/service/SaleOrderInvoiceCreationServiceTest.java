package my.maleva.api.module.saleorder.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import my.maleva.api.module.invoice.service.SaleInvoiceTransactionService;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.jobs.repository.JobStatusMasterRepository;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCreated;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceLink;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoicePreview;
import my.maleva.api.module.saleorder.dto.SaleOrderStatusUpdateDto;
import my.maleva.api.module.saleorder.entity.SaleOrderDetails;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderDetailsRepository;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class SaleOrderInvoiceCreationServiceTest {

    private static final int COMPANY = 6;
    private static final int ORDER_ID = 20995;
    private static final int COMPLETED_ID = 8;

    private final SaleOrderMasterRepository saleOrders = mock(SaleOrderMasterRepository.class);
    private final SaleOrderDetailsRepository details = mock(SaleOrderDetailsRepository.class);
    private final ItemMasterRepository items = mock(ItemMasterRepository.class);
    private final UomRepository uoms = mock(UomRepository.class);
    private final JobStatusMasterRepository statuses = mock(JobStatusMasterRepository.class);
    private final SaleOrderInvoiceLinkService links = mock(SaleOrderInvoiceLinkService.class);
    private final SaleInvoiceTransactionService invoices = mock(SaleInvoiceTransactionService.class);
    private final SaleOrderMasterService saleOrderService = mock(SaleOrderMasterService.class);
    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);

    private final SaleOrderInvoiceCreationService service = new SaleOrderInvoiceCreationService(
            saleOrders, details, items, uoms, statuses, links, invoices, saleOrderService, jdbc);

    private SaleOrderMaster order;

    @BeforeEach
    void setUp() {
        order = new SaleOrderMaster();
        order.setId(ORDER_ID);
        order.setCompanyRefId(COMPANY);
        order.setActive(1);
        order.setCNumberDisplay("TR002601394");
        order.setCustomerRefId(1109);
        order.setJobMasterRefId(10);
        order.setEmployeeRefId(77);
        order.setJStatus(6);
        order.setBillType(null);
        order.setCurrencyValue(null);
        order.setSPort("PKG");
        order.setForwardingSMKNo("SMK-1");
        when(saleOrders.findByIdAndCompanyRefId(ORDER_ID, COMPANY)).thenReturn(Optional.of(order));

        SaleOrderDetails line = new SaleOrderDetails();
        line.setId(1);
        line.setSaleOrderMasterRefId(ORDER_ID);
        line.setItemMasterRefId(54);
        line.setItemQty(2.0);
        line.setSalesRate(500.0);
        line.setTaxPercent(6.0);
        line.setTaxRefId(3);
        line.setSdRemarks("PKG - KLANG");
        when(details.findBySaleOrderMasterRefId(ORDER_ID)).thenReturn(List.of(line));

        ItemMaster item = new ItemMaster();
        item.setId(54);
        item.setProdCode("TR");
        item.setPName("TRANSPORTATION");
        item.setUomCode(3);
        when(items.findAllById(anyList())).thenReturn(List.of(item));
        Uom trip = new Uom();
        trip.setId(3);
        trip.setDescription("TRIP ");
        when(uoms.findAllById(anyList())).thenReturn(List.of(trip));

        JobStatusMaster completed = new JobStatusMaster();
        completed.setId(COMPLETED_ID);
        completed.setCompanyRefId(COMPANY);
        completed.setName("JOB COMPLET ");
        JobStatusMaster waiting = new JobStatusMaster();
        waiting.setId(6);
        waiting.setName("WAITING FOR BILLING");
        when(statuses.findSelectableByCompanyId(eq(COMPANY), anyInt())).thenReturn(List.of(waiting, completed));

        when(links.find(ORDER_ID, COMPANY))
                .thenReturn(Optional.of(SaleOrderInvoiceLink.notInvoiced(ORDER_ID, "TR002601394", "NX TRANSPORT")));

        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
    }

    @Test
    void previewCalculatesLinesTheWayTheInvoiceScreenDoes() {
        SaleOrderInvoicePreview preview = service.preview(ORDER_ID, COMPANY);

        assertThat(preview.canCreate()).isTrue();
        assertThat(preview.blockers()).isEmpty();
        assertThat(preview.existing()).isNull();
        assertThat(preview.jobNo()).isEqualTo("TR002601394");
        assertThat(preview.invoiceDate()).isEqualTo(LocalDate.now());
        // No BillType on the order: the series comes from the job number.
        assertThat(preview.billType()).isEqualTo("TR");
        assertThat(preview.completedStatus()).isEqualTo("JOB COMPLET");
        assertThat(preview.lines()).hasSize(1);
        SaleOrderInvoicePreview.Line line = preview.lines().get(0);
        assertThat(line.productCode()).isEqualTo("TR");
        assertThat(line.productName()).isEqualTo("TRANSPORTATION");
        assertThat(line.uom()).isEqualTo("TRIP");
        assertThat(line.quantity()).isEqualTo(2.0);
        assertThat(line.rate()).isEqualTo(500.0);
        assertThat(line.taxAmount()).isEqualTo(60.0);
        assertThat(line.amount()).isEqualTo(1060.0);
        assertThat(preview.subtotal()).isEqualTo(1000.0);
        assertThat(preview.taxTotal()).isEqualTo(60.0);
        assertThat(preview.netTotal()).isEqualTo(1060.0);
        // No currency on the order: the preview says so instead of silently using 1.
        assertThat(preview.warnings()).anyMatch(w -> w.contains("currency"));
        assertThat(preview.currencyValue()).isEqualTo(1.0);
    }

    @Test
    void theRequestMirrorsTheInvoiceScreenPush() {
        SaleInvoiceRequestDTO request = service.toInvoiceRequest(order, details.findBySaleOrderMasterRefId(ORDER_ID),
                new SaleOrderInvoiceCreationService.Catalog(Map.of(), Map.of()),
                statuses.findSelectableByCompanyId(COMPANY, 0).get(1));

        assertThat(request.getId()).isZero();
        assertThat(request.getCompanyRefId()).isEqualTo(COMPANY);
        assertThat(request.getCustomerRefId()).isEqualTo(1109);
        assertThat(request.getJobMasterRefId()).isEqualTo(10);
        assertThat(request.getEmployeeRefId()).isEqualTo(77);
        assertThat(request.getSaleOrderMasterNo()).isEqualTo(ORDER_ID);
        assertThat(request.getSaleOrderRefIds()).containsExactly(ORDER_ID);
        assertThat(request.getJStatus()).isEqualTo(COMPLETED_ID);
        assertThat(request.getSPort()).isEqualTo("PKG");
        assertThat(request.getForwardingSmkNo()).isEqualTo("SMK-1");
        assertThat(request.getGrossAmount()).isEqualTo(1060.0);
        assertThat(request.getAmount()).isEqualTo(1060.0);
        assertThat(request.getTaxAmount()).isEqualTo(60.0);
        assertThat(request.getActualNetAmount()).isEqualTo(1060.0);
        SaleInvoiceDetailRequestDTO line = request.getDetails().get(0);
        assertThat(line.getSaleOrderMasterRefId()).isEqualTo(ORDER_ID);
        assertThat(line.getTaxRefId()).isEqualTo(3);
        assertThat(line.getRemarks()).isEqualTo("PKG - KLANG");
        // Unknown item: names stay empty rather than failing the whole push.
        assertThat(line.getProductCode()).isEmpty();
        assertThat(line.getUom()).isEmpty();
    }

    @Test
    void anAlreadyInvoicedJobIsShownNotRecreated() {
        SaleOrderInvoiceLink existing = new SaleOrderInvoiceLink(ORDER_ID, "TR002601394", "NX TRANSPORT",
                true, 43933, "INV000043933", "27/08/2026", "", "");
        when(links.find(ORDER_ID, COMPANY)).thenReturn(Optional.of(existing));

        SaleOrderInvoicePreview preview = service.preview(ORDER_ID, COMPANY);
        assertThat(preview.canCreate()).isFalse();
        assertThat(preview.existing()).isEqualTo(existing);
        assertThat(preview.blockers()).containsExactly("Invoice INV000043933 already bills this job");

        assertThatThrownBy(() -> service.create(ORDER_ID, COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("INV000043933");
        verify(invoices, never()).save(any());
        verify(saleOrderService, never()).updateStatus(anyInt(), anyInt(), anyInt());
    }

    @Test
    void anOrderWithoutLinesCannotBeInvoiced() {
        when(details.findBySaleOrderMasterRefId(ORDER_ID)).thenReturn(List.of());

        SaleOrderInvoicePreview preview = service.preview(ORDER_ID, COMPANY);

        assertThat(preview.canCreate()).isFalse();
        assertThat(preview.blockers()).containsExactly("The sale order has no item lines to invoice");
    }

    @Test
    void createSavesTheInvoiceThenCompletesTheJob() {
        when(invoices.save(any())).thenReturn(SaleInvoiceSaveResult.builder()
                .id(43999).billNo("INV000043999").created(true).build());
        when(saleOrderService.updateStatus(ORDER_ID, COMPANY, COMPLETED_ID)).thenReturn(
                SaleOrderStatusUpdateDto.builder().id(ORDER_ID).jStatus(COMPLETED_ID).statusName("JOB COMPLET").build());

        SaleOrderInvoiceCreated created = service.create(ORDER_ID, COMPANY);

        ArgumentCaptor<SaleInvoiceRequestDTO> captor = ArgumentCaptor.forClass(SaleInvoiceRequestDTO.class);
        verify(invoices).save(captor.capture());
        assertThat(captor.getValue().getSaleOrderRefIds()).containsExactly(ORDER_ID);
        assertThat(created.invoiceId()).isEqualTo(43999);
        assertThat(created.invoiceNo()).isEqualTo("INV000043999");
        assertThat(created.netTotal()).isEqualTo(1060.0);
        assertThat(created.statusUpdated()).isTrue();
        assertThat(created.statusId()).isEqualTo(COMPLETED_ID);
        assertThat(created.statusName()).isEqualTo("JOB COMPLET");
        assertThat(created.statusMessage()).isEqualTo("Job status changed to JOB COMPLET");
    }

    @Test
    void aRefusedStatusChangeKeepsTheInvoiceAndSaysWhy() {
        when(invoices.save(any())).thenReturn(SaleInvoiceSaveResult.builder()
                .id(43999).billNo("INV000043999").created(true).build());
        when(saleOrderService.updateStatus(ORDER_ID, COMPANY, COMPLETED_ID))
                .thenThrow(new InvalidRequestException("Please complete all purchase orders before changing status!"));

        SaleOrderInvoiceCreated created = service.create(ORDER_ID, COMPANY);

        assertThat(created.invoiceNo()).isEqualTo("INV000043999");
        assertThat(created.statusUpdated()).isFalse();
        assertThat(created.statusId()).isEqualTo(6);
        assertThat(created.statusMessage())
                .startsWith("Invoice saved, but the job status was not changed: Please complete all purchase orders");
    }

    @Test
    void aJobAlreadyCompletedIsNotUpdatedAgain() {
        order.setJStatus(COMPLETED_ID);
        when(invoices.save(any())).thenReturn(SaleInvoiceSaveResult.builder()
                .id(43999).billNo("INV000043999").created(true).build());

        SaleOrderInvoiceCreated created = service.create(ORDER_ID, COMPANY);

        assertThat(created.statusUpdated()).isTrue();
        assertThat(created.statusMessage()).isEqualTo("The job was already JOB COMPLET");
        verify(saleOrderService, never()).updateStatus(anyInt(), anyInt(), anyInt());
    }

    @Test
    void aCompanyWithoutTheCompletedStatusStillGetsItsInvoice() {
        when(statuses.findSelectableByCompanyId(eq(COMPANY), anyInt())).thenReturn(List.of());
        when(invoices.save(any())).thenReturn(SaleInvoiceSaveResult.builder()
                .id(43999).billNo("INV000043999").created(true).build());

        SaleOrderInvoicePreview preview = service.preview(ORDER_ID, COMPANY);
        assertThat(preview.canCreate()).isTrue();
        assertThat(preview.warnings()).anyMatch(w -> w.contains("JOB COMPLET"));

        SaleOrderInvoiceCreated created = service.create(ORDER_ID, COMPANY);
        assertThat(created.invoiceNo()).isEqualTo("INV000043999");
        assertThat(created.statusUpdated()).isFalse();
        verify(saleOrderService, never()).updateStatus(anyInt(), anyInt(), anyInt());
    }

    @Test
    void billTypeIsTheStoredSeriesElseTheJobPrefix() {
        order.setBillType("MY ");
        assertThat(SaleOrderInvoiceCreationService.billTypeOf(order)).isEqualTo("MY");
        order.setBillType(null);
        assertThat(SaleOrderInvoiceCreationService.billTypeOf(order)).isEqualTo("TR");
        order.setCNumberDisplay("123");
        assertThat(SaleOrderInvoiceCreationService.billTypeOf(order)).isEqualTo("MY");
    }

    @Test
    void anUnknownOrderIsRefusedBeforeAnythingIsRead() {
        when(saleOrders.findByIdAndCompanyRefId(999, COMPANY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.preview(999, COMPANY)).hasMessageContaining("999");
        assertThatThrownBy(() -> service.preview(0, COMPANY)).isInstanceOf(InvalidRequestException.class);
    }
}

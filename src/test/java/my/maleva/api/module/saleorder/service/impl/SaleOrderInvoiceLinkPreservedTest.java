package my.maleva.api.module.saleorder.service.impl;

import my.maleva.api.module.boardingsettlement.service.BoardingEventSyncService;
import my.maleva.api.module.saleorder.dto.SaleOrderDTO;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.mapper.SaleOrderMasterMapperImpl;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.saleorder.service.SaleOrderNumberAllocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SaleOrderMaster.InvoiceNo belongs to the invoice save. The sale order screen
 * never loads it and used to send 0, so updating an invoiced job erased its
 * invoice link. These run the real mapper through the real update and create.
 */
@ExtendWith(MockitoExtension.class)
class SaleOrderInvoiceLinkPreservedTest {

    private static final int ORDER_ID = 21507;
    private static final int INVOICE_ID = 44358;

    @Mock
    private SaleOrderMasterRepository repository;

    @Mock
    private BoardingEventSyncService boardingEventSyncService;

    @Mock
    private SaleOrderNumberAllocator numberAllocator;

    @Spy
    private SaleOrderMasterMapperImpl mapper = new SaleOrderMasterMapperImpl();

    @InjectMocks
    private SaleOrderMasterServiceImpl service;

    private SaleOrderMaster savedRow() {
        ArgumentCaptor<SaleOrderMaster> row = ArgumentCaptor.forClass(SaleOrderMaster.class);
        verify(repository).saveAndFlush(row.capture());
        return row.getValue();
    }

    @Test
    void anUpdateSendingInvoiceNoZeroKeepsTheStoredInvoiceLink() {
        SaleOrderMaster stored = new SaleOrderMaster();
        stored.setId(ORDER_ID);
        stored.setCompanyRefId(6);
        stored.setCustomerRefId(49);
        stored.setBillType("MY");
        stored.setCNumber(2605171);
        stored.setCNumberDisplay("MY002605171");
        stored.setInvoiceNo(INVOICE_ID);
        when(repository.findByIdAndActive(eq(ORDER_ID), eq(1))).thenReturn(Optional.of(stored));
        when(repository.saveAndFlush(any(SaleOrderMaster.class))).thenAnswer(call -> call.getArgument(0));

        SaleOrderDTO edit = new SaleOrderDTO();
        edit.setId(ORDER_ID);
        edit.setCompanyRefId(6);
        edit.setCustomerRefId(49);
        edit.setCNumber(2605171);
        edit.setCNumberDisplay("MY002605171");
        edit.setInvoiceNo(0); // what the edit screen used to send for every order

        service.update(ORDER_ID, edit);

        assertThat(savedRow().getInvoiceNo()).isEqualTo(INVOICE_ID);
    }

    @Test
    void anUpdateCannotAttachAnInvoiceEither() {
        SaleOrderMaster stored = new SaleOrderMaster();
        stored.setId(ORDER_ID);
        stored.setCompanyRefId(6);
        stored.setCustomerRefId(49);
        stored.setBillType("MY");
        stored.setCNumber(2605171);
        stored.setCNumberDisplay("MY002605171");
        stored.setInvoiceNo(0);
        when(repository.findByIdAndActive(eq(ORDER_ID), eq(1))).thenReturn(Optional.of(stored));
        when(repository.saveAndFlush(any(SaleOrderMaster.class))).thenAnswer(call -> call.getArgument(0));

        SaleOrderDTO edit = new SaleOrderDTO();
        edit.setId(ORDER_ID);
        edit.setCompanyRefId(6);
        edit.setCustomerRefId(49);
        edit.setCNumber(2605171);
        edit.setInvoiceNo(INVOICE_ID);

        service.update(ORDER_ID, edit);

        assertThat(savedRow().getInvoiceNo()).isZero();
    }

    @Test
    void aNewOrderIsAlwaysSavedAsNotInvoiced() {
        when(numberAllocator.next(any(), anyString(), anyString())).thenReturn(2605172);
        when(repository.saveAndFlush(any(SaleOrderMaster.class))).thenAnswer(call -> call.getArgument(0));

        SaleOrderDTO create = new SaleOrderDTO();
        create.setId(0);
        create.setCompanyRefId(6);
        create.setCustomerRefId(49);
        create.setInvoiceNo(INVOICE_ID); // a create must not be able to claim an invoice

        service.save(create);

        SaleOrderMaster row = savedRow();
        assertThat(row.getInvoiceNo()).isZero();
        assertThat(row.getCNumber()).isEqualTo(2605172);
    }
}

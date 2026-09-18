package my.maleva.api.module.paymentrecept.service.impl;

import my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import my.maleva.api.module.paymentrecept.service.ReceiptCreateGuard;
import my.maleva.api.module.paymentrecept.service.ReceiptNumberAllocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * A new receipt saved, the page gave up waiting, SAVE was pressed again: the
 * second request must hand back the first receipt, not number a second one.
 */
@ExtendWith(MockitoExtension.class)
class ReceiptCreateReplayTest {

    private static final String KEY = "rc-create-5b1e";

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ReceiptDetailsRepository receiptDetailsRepository;

    @Mock
    private ReceiptNumberAllocator numberAllocator;

    @Spy
    private ReceiptCreateGuard createGuard = new ReceiptCreateGuard();

    @InjectMocks
    private ReceiptServiceImpl service;

    private static ReceiptSaveRequest newReceipt(String key) {
        return ReceiptSaveRequest.builder()
                .id(0)
                .companyRefId(6)
                .customerRefId(49)
                .receiptDate("2026-09-17")
                .amount(new BigDecimal("1500.00"))
                .currencyValue(1.0)
                .clientRequestId(key)
                .build();
    }

    private void repositoriesCreateReceipt(int id) {
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(inv -> {
            Receipt r = inv.getArgument(0);
            r.setId(id);
            return r;
        });
        when(numberAllocator.next(anyInt())).thenReturn(121);
    }

    @Test
    void aRepeatedCreateReturnsTheReceiptAlreadyMadeAndWritesNothing() {
        ReceiptSaveResponseDto first = ReceiptSaveResponseDto.builder()
                .ok(true).isSuccess(true).id(9001).name("RC000000121").build();
        createGuard.begin(KEY);
        createGuard.complete(KEY, first);

        assertThat(service.insertReceipt(List.of(newReceipt(KEY)), 6)).isSameAs(first);
        verifyNoInteractions(receiptRepository, receiptDetailsRepository, numberAllocator);
    }

    @Test
    void aRepeatWhileTheFirstIsStillSavingIsRefusedAndWritesNothing() {
        createGuard.begin(KEY);

        ReceiptSaveResponseDto answer = service.insertReceipt(List.of(newReceipt(KEY)), 6);

        assertThat(answer.getOk()).isFalse();
        assertThat(answer.getMessage()).isEqualTo(ReceiptCreateGuard.STILL_SAVING_MESSAGE);
        verifyNoInteractions(receiptRepository, receiptDetailsRepository, numberAllocator);
    }

    @Test
    void theSameKeyTwiceCreatesOneReceipt() {
        repositoriesCreateReceipt(9002);

        ReceiptSaveResponseDto first = service.insertReceipt(List.of(newReceipt(KEY)), 6);
        ReceiptSaveResponseDto again = service.insertReceipt(List.of(newReceipt(KEY)), 6);

        assertThat(first.getOk()).isTrue();
        assertThat(first.getName()).isEqualTo("RC000000121");
        assertThat(again).isSameAs(first);
        // one insert + one numbering save, not four
        verify(receiptRepository, times(2)).save(any(Receipt.class));
    }

    @Test
    void aFailedCreateReleasesTheKeySoSaveCanReallyRunAgain() {
        when(receiptRepository.save(any(Receipt.class))).thenThrow(new IllegalStateException("deadlock victim"));

        try {
            service.insertReceipt(List.of(newReceipt(KEY)), 6);
        } catch (IllegalStateException expected) {
            // the controller turns this into a 500
        }

        assertThat(createGuard.begin(KEY)).isInstanceOf(ReceiptCreateGuard.Owned.class);
    }

    @Test
    void differentKeysAreDifferentReceipts() {
        repositoriesCreateReceipt(9003);

        service.insertReceipt(List.of(newReceipt("key-a")), 6);
        service.insertReceipt(List.of(newReceipt("key-b")), 6);

        verify(receiptRepository, times(4)).save(any(Receipt.class));
    }

    @Test
    void anUpdateIgnoresTheKey() {
        ReceiptSaveRequest update = newReceipt(KEY);
        update.setId(9001);
        when(receiptRepository.findById(9001)).thenReturn(Optional.empty());

        service.insertReceipt(List.of(update), 6);

        verifyNoInteractions(createGuard);
    }
}

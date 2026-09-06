package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.module.user.repository.AppUserRepository;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.paymentrecept.dto.ReceiptBillDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import my.maleva.api.module.paymentrecept.mapper.ReceiptMapper;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import my.maleva.api.module.paymentrecept.service.impl.ReceiptServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptInsertTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ReceiptDetailsRepository receiptDetailsRepository;

    @Mock
    private ReceiptMapper receiptMapper;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private BankMasterRepository bankMasterRepository;

    @Mock
    private EmployeeMasterRepository employeeMasterRepository;

    @Mock
    private SequenceNoMasterRepository sequenceNoMasterRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReceiptServiceImpl receiptService;

    private Integer companyId = 6;

    @Test
    void insertNewReceipt_Success() {
        ReceiptSaveRequest request = ReceiptSaveRequest.builder()
                .id(0)
                .companyRefId(companyId)
                .userRefId(1)
                .employeeRefId(10)
                .customerRefId(100)
                .bankRefId(5)
                .receiptDate("2026-09-05")
                .amount(new BigDecimal("500.00"))
                .currencyValue(1.0)
                .actualNetAmount(500.0)
                .bankCharges(0.0)
                .remarks("Advance payment")
                .refNumber("REF-001")
                .receiptDetails(List.of(
                        ReceiptBillDto.builder()
                                .saleMasterRefId(2001)
                                .amount(new BigDecimal("500.00"))
                                .currencyValue(new BigDecimal("1.0"))
                                .actualAmount(new BigDecimal("500.00"))
                                .build()
                ))
                .build();

        when(appUserRepository.existsByIdAndCompanyRefIdAndActive(1, companyId, 1)).thenReturn(true);
        when(bankMasterRepository.existsByIdAndCompanyRefIdAndActive(5, companyId, 1)).thenReturn(true);
        when(employeeMasterRepository.existsByIdAndCompanyRefIdAndActive(10, companyId, 1)).thenReturn(true);

        when(sequenceNoMasterRepository.findMaxSequenceNoByCompanyAndSequenceName(companyId, "Receipt"))
                .thenReturn(42);
        when(sequenceNoMasterRepository.findByCompanyRefIdAndSequenceName(companyId, "Receipt"))
                .thenReturn(Optional.of(new SequenceNoMaster()));

        Receipt savedReceipt = new Receipt();
        savedReceipt.setId(88);
        when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);

        ReceiptSaveResponseDto response = receiptService.insertReceipt(List.of(request), companyId);

        assertThat(response).isNotNull();
        assertThat(response.getOk()).isTrue();
        assertThat(response.getIsSuccess()).isTrue();
        assertThat(response.getId()).isEqualTo(88);
        assertThat(response.getName()).isEqualTo("RC000000043");
        assertThat(response.getMessage()).isEqualTo("Receipt Created Successfully");
        assertThat(response.getData1()).isEqualTo("RC000000043");
        assertThat(response.getData2()).isEqualTo(88);

        ArgumentCaptor<List<ReceiptDetails>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(receiptDetailsRepository).saveAll(detailsCaptor.capture());
        List<ReceiptDetails> savedDetails = detailsCaptor.getValue();
        assertThat(savedDetails).hasSize(1);
        assertThat(savedDetails.get(0).getReceiptRefId()).isEqualTo(88);
        assertThat(savedDetails.get(0).getSaleMasterRefId()).isEqualTo(2001);
        assertThat(savedDetails.get(0).getReceiptAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void updateExistingReceipt_Success() {
        Receipt existingReceipt = new Receipt();
        existingReceipt.setId(55);
        existingReceipt.setCNumber(12);
        existingReceipt.setCNumberDisplay("RC000000012");
        existingReceipt.setAmount(new BigDecimal("300.00"));

        ReceiptSaveRequest request = ReceiptSaveRequest.builder()
                .id(55)
                .companyRefId(companyId)
                .userRefId(1)
                .employeeRefId(10)
                .customerRefId(100)
                .bankRefId(5)
                .receiptDate("2026-09-05")
                .amount(new BigDecimal("350.00"))
                .receiptDetails(List.of(
                        ReceiptBillDto.builder()
                                .saleMasterRefId(2002)
                                .amount(new BigDecimal("350.00"))
                                .build()
                ))
                .build();

        when(appUserRepository.existsByIdAndCompanyRefIdAndActive(1, companyId, 1)).thenReturn(true);
        when(bankMasterRepository.existsByIdAndCompanyRefIdAndActive(5, companyId, 1)).thenReturn(true);
        when(employeeMasterRepository.existsByIdAndCompanyRefIdAndActive(10, companyId, 1)).thenReturn(true);
        when(receiptRepository.findById(55)).thenReturn(Optional.of(existingReceipt));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(existingReceipt);

        ReceiptSaveResponseDto response = receiptService.insertReceipt(List.of(request), companyId);

        assertThat(response.getOk()).isTrue();
        assertThat(response.getIsSuccess()).isTrue();
        assertThat(response.getId()).isEqualTo(55);
        assertThat(response.getName()).isEqualTo("RC000000012");
        assertThat(response.getMessage()).isEqualTo("Receipt Updated Successfully");

        verify(receiptDetailsRepository).deleteByReceiptRefId(55);
        verify(receiptDetailsRepository).saveAll(anyList());
    }

    @Test
    void insertReceipt_FailsOnInvalidBank() {
        ReceiptSaveRequest request = ReceiptSaveRequest.builder()
                .id(0)
                .companyRefId(companyId)
                .userRefId(1)
                .bankRefId(999)
                .build();

        when(appUserRepository.existsByIdAndCompanyRefIdAndActive(1, companyId, 1)).thenReturn(true);
        when(bankMasterRepository.existsByIdAndCompanyRefIdAndActive(999, companyId, 1)).thenReturn(false);

        ReceiptSaveResponseDto response = receiptService.insertReceipt(List.of(request), companyId);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getIsSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Bank Not Found Issue");
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void insertReceipt_FailsOnEmptyPayload() {
        ReceiptSaveResponseDto response = receiptService.insertReceipt(Collections.emptyList(), companyId);

        assertThat(response.getOk()).isFalse();
        assertThat(response.getMessage()).contains("Empty receipt data");
    }
}

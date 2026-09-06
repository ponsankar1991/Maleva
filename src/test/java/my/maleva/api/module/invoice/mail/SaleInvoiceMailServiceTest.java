package my.maleva.api.module.invoice.mail;

import my.maleva.api.common.config.MailProperties;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.print.SaleInvoicePdfService;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaleInvoiceMailServiceTest {

    private final SaleMasterRepository saleMasters = mock(SaleMasterRepository.class);
    private final SaleInvoicePdfService pdf = mock(SaleInvoicePdfService.class);
    private final AttachmentStorageService attachments = mock(AttachmentStorageService.class);
    private final EmailService email = mock(EmailService.class);
    private final MailProperties mail = new MailProperties();
    private final SaleInvoiceMailService service =
            new SaleInvoiceMailService(saleMasters, pdf, attachments, email, mail);

    @BeforeEach
    void setUp() {
        mail.setInvoiceRecipients(List.of("ops@example.com", " accounts@example.com "));
        when(email.isConfigured()).thenReturn(true);

        SaleMaster invoice = new SaleMaster();
        invoice.setId(4711);
        invoice.setCompanyRefId(1);
        invoice.setCNumberDisplay("INV000004711");
        when(saleMasters.findById(4711)).thenReturn(Optional.of(invoice));
        when(pdf.render(4711, 1)).thenReturn(Optional.of(
                new SaleInvoicePdfService.RenderedInvoice("INV000004711.pdf", "%PDF-".getBytes())));
        when(attachments.list(any(AttachmentScope.class))).thenReturn(List.of());
    }

    @Test
    void sendsThePdfAndEveryStoredAttachmentToTheConfiguredList() {
        when(attachments.list(any(AttachmentScope.class))).thenReturn(List.of(
                AttachmentDto.builder().fileName("pod.jpg").path("/uploads/1/SaleInvoice/4711/pod.jpg").contentType("image/jpeg").build(),
                AttachmentDto.builder().fileName("gone.pdf").path("/uploads/1/SaleInvoice/4711/gone.pdf").build()));
        when(attachments.read("/uploads/1/SaleInvoice/4711/pod.jpg")).thenReturn(Optional.of(new byte[]{1, 2}));
        when(attachments.read("/uploads/1/SaleInvoice/4711/gone.pdf")).thenReturn(Optional.empty());

        SaleInvoiceMailService.MailOutcome outcome = service.send(4711, 1, "Shalini");

        assertThat(outcome.ok()).isTrue();
        assertThat(outcome.recipients()).containsExactly("ops@example.com", "accounts@example.com");
        assertThat(outcome.attachmentCount()).isEqualTo(2);
        assertThat(outcome.message()).contains("INV000004711").contains("gone.pdf");

        ArgumentCaptor<List<EmailAttachment>> files = ArgumentCaptor.forClass(List.class);
        verify(email).sendInvoiceMail(eq(List.of("ops@example.com", "accounts@example.com")),
                eq("INV000004711"), eq("Sir/Mam"), eq("Shalini"), files.capture());
        assertThat(files.getValue()).extracting(EmailAttachment::fileName)
                .containsExactly("INV000004711.pdf", "pod.jpg");
        ArgumentCaptor<AttachmentScope> scope = ArgumentCaptor.forClass(AttachmentScope.class);
        verify(attachments).list(scope.capture());
        assertThat(scope.getValue().getFolderName()).isEqualTo("SaleInvoice");
        assertThat(scope.getValue().getRecordId()).isEqualTo(4711);
    }

    @Test
    void refusesClearlyWhenNothingCanGoOut() {
        mail.setInvoiceRecipients(List.of());
        assertThat(service.send(4711, 1, null).message()).contains("mail.invoice-recipients");

        mail.setInvoiceRecipients(List.of("ops@example.com"));
        when(email.isConfigured()).thenReturn(false);
        assertThat(service.send(4711, 1, null).message()).contains("mail.smtp.host");

        when(email.isConfigured()).thenReturn(true);
        assertThat(service.send(4711, 2, null).message()).contains("not found for this company");
        verify(email, never()).sendInvoiceMail(anyList(), anyString(), anyString(), anyString(), anyList());
    }

    @Test
    void aRefusedMessageIsReportedNotSwallowed() {
        doThrow(new IllegalStateException("535 Authentication failed"))
                .when(email).sendInvoiceMail(anyList(), anyString(), anyString(), anyString(), anyList());

        SaleInvoiceMailService.MailOutcome outcome = service.send(4711, 1, "");

        assertThat(outcome.ok()).isFalse();
        assertThat(outcome.message()).contains("INV000004711").contains("535 Authentication failed");
        verify(pdf).render(anyInt(), anyInt());
    }
}

package my.maleva.api.module.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneAfterCommit;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneCustomerRequest;
import my.maleva.api.integration.qne.dto.QneCustomerResponse;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * QNE sync for customers — the Java port of the QNE side of legacy
 * {@code CustomerServices} (InsertCustomer's push, UpdateCustomerId1's
 * backfill, and the CustomerStatement report URL).
 *
 * <p>Customer is the one entity whose QNE identity lives in
 * {@code UpdateId}/{@code CompanyCode} instead of QNEId/QNECode.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;

    /**
     * Pushes a newly created customer once its insert commits — the legacy
     * SP committed the row before calling QNE, so a QNE failure leaves a
     * committed row for {@link #backfill} to repair, never the reverse.
     */
    public void pushCreatedAfterCommit(Customer saved) {
        QneAfterCommit.run(() -> {
            try {
                QnePushResult result = pushCreated(saved);
                if (!result.success()) {
                    log.warn("QNE push for new customer {} did not complete: {}",
                            saved.getId(), result.message());
                }
            } catch (RuntimeException ex) {
                // Nothing thrown here may escape. Spring propagates an
                // afterCommit failure to the caller, so a stumble in the
                // currency lookup or the id write-back would answer a save
                // that has already committed with a 500 — and the operator
                // would enter the customer a second time. QNE transport
                // failures are already returned rather than thrown; this
                // catches everything around them.
                log.error("QNE push for new customer {} threw after the row was committed",
                        saved.getId(), ex);
            }
        });
    }

    /**
     * Pushes a customer that QNE does not have yet. Called from both save
     * paths: legacy's guard was {@code Id == 0 || CompanyCode is blank}, so an
     * edit of a never-pushed customer created it too.
     *
     * <p>One deliberate divergence: legacy's leading {@code Id == 0} meant a
     * NEW customer was pushed even when someone had pasted a QNE code into the
     * form, creating a second QNE company for one customer. A blank code is the
     * only condition here, which is also what {@code claimQneIdentity} relies
     * on to make the write-back a one-time claim.
     */
    public QnePushResult pushCreated(Customer customer) {
        if (!QnePayloads.isBlank(customer.getCompanyCode())) {
            return QnePushResult.alreadyPushed(customer.getUpdateId(), customer.getCompanyCode(),
                    "Customer already exists in QNE as " + customer.getCompanyCode());
        }

        QneCustomerRequest request = buildRequest(
                customer, currencyName(customer), properties.getControlCodes().getCustomer());
        QneCall<QneCustomerResponse> call = gateway.createCustomer(request);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        customers.claimQneIdentity(customer.getId(), call.data().getId(), call.data().getCompanyCode());
        return QnePushResult.ok(call.data().getId(), call.data().getCompanyCode(), null,
                "Customer pushed to QNE as " + call.data().getCompanyCode());
    }

    /**
     * Repairs customers whose QNE code is known but whose QNE GUID was never
     * stored (a failed write-back, or rows migrated from the legacy system).
     */
    public QnePushResult backfill(Integer companyRefId) {
        List<Customer> pending = customers.findQneBackfillCandidates(companyRefId);
        if (pending.isEmpty()) {
            return QnePushResult.ok(null, null, null, "No customers waiting for a QNE id");
        }

        int repaired = 0;
        List<String> codes = pending.stream().map(Customer::getCompanyCode).toList();
        for (List<String> chunk : QnePayloads.chunks(codes, 100)) {
            QneCall<List<QneCustomerResponse>> call = gateway.findCustomersByCompanyCodes(chunk);
            if (!call.success()) {
                return QnePushResult.rejected(call.message());
            }
            for (QneCustomerResponse match : call.data()) {
                if (!QnePayloads.isBlank(match.getCompanyCode())) {
                    repaired += customers.backfillQneId(companyRefId, match.getCompanyCode(), match.getId());
                }
            }
        }
        return QnePushResult.ok(null, null, null,
                "Backfilled QNE ids for " + repaired + " of " + pending.size() + " customers");
    }

    /**
     * QNE-hosted customer statement for one month — the only QNE report the
     * legacy system shipped with its gate on ({@code qnereportview=true}).
     */
    public QnePushResult statementUrl(Integer customerId, int year, int month) {
        if (!properties.isReportView()) {
            return QnePushResult.localError(409, "QNE report view is disabled (qne.report-view=false)");
        }
        Customer customer = customers.findById(customerId).orElse(null);
        if (customer == null) {
            return QnePushResult.localError(404, "Customer not found: " + customerId);
        }
        if (QnePayloads.isBlank(customer.getUpdateId())) {
            return QnePushResult.localError(409,
                    "Customer has no QNE id yet — push or backfill the customer first");
        }
        QneCall<String> call = gateway.customerStatementUrl(customer.getUpdateId(), year, month);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }
        return QnePushResult.ok(customer.getUpdateId(), customer.getCompanyCode(), call.data(),
                "QNE customer statement URL fetched");
    }

    /**
     * The currency QNE is told about, from the customer's symbol.
     *
     * <p>Scoped to the company, as legacy's
     * {@code WHERE S.CompanyRefId = @Comid AND S.Id = @SymbolRefid} was: the id
     * alone would happily read another tenant's symbol. Absent or unmatched
     * gives "", which is what the legacy ExecuteScalar's null coalesce produced.
     */
    private String currencyName(Customer customer) {
        if (customer.getSymbolRefid() == null || customer.getSymbolRefid() == 0) {
            return "";
        }
        return symbols.findByIdAndCompanyRefId(customer.getSymbolRefid(), customer.getCompanyRefId())
                .map(SymbolMaster::getSName)
                .orElse("");
    }

    /**
     * Field mapping pinned by legacy {@code CustomerServices.InsertCustomer}:
     * both company names carry the customer name, the City column is
     * repurposed as QNE's contact person, and Email/PhoneNo1 come from the
     * operations contact (OEmail/OPhone).
     */
    static QneCustomerRequest buildRequest(Customer customer, String currency, String controlAccount) {
        String[] address = QnePayloads.addressChunks(customer.getAddress1());
        return QneCustomerRequest.builder()
                .companyName(customer.getCustomerName())
                .companyName2(customer.getCustomerName())
                .controlAccount(controlAccount)
                .currency(currency)
                .address1(address[0])
                .address2(address[1])
                .address3(address[2])
                .address4(address[3])
                .contactPerson(customer.getCity())
                .email(customer.getOEmail())
                .phoneNo1(customer.getOPhone())
                .status("ACTIVE")
                .build();
    }
}

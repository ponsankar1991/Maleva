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
            QnePushResult result = pushCreated(saved);
            if (!result.success()) {
                log.warn("QNE push for new customer {} did not complete: {}",
                        saved.getId(), result.message());
            }
        });
    }

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

    private String currencyName(Customer customer) {
        if (customer.getSymbolRefid() == null) {
            return "";
        }
        return symbols.findById(customer.getSymbolRefid())
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

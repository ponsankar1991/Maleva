package my.maleva.api.integration.qne;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import my.maleva.api.integration.qne.dto.QneBillLine;
import my.maleva.api.integration.qne.dto.QneBillRequest;
import my.maleva.api.integration.qne.dto.QneCustomerRequest;
import my.maleva.api.integration.qne.dto.QneKnockoffItem;
import my.maleva.api.integration.qne.dto.QneKnockoffRequest;
import my.maleva.api.integration.qne.dto.QnePayBillRequest;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherLine;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherRequest;
import my.maleva.api.integration.qne.dto.QneReceiptRequest;
import my.maleva.api.integration.qne.dto.QneReportUrlResponse;
import my.maleva.api.integration.qne.dto.QneSalesCnLine;
import my.maleva.api.integration.qne.dto.QneSalesCnRequest;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceLine;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceRequest;
import my.maleva.api.integration.qne.dto.QneStockRequest;
import my.maleva.api.integration.qne.dto.QneSupplierRequest;

/**
 * Pins the exact JSON field names each request DTO puts on the wire.
 *
 * <p>QNE's contract is PascalCase and casing-sensitive, and two silent
 * renamers sit between a Java field and the wire: Lombok strips the
 * {@code is} prefix from boolean getters (so {@code IsTaxInclusive} degrades
 * to {@code TaxInclusive}), and the naming strategy only maps what survives.
 * Every expected set below is transcribed from the legacy .NET model — a
 * failure here means QNE would receive a renamed or missing field.
 */
class QneWireContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private Set<String> keysOf(Object dto) throws Exception {
        JsonNode node = mapper.readTree(mapper.writeValueAsString(dto));
        Set<String> keys = new LinkedHashSet<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            keys.add(it.next());
        }
        return keys;
    }

    @Test
    void customerRequestMatchesLegacyCustomerQNEInsertModel() throws Exception {
        assertThat(keysOf(QneCustomerRequest.builder().build())).containsExactlyInAnyOrder(
                "CompanyCode", "CompanyName", "CompanyName2", "ControlAccount", "RegistrationNo",
                "GstRegNo", "Category", "Address1", "Address2", "Address3", "Address4",
                "ContactPerson", "Email", "PhoneNo1", "PhoneNo2", "FaxNo1", "FaxNo2",
                "BusinessNature", "Homepage", "Area", "Term", "SalesPerson", "Currency",
                "DefaultTaxCode", "SourceOfLead", "Status");
    }

    @Test
    void supplierRequestMatchesLegacySupplierQNEInsertModel() throws Exception {
        assertThat(keysOf(QneSupplierRequest.builder().build())).containsExactlyInAnyOrder(
                "CompanyCode", "CompanyName", "CompanyName2", "ControlAccount", "RegistrationNo",
                "Address1", "Address2", "Address3", "Address4", "ContactPerson", "PhoneNo1",
                "PhoneNo2", "FaxNo1", "FaxNo2", "Email", "Homepage", "BusinessNature",
                "IsProspect", "IsSuspended", "Category", "DeliveryAddress1", "DeliveryAddress2",
                "DeliveryAddress3", "DeliveryAddress4", "Area", "Purchaser", "Currency", "Term",
                "IsExceedCreditAllowed", "IsTaxExempted", "BillingContactPerson",
                "BillingContactPhoneNo", "BillingContactEmail", "AccountContactPerson",
                "AccountContactPhoneNo", "AccountContactEmail", "ManagementContactPerson",
                "ManagementContactPhoneNo", "ManagementContactEmail", "GuarantorName",
                "GuarantorIdNo", "GuarantorGender", "GuarantorRace", "GuarantorCitizenship",
                "GstExemptionNo", "GstExemptionExpiryDate", "StartDate", "GstRegNo",
                "DefaultTaxCode", "GstStatusVerifiedDate", "DefaultWTaxCode", "BranchCode");
    }

    @Test
    void stockRequestMatchesLegacyProductInsetQNE() throws Exception {
        // "Volumn" and "Class" are legacy wire spellings, deliberately kept.
        assertThat(keysOf(QneStockRequest.builder().build())).containsExactlyInAnyOrder(
                "AutoCode", "StockCode", "StockName", "StockName2", "Description", "BaseUom",
                "MinQty", "MaxQty", "ReorderLevel", "ReorderQty", "ListPrice", "MinPrice",
                "SalesDiscount", "PurchasePrice", "PurchaseDiscount", "BarCode", "Weight",
                "Volumn", "IsBundled", "StockControl", "UseSerialNo", "SerialNoPrefix",
                "SerialNoSuffix", "Remark1", "Remark2", "Remark3", "Remark4", "Remark5",
                "UseBatchNo", "AccountPreset", "Category", "Group", "Class",
                "DefaultInputTaxCode", "DefaultOutputTaxCode");
    }

    @Test
    void salesInvoiceRequestMatchesLegacyInsertModel() throws Exception {
        assertThat(keysOf(QneSalesInvoiceRequest.builder().build())).containsExactlyInAnyOrder(
                "Id", "Customer", "InvoiceDate", "InvoiceCode", "InvoiceTo", "DeliveryTerm",
                "Term", "StockLocation", "Attention", "Phone", "Fax", "Address1", "Address2",
                "Address3", "Address4", "ReferenceNo", "Notes", "SalesPerson", "OurDono",
                "Title", "Title2", "Ref1", "Ref2", "Ref3", "Ref4", "Ref5", "Remark1", "Remark2",
                "Remark3", "Remark4", "Remark5", "Project", "CostCentre", "CurrencyRate",
                "IsTaxInclusive", "IsRounding", "DoBranchCode", "DoBranchName", "DoContact",
                "DoPhone", "DoAddress1", "DoAddress2", "DoAddress3", "DoAddress4", "Details");
    }

    @Test
    void salesInvoiceLineMatchesLegacyDetailModelAndSendsEmptyTransferFrom() throws Exception {
        QneSalesInvoiceLine line = QneSalesInvoiceLine.builder().build();
        assertThat(keysOf(line)).containsExactlyInAnyOrder(
                "Numbering", "Stock", "Description", "Note", "Uom", "Qty", "TaxCode",
                "IsTaxInclusive", "UnitPrice", "Discount", "ReferenceNo", "GlAccount",
                "Project", "CostCentre", "WTaxCode", "Ref", "Ref2", "Ref3", "Ref4", "Ref5",
                "DateRef1", "DateRef2", "NumRef1", "NumRef2", "StockLocation", "TransferFrom");

        // Legacy sent TransferFrom as a property-less {} on every line; QNE
        // requires the key to exist.
        JsonNode node = mapper.readTree(mapper.writeValueAsString(line));
        assertThat(node.get("TransferFrom").isObject()).isTrue();
        assertThat(node.get("TransferFrom").size()).isZero();
    }

    @Test
    void salesCnRequestMatchesLegacyInsertModel() throws Exception {
        // "DoRegistationNo" is the legacy wire misspelling, deliberately kept.
        assertThat(keysOf(QneSalesCnRequest.builder().build())).containsExactlyInAnyOrder(
                "Customer", "CnDate", "CnCode", "CustomerName", "DeliveryTerm", "Term",
                "StockLocation", "Attention", "SalesPerson", "OurDono", "Project", "CostCentre",
                "CurrencyRate", "ReferenceNo", "IsRounding", "Phone", "Fax", "Address1",
                "Address2", "Address3", "Address4", "TermId", "SalesPersonId", "Title", "Title2",
                "Ref1", "Ref2", "Ref3", "Ref4", "Ref5", "Remark1", "Remark2", "Remark3",
                "Remark4", "Remark5", "IsCancelled", "DoBranchCode", "DoBranchName", "DoContact",
                "DoPhone", "DoFax", "DoAddress1", "DoAddress2", "DoAddress3", "DoAddress4",
                "Discount", "Notes", "IsTaxInclusive", "TaxDate", "DoRegistationNo",
                "DoGstRegNo", "DoPhone2", "DoEmail", "DoRemark", "DeliveryArea", "IsApproved",
                "Details");
    }

    @Test
    void salesCnLineMatchesLegacyDetailModel() throws Exception {
        assertThat(keysOf(QneSalesCnLine.builder().build())).containsExactlyInAnyOrder(
                "Numbering", "Stock", "Description", "Note", "Uom", "Qty", "TaxCode",
                "IsTaxInclusive", "UnitPrice", "Discount", "ReferenceNo", "WTaxCode",
                "StockLocation", "Project", "CostCentre", "Ref", "Ref2", "Ref3", "Ref4", "Ref5",
                "DateRef1", "DateRef2", "NumRef1", "NumRef2", "Pos");
    }

    @Test
    void receiptRequestMatchesLegacyReceiptInsertQne() throws Exception {
        assertThat(keysOf(QneReceiptRequest.builder().build())).containsExactlyInAnyOrder(
                "CustomerCode", "DocDate", "Amount", "DepositAccountCode", "SalesPersonCode",
                "CostCentreCode", "ProjectCode", "CurrencyRate", "DocCode", "Description",
                "ReferenceNo", "BankChargesAccountCode", "BankCharges");
    }

    @Test
    void knockoffModelsMatchLegacy() throws Exception {
        assertThat(keysOf(QneKnockoffRequest.builder().build()))
                .containsExactlyInAnyOrder("DocId", "KnockoffItems");
        assertThat(keysOf(QneKnockoffItem.builder().build())).containsExactlyInAnyOrder(
                "DocType", "DocCode", "Payment", "ForexPostingDate", "KnockoffRefId");
    }

    @Test
    void billModelsMatchLegacy() throws Exception {
        assertThat(keysOf(QneBillRequest.builder().build())).containsExactlyInAnyOrder(
                "BillCode", "BillDate", "BillFrom", "Supplier", "ReferenceNo", "Term", "DueDate",
                "Purchaser", "Project", "Currency", "CurrencyRate", "Description", "Description2",
                "Notes", "PostDate", "CostCentre", "IsTaxInclusive", "SupplierInvNo", "TaxDate",
                "RoundingAdjustmentAccount", "IsRounding", "Details");
        assertThat(keysOf(QneBillLine.builder().build())).containsExactlyInAnyOrder(
                "Account", "Description", "ReferenceNo", "Amount", "TaxCode", "IsTaxInclusive",
                "Project", "CostCentre");
    }

    @Test
    void payBillRequestMatchesLegacyPaymentQNEInsertModel() throws Exception {
        assertThat(keysOf(QnePayBillRequest.builder().build())).containsExactlyInAnyOrder(
                "PaymentCode", "PaymentDate", "PayByAccount", "ReferenceNo", "Project",
                "CurrencyRate", "Supplier", "PayTo", "Description", "TotalAmount", "IsCancelled",
                "IsPostDatedCheque", "ChequePreparedDate", "ChequeDate", "IsBouncedCheque",
                "BouncedChequeDate", "IsTaxInclusive", "Purchaser", "CostCentre", "TaxDate",
                "BankChargesAmount", "IsTaxInclusiveOnly", "RoundingAdjustmentAccount",
                "IsRounding", "PostGlDescription");
    }

    @Test
    void paymentVoucherModelsMatchLegacy() throws Exception {
        assertThat(keysOf(QnePaymentVoucherRequest.builder().build())).containsExactlyInAnyOrder(
                "PaymentCode", "PaymentDate", "PayByAccount", "ReferenceNo", "Project",
                "Currency", "CurrencyRate", "PayTo", "Description", "IsCancelled",
                "IsPostDatedCheque", "ChequePreparedDate", "ChequeDate", "IsBouncedCheque",
                "BouncedChequeDate", "IsTaxInclusive", "Purchaser", "CostCentre", "TaxDate",
                "BankChargesAmount", "IsTaxInclusiveOnly", "RoundingAdjustmentAccount",
                "IsRounding", "PostGlDescription", "Details");
        assertThat(keysOf(QnePaymentVoucherLine.builder().build())).containsExactlyInAnyOrder(
                "Account", "Description", "Amount", "Project", "ReferenceNo", "RegistrationTin",
                "Supplier", "Address", "CostCentre", "TaxCode", "IsTaxInclusive", "WTaxCode");
    }

    /** QNE's one lowercase field: the report endpoints answer {@code {"file": url}}. */
    @Test
    void reportUrlResponseReadsLowercaseFile() throws Exception {
        QneReportUrlResponse response =
                mapper.readValue("{\"file\":\"https://qne.example/doc.pdf\"}", QneReportUrlResponse.class);
        assertThat(response.getFile()).isEqualTo("https://qne.example/doc.pdf");
    }

    /** The legacy in-membership filter, byte-for-byte. */
    @Test
    void inFilterMatchesLegacyShape() {
        assertThat(QneGateway.inFilter("companyCode", java.util.List.of("700-B038", "700-B039")))
                .isEqualTo("?$filter=companyCode in ['700-B038','700-B039']");
        // Only & is escaped, exactly as the legacy did.
        assertThat(QneGateway.inFilter("companyCode", java.util.List.of("A&B")))
                .isEqualTo("?$filter=companyCode in ['A%26B']");
    }
}

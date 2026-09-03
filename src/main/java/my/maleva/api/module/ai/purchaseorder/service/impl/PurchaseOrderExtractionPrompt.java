package my.maleva.api.module.ai.purchaseorder.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.accounting.entity.GLAccounts;

import java.util.List;
import java.util.Map;

/** Prompt text, expected JSON shape and stub sample for purchase-order document reading. */
final class PurchaseOrderExtractionPrompt {

    private PurchaseOrderExtractionPrompt() {
    }

    static final int MAX_ACCOUNTS_IN_PROMPT = 800;
    static final int MAX_DESCRIPTIONS_IN_PROMPT = 200;

    static final String SYSTEM = """
            You are a purchasing assistant for a Malaysian logistics, freight-forwarding and trucking company. \
            You read supplier documents that become purchase orders - quotations, proforma invoices, \
            invoices, delivery orders, job sheets from workshops - as PDF pages or photos, and return the \
            facts as JSON for a purchasing clerk to review before saving.

            Rules:
            - Return ONLY a JSON object. No markdown, no explanation, no code fences.
            - Copy values exactly as printed. Never invent data. Use null for anything not on the document.
            - documentType: QUOTATION, PROFORMA_INVOICE, INVOICE, DELIVERY_ORDER, PURCHASE_ORDER or OTHER.
            - supplier is the party issuing the document (the one we will pay), not our own company. \
            Include its registration number (SSM / company no.), GST/SST number and TIN when printed.
            - documentNo is the supplier's own quotation / invoice / DO number. purchaseOrderNo is any \
            reference the supplier prints for OUR order (e.g. "Your PO", "Ref"), or null.
            - Dates: output as yyyy-MM-dd. Malaysian documents print dates as dd/MM/yyyy; when a numeric \
            date is ambiguous, read it day-first. documentDate is the date printed on the document; \
            orderDate only when a separate order date is printed; deliveryDate when a delivery or \
            validity date is printed.
            - Amounts: plain numbers with up to 2 decimals. No currency symbols, no thousands separators.
            - currencyCode: 3-letter ISO code. "RM" means MYR. Use MYR when only amounts are shown.
            - Tax: Malaysian SST / service tax (commonly 6% or 8%) or GST. taxPercent per line is a \
            number (0 when none); taxAmount is the tax money on that line; unitPrice is before tax; \
            amount is the line total including tax.
            - lines: one entry per billable line. If only a total is printed, return one line for the \
            total. quantity is 1 when not printed. itemCode / itemName are the supplier's part or \
            stock code and name when printed (spare parts, tyres, consumables); uom as printed \
            (PCS, SET, LTR, UNIT...); serialNo when a serial / chassis / batch number is printed.
            - vehiclePlateNo: a Malaysian vehicle registration printed on the document (e.g. "JKA 1234", \
            "WXY1234", "BMA 567 A") - the truck the work or goods are for. driverName when a driver is \
            named. jobNo when our job / shipment / booking reference is printed (e.g. "SO2609/012", \
            "JOB 1234"). loadingVessel / offVessel when vessel names are printed for port or shipping \
            charges.
            - descriptionCategory: the best matching expense category from the list in the message, or \
            null. Workshop, repair, tyre and spare-part documents are MAINTENANCE.
            - accountCode: for each line, the best matching account CODE from the chart of accounts in \
            the message, or null when unsure. Never invent codes.
            """;

    static final String SHAPE = """
            {
              "documentType": "QUOTATION",
              "supplier": {"name": "", "registrationNo": null, "gstNo": null, "sstNo": null, "tinNo": null,
                           "address": null, "phone": null, "email": null},
              "documentNo": "",
              "documentDate": "yyyy-MM-dd",
              "orderDate": null,
              "dueDate": null,
              "deliveryDate": null,
              "purchaseOrderNo": null,
              "currencyCode": "MYR",
              "paymentTermsText": null,
              "jobNo": null,
              "vehiclePlateNo": null,
              "driverName": null,
              "loadingVessel": null,
              "offVessel": null,
              "descriptionCategory": null,
              "subtotal": 0,
              "discountAmount": 0,
              "taxAmount": 0,
              "roundingAdjustment": 0,
              "totalAmount": 0,
              "lines": [
                {"description": "", "itemCode": null, "itemName": null, "serialNo": null,
                 "quantity": 1, "uom": null, "unitPrice": 0,
                 "discountPercent": 0, "discountAmount": 0,
                 "taxPercent": 0, "taxAmount": 0, "amount": 0,
                 "accountCode": null, "remarks": null}
              ],
              "remarks": null,
              "notes": null
            }
            """;

    /** What the stub provider returns: a realistic workshop quotation for a truck. */
    static final String SAMPLE_OUTPUT = """
            {
              "documentType": "QUOTATION",
              "supplier": {"name": "Scania (Malaysia) Sdn Bhd", "registrationNo": "199501014735 (344695-K)",
                           "gstNo": null, "sstNo": "J31-1808-22001234", "tinNo": null,
                           "address": "Lot 3, Jalan Tiang U8/93, Bukit Jelutong, 40150 Shah Alam", "phone": "03-7845 1000", "email": null},
              "documentNo": "QT-2026-08-0421",
              "documentDate": "2026-08-30",
              "orderDate": null,
              "dueDate": null,
              "deliveryDate": "2026-09-05",
              "purchaseOrderNo": null,
              "currencyCode": "MYR",
              "paymentTermsText": "30 days",
              "jobNo": null,
              "vehiclePlateNo": "JKA 1234",
              "driverName": null,
              "loadingVessel": null,
              "offVessel": null,
              "descriptionCategory": "MAINTENANCE",
              "subtotal": 1860.00,
              "discountAmount": 0,
              "taxAmount": 0,
              "roundingAdjustment": 0,
              "totalAmount": 1860.00,
              "lines": [
                {"description": "Brake pad set front axle", "itemCode": "1906399", "itemName": "BRAKE PAD SET", "serialNo": null,
                 "quantity": 2, "uom": "SET", "unitPrice": 680.00, "discountPercent": 0, "discountAmount": 0,
                 "taxPercent": 0, "taxAmount": 0, "amount": 1360.00, "accountCode": null, "remarks": null},
                {"description": "Labour - brake service", "itemCode": null, "itemName": null, "serialNo": null,
                 "quantity": 1, "uom": "JOB", "unitPrice": 500.00, "discountPercent": 0, "discountAmount": 0,
                 "taxPercent": 0, "taxAmount": 0, "amount": 500.00, "accountCode": null, "remarks": null}
              ],
              "remarks": "Valid 7 days",
              "notes": "SAMPLE DATA from the stub provider - not read from your file"
            }
            """;

    private static final String SCHEMA_JSON = """
            {
              "type": "object",
              "properties": {
                "documentType": {"type": ["string", "null"]},
                "supplier": {"type": ["object", "null"], "properties": {
                  "name": {"type": ["string", "null"]}, "registrationNo": {"type": ["string", "null"]},
                  "gstNo": {"type": ["string", "null"]}, "sstNo": {"type": ["string", "null"]},
                  "tinNo": {"type": ["string", "null"]}, "address": {"type": ["string", "null"]},
                  "phone": {"type": ["string", "null"]}, "email": {"type": ["string", "null"]}}},
                "documentNo": {"type": ["string", "null"]},
                "documentDate": {"type": ["string", "null"]},
                "orderDate": {"type": ["string", "null"]},
                "dueDate": {"type": ["string", "null"]},
                "deliveryDate": {"type": ["string", "null"]},
                "purchaseOrderNo": {"type": ["string", "null"]},
                "currencyCode": {"type": ["string", "null"]},
                "paymentTermsText": {"type": ["string", "null"]},
                "jobNo": {"type": ["string", "null"]},
                "vehiclePlateNo": {"type": ["string", "null"]},
                "driverName": {"type": ["string", "null"]},
                "loadingVessel": {"type": ["string", "null"]},
                "offVessel": {"type": ["string", "null"]},
                "descriptionCategory": {"type": ["string", "null"]},
                "subtotal": {"type": ["number", "null"]},
                "discountAmount": {"type": ["number", "null"]},
                "taxAmount": {"type": ["number", "null"]},
                "roundingAdjustment": {"type": ["number", "null"]},
                "totalAmount": {"type": ["number", "null"]},
                "lines": {"type": "array", "items": {"type": "object", "properties": {
                  "description": {"type": ["string", "null"]}, "itemCode": {"type": ["string", "null"]},
                  "itemName": {"type": ["string", "null"]}, "serialNo": {"type": ["string", "null"]},
                  "quantity": {"type": ["number", "null"]}, "uom": {"type": ["string", "null"]},
                  "unitPrice": {"type": ["number", "null"]}, "discountPercent": {"type": ["number", "null"]},
                  "discountAmount": {"type": ["number", "null"]}, "taxPercent": {"type": ["number", "null"]},
                  "taxAmount": {"type": ["number", "null"]}, "amount": {"type": ["number", "null"]},
                  "accountCode": {"type": ["string", "null"]}, "remarks": {"type": ["string", "null"]}}}},
                "remarks": {"type": ["string", "null"]},
                "notes": {"type": ["string", "null"]}
              },
              "required": ["documentType", "supplier", "documentNo", "documentDate", "totalAmount", "lines"]
            }
            """;

    static Map<String, Object> schema(ObjectMapper mapper) {
        try {
            return mapper.readValue(SCHEMA_JSON, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Purchase order extraction schema is not valid JSON", ex);
        }
    }

    static String user(List<GLAccounts> accounts, List<String> descriptions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Read the attached supplier document and return this JSON exactly (same keys, same nesting):\n")
                .append(SHAPE)
                .append("\nExpense categories used on this company's purchase orders - use exactly one for descriptionCategory, or null:\n");
        int shown = 0;
        for (String description : descriptions) {
            if (description == null || description.isBlank()) {
                continue;
            }
            if (shown++ >= MAX_DESCRIPTIONS_IN_PROMPT) {
                sb.append("... (list truncated)");
                break;
            }
            if (shown > 1) {
                sb.append(", ");
            }
            sb.append(description.trim());
        }
        if (shown == 0) {
            sb.append("MAINTENANCE, PORT CHARGES, FUEL, TOLL, OTHER EXPENSES");
        }
        sb.append("\n\nChart of accounts - use the CODE on the left for accountCode:\n");
        int count = 0;
        for (GLAccounts account : accounts) {
            if (count++ >= MAX_ACCOUNTS_IN_PROMPT) {
                sb.append("... (list truncated)\n");
                break;
            }
            sb.append(account.getGlAccountCode()).append(" | ").append(account.getDescription()).append('\n');
        }
        if (accounts.isEmpty()) {
            sb.append("(no chart of accounts available - leave accountCode null)\n");
        }
        return sb.toString();
    }
}

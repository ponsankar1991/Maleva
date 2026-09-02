package my.maleva.api.module.ai.billextraction.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.accounting.entity.GLAccounts;

import java.util.List;
import java.util.Map;

/** Prompt text, expected JSON shape and stub sample for supplier-bill reading. */
final class BillExtractionPrompt {

    private BillExtractionPrompt() {
    }

    static final int MAX_ACCOUNTS_IN_PROMPT = 800;

    static final String SYSTEM = """
            You are an accounts-payable assistant for a Malaysian logistics and freight-forwarding company. \
            You read supplier bills, tax invoices, receipts and statements (PDF pages or photos) and return \
            the facts as JSON for a data-entry clerk to review before saving.

            Rules:
            - Return ONLY a JSON object. No markdown, no explanation, no code fences.
            - Copy values exactly as printed. Never invent data. Use null for anything not on the document.
            - Dates: output as yyyy-MM-dd. Malaysian documents print dates as dd/MM/yyyy; when a numeric \
            date is ambiguous, read it day-first.
            - Amounts: plain numbers with up to 2 decimals. No currency symbols, no thousands separators, no text.
            - currencyCode: 3-letter ISO code. "RM" means MYR. Use MYR when only amounts are shown.
            - Tax: Malaysian SST / service tax (commonly 6% or 8%) or GST. taxPercent per line is a number \
            (0 when no tax); taxAmount is the tax money on that line; unitPrice is before tax; amount is the \
            line total including tax.
            - lines: one entry per billable line on the document. If only a total is printed, return one \
            line for the total. quantity is 1 when not printed.
            - invoiceNo is the supplier's own invoice / bill / receipt number - not a PO number, customer \
            number, account number or DO number.
            - supplier is the party issuing the bill (the payee), not the customer being billed. Include its \
            registration number (SSM / company no.), GST/SST number and TIN when printed.
            - descriptionCategory: the best matching expense category from the list in the message, or null.
            - accountCode: for each line, the best matching account CODE from the chart of accounts in the \
            message, or null when unsure. Never invent codes.
            """;

    static final String SHAPE = """
            {
              "supplier": {"name": "", "registrationNo": null, "gstNo": null, "sstNo": null, "tinNo": null,
                           "address": null, "phone": null, "email": null},
              "invoiceNo": "",
              "invoiceDate": "yyyy-MM-dd",
              "dueDate": null,
              "currencyCode": "MYR",
              "paymentTermsText": null,
              "purchaseOrderNo": null,
              "subtotal": 0,
              "taxAmount": 0,
              "discountAmount": 0,
              "roundingAdjustment": 0,
              "totalAmount": 0,
              "descriptionCategory": null,
              "lines": [
                {"description": "", "quantity": 1, "unitPrice": 0, "taxPercent": 0, "taxAmount": 0,
                 "amount": 0, "accountCode": null}
              ],
              "notes": null
            }
            """;

    /** What the stub provider returns: a realistic Malaysian diesel bill. */
    static final String SAMPLE_OUTPUT = """
            {
              "supplier": {"name": "Petronas Dagangan Berhad", "registrationNo": "198201004309 (88222-D)",
                           "gstNo": null, "sstNo": "W10-1808-31006123", "tinNo": null,
                           "address": "Level 30, Tower 1, PETRONAS Twin Towers, KLCC, 50088 Kuala Lumpur",
                           "phone": "03-2331 3000", "email": null},
              "invoiceNo": "PDB-2026-004521",
              "invoiceDate": "2026-08-28",
              "dueDate": null,
              "currencyCode": "MYR",
              "paymentTermsText": "30 days",
              "purchaseOrderNo": null,
              "subtotal": 2450.00,
              "taxAmount": 0,
              "discountAmount": 0,
              "roundingAdjustment": 0,
              "totalAmount": 2450.00,
              "descriptionCategory": "FUEL",
              "lines": [
                {"description": "Diesel Euro 5 B10 - 1000 litres", "quantity": 1000, "unitPrice": 2.45,
                 "taxPercent": 0, "taxAmount": 0, "amount": 2450.00, "accountCode": null}
              ],
              "notes": "SAMPLE DATA from the stub provider - not read from your file"
            }
            """;

    private static final String SCHEMA_JSON = """
            {
              "type": "object",
              "properties": {
                "supplier": {"type": ["object", "null"], "properties": {
                  "name": {"type": ["string", "null"]}, "registrationNo": {"type": ["string", "null"]},
                  "gstNo": {"type": ["string", "null"]}, "sstNo": {"type": ["string", "null"]},
                  "tinNo": {"type": ["string", "null"]}, "address": {"type": ["string", "null"]},
                  "phone": {"type": ["string", "null"]}, "email": {"type": ["string", "null"]}}},
                "invoiceNo": {"type": ["string", "null"]},
                "invoiceDate": {"type": ["string", "null"]},
                "dueDate": {"type": ["string", "null"]},
                "currencyCode": {"type": ["string", "null"]},
                "paymentTermsText": {"type": ["string", "null"]},
                "purchaseOrderNo": {"type": ["string", "null"]},
                "subtotal": {"type": ["number", "null"]},
                "taxAmount": {"type": ["number", "null"]},
                "discountAmount": {"type": ["number", "null"]},
                "roundingAdjustment": {"type": ["number", "null"]},
                "totalAmount": {"type": ["number", "null"]},
                "descriptionCategory": {"type": ["string", "null"]},
                "lines": {"type": "array", "items": {"type": "object", "properties": {
                  "description": {"type": ["string", "null"]}, "quantity": {"type": ["number", "null"]},
                  "unitPrice": {"type": ["number", "null"]}, "taxPercent": {"type": ["number", "null"]},
                  "taxAmount": {"type": ["number", "null"]}, "amount": {"type": ["number", "null"]},
                  "accountCode": {"type": ["string", "null"]}}}},
                "notes": {"type": ["string", "null"]}
              },
              "required": ["supplier", "invoiceNo", "invoiceDate", "totalAmount", "lines"]
            }
            """;

    static Map<String, Object> schema(ObjectMapper mapper) {
        try {
            return mapper.readValue(SCHEMA_JSON, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Bill extraction schema is not valid JSON", ex);
        }
    }

    static String user(List<GLAccounts> accounts, List<String> categories) {
        StringBuilder sb = new StringBuilder();
        sb.append("Read the attached supplier bill and return this JSON exactly (same keys, same nesting):\n")
                .append(SHAPE)
                .append("\nExpense categories - use exactly one of these for descriptionCategory, or null:\n")
                .append(String.join(", ", categories))
                .append("\n\nChart of accounts - use the CODE on the left for accountCode:\n");
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

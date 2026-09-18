package my.maleva.api.module.customerstatement.dto;

import java.util.Locale;

/**
 * Which statement file(s) a mail carries: the PDF, the Excel workbook, or
 * both. Read leniently - null, blank or anything unknown is the PDF, which is
 * what every statement mail carried before Excel existed.
 */
public enum StatementAttach {
    PDF, EXCEL, BOTH;

    public static StatementAttach of(String value) {
        if (value == null || value.isBlank()) {
            return PDF;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException unknown) {
            return PDF;
        }
    }

    public boolean pdf() {
        return this != EXCEL;
    }

    public boolean excel() {
        return this != PDF;
    }
}

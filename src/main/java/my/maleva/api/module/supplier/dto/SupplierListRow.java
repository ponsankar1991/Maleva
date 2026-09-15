package my.maleva.api.module.supplier.dto;

/**
 * One row of the SupplierView grid — the columns legacy {@code AllSupplier.js}
 * shows, with the symbol, payment term and account code already joined.
 *
 * <p>A record on purpose: its JSON names are the component names. A Lombok
 * class would serialise {@code sName} as {@code sname}.
 */
public record SupplierListRow(
        Integer id,
        String accountCode,
        String qneCode,
        String supplierName,
        String supplierType,
        String sName,
        String termsName,
        String address1,
        String address2,
        String city,
        String mobileNo,
        String gstNo,
        Integer active
) {
}

package my.maleva.api.module.customer.dto.response;

/**
 * One entry for a customer dropdown — the port of the legacy
 * {@code /CustomerMaster/GetCustomer} row ({@code Id}, {@code AccountName}).
 *
 * <p>Built by a JPQL constructor expression from three columns, so filling a
 * combo costs one indexed query and no entity hydration. The paged
 * {@code /select} used by the master list loads every column of every
 * customer plus four joins — measured at 55 s for one company — and is the
 * wrong tool for a dropdown.
 *
 * @param label what the combo shows: {@code CUSTOMER NAME-QNECODE}, the legacy
 *              {@code customername + '-' + CompanyCode}, which is how operators
 *              have always found a customer in the list
 */
public record CustomerOptionDto(Integer id, String customerName, String companyCode, String label) {

    public CustomerOptionDto(Integer id, String customerName, String companyCode) {
        this(id, customerName, companyCode,
                companyCode == null || companyCode.isBlank() ? customerName : customerName + "-" + companyCode);
    }
}

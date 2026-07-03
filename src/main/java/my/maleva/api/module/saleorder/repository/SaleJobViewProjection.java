package my.maleva.api.module.saleorder.repository;

public interface SaleJobViewProjection {

    String getCurrencyName();

    String getCountryName();

    Integer getJobCount();

    String getEmployeeName();

    Integer getEmployeeCount();

    String getJobType();

    Integer getTypeCount();

    String getJobStatus();

    Integer getStatusCount();

    String getCustomerName();

    Double getMonth1();

    Double getMonth2();

    Double getMonth3();

    Double getCurrentMonth();

    Double getAmount();

    Integer getCompanyRefId();
}

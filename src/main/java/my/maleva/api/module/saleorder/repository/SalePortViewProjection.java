package my.maleva.api.module.saleorder.repository;

public interface SalePortViewProjection {
    String getPortName();
    String getSaleMonth();
    Integer getJobCount();
    Double getTotalAmount();
}

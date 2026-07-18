package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

public class SaleOrderMasterRepositoryImpl implements SaleOrderMasterRepositoryCustom {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<SaleOrderInvoiceCheckDto> checkSaleOrderInvoice(SaleOrderInvoiceCheckRequest request) {
        StringBuilder where = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("comid", request.getComid());

        if (Boolean.TRUE.equals(request.getInvoice())) {
            where.append(" AND A.SaleDate >= '2024-10-01' AND A.JStatus IN (6, 15) ");
        } else {
            where.append(" AND A.SaleDate BETWEEN :fromDate AND :toDate ");
            params.addValue("fromDate", request.getFromdate());
            params.addValue("toDate", request.getTodate());

            if (request.getRemarks() != null && request.getRemarks() == 1) {
                where.append(" AND A.InvoiceNo != 0 ");
            } else if (request.getRemarks() != null && request.getRemarks() == 2) {
                where.append(" AND ((A.SaleDate < '2024-10-01' AND ISNULL(A.Remarks, '') = '') OR (A.SaleDate >= '2024-10-01' AND A.InvoiceNo = 0)) ");
            }

            if (request.getStatusid() != null && request.getStatusid() != 0 && Boolean.TRUE.equals(request.getCompletestatusnotshow())) {
                where.append(" AND A.JStatus = :statusId ");
                params.addValue("statusId", request.getStatusid());
            } else if (request.getRemarks() != null && request.getRemarks() == 2) {
                where.append(" AND A.JStatus NOT IN (8, 12) ");
            }

            if (request.getId1() != null && request.getId1() != 0) {
                where.append(" AND A.CustomerRefId = :customerId ");
                params.addValue("customerId", request.getId1());
            }

            if (request.getEmployeeid() != null && request.getEmployeeid() != 0) {
                if (request.getDashboardStatus() != null && request.getDashboardStatus() == 0) {
                    where.append(" AND A.EmployeeRefId = :employeeId ");
                    params.addValue("employeeId", request.getEmployeeid());
                } else {
                    where.append(" AND A.EmployeeRefId IN (SELECT SubEmployeeId AS Id FROM RulesTypeMaster WHERE MasterEmployeeId = :employeeId UNION ALL SELECT :employeeId) ");
                    params.addValue("employeeId", request.getEmployeeid());
                }
            }
        }

        if (request.getOffvesselname() != null && !request.getOffvesselname().isEmpty()) {
            where = new StringBuilder(); // Reset where clause as per legacy logic
            if (Boolean.TRUE.equals(request.getInvoicecheck())) {
                where.append(" AND SM.CNumberDisplay = :offVesselName ");
            } else {
                where.append(" AND A.CNumberDisplay = :offVesselName ");
            }
            params.addValue("offVesselName", request.getOffvesselname());
        }

        String sql = "SELECT " +
                "A.Id as id, " +
                "A.Remarks as remarks, " +
                "A.JobMasterRefId as jobMasterRefId, " +
                "ISNULL(E.EmployeeName, '') as employeeName, " +
                "A.Offvesselname as offvesselname, " +
                "A.Loadingvesselname as loadingvesselname, " +
                "A.SPort as sPort, " +
                "A.OPort as oPort, " +
                "FORMAT(ISNULL(A.SaleDate, '1900-01-01'), 'dd/MM/yyyy') as billDate, " +
                "A.ETA as eta, " +
                "ISNULL(FORMAT(A.ETA, 'dd/MM/yyyy HH:mm:ss'), '') as seta, " +
                "ISNULL(FORMAT(A.ETB, 'dd/MM/yyyy HH:mm:ss'), '') as setb, " +
                "ISNULL(FORMAT(A.OETA, 'dd/MM/yyyy HH:mm:ss'), '') as soeta, " +
                "ISNULL(FORMAT(A.OETB, 'dd/MM/yyyy HH:mm:ss'), '') as soetb, " +
                "ISNULL(CONVERT(VARCHAR(26), A.PickupDate, 20), '') as sPickupDate, " +
                "A.CNumberDisplay as billNoDisplay, " +
                "FORMAT(ISNULL(A.Created_Date, '1900-01-01'), 'dd/MM/yyyy hh:mm:ss') as billTime, " +
                "B.CustomerName as customerName, " +
                "A.Amount as netAmt, " +
                "A.SaleType as saleType, " +
                "A.CNumber as billNo, " +
                "ISNULL(J.Name, '') as jobStatus, " +
                "ISNULL(SM.CNumberDisplay, '') as invoiceNo, " +
                "ISNULL(SM.QNECode, '') as qneCode, " +
                "ISNULL(SM.QNEId, '') as qneId, " +
                "DATEDIFF(DAY, A.CompletedDate, GETDATE()) AS dayCount " +
                "FROM SaleOrderMaster A WITH(NOLOCK) " +
                "INNER JOIN Customer B WITH(NOLOCK) ON A.CustomerRefId = B.Id " +
                "LEFT JOIN EmployeeMaster E WITH(NOLOCK) ON E.Id = A.EmployeeRefId " +
                "LEFT JOIN JobStatusMaster J WITH(NOLOCK) ON J.Id = A.JStatus " +
                "LEFT JOIN SaleMaster SM WITH(NOLOCK) ON SM.id = A.InvoiceNo " +
                "WHERE A.CompanyRefId = :comid AND A.Active = 1 " + where.toString() +
                " ORDER BY dayCount DESC";

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(SaleOrderInvoiceCheckDto.class));
    }
}

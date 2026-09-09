package my.maleva.api.module.saleorder.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import my.maleva.api.module.invoice.dto.SaleDetailsViewModel;
import my.maleva.api.module.invoice.dto.SaleMasterViewModel;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckDto;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCheckRequest;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderMasterRepositoryImpl implements SaleOrderMasterRepositoryCustom {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

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

    @Override
    public List<Integer> findFilteredIds(Specification<SaleOrderMaster> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<SaleOrderMaster> root = query.from(SaleOrderMaster.class);

        query.select(root.get("id"));

        Predicate predicate = specification.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<SaleMasterViewModel> findSaleMasterRows(Integer companyId, List<Integer> orderIds) {
        List<SaleOrderSearchRow<SaleMasterViewModel>> rows =
                queryInBatches(companyId, orderIds, SaleOrderSearchQueries.MASTER_ROWS, MASTER_ROW_MAPPER);

        // SaleDate DESC, then DETA DESC, then Id DESC - newest job first.
        // Applied here rather than in SQL because the ids are queried in batches, so
        // no single statement sees every row.
        rows.sort(SaleOrderSearchRow.masterOrder());

        return rows.stream().map(SaleOrderSearchRow::value).toList();
    }

    @Override
    public List<SaleDetailsViewModel> findSaleDetailRows(Integer companyId, List<Integer> orderIds) {
        List<SaleOrderSearchRow<SaleDetailsViewModel>> rows =
                queryInBatches(companyId, orderIds, SaleOrderSearchQueries.DETAIL_ROWS, DETAIL_ROW_MAPPER);

        // Legacy ordered detail rows by SaleOrderDetails.Id; restore that across batches.
        rows.sort(SaleOrderSearchRow.detailOrder());

        return rows.stream().map(SaleOrderSearchRow::value).toList();
    }

    /**
     * Runs the query once per batch of ids and concatenates the results.
     *
     * @see SaleOrderSearchQueries#ID_BATCH_SIZE for why the list is split at all
     */
    private <T> List<SaleOrderSearchRow<T>> queryInBatches(Integer companyId,
                                                           List<Integer> orderIds,
                                                           String sql,
                                                           RowMapper<SaleOrderSearchRow<T>> rowMapper) {
        if (companyId == null || orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<SaleOrderSearchRow<T>> results = new ArrayList<>(orderIds.size());
        for (int start = 0; start < orderIds.size(); start += SaleOrderSearchQueries.ID_BATCH_SIZE) {
            int end = Math.min(start + SaleOrderSearchQueries.ID_BATCH_SIZE, orderIds.size());
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("companyId", companyId)
                    .addValue("orderIds", orderIds.subList(start, end));

            results.addAll(jdbcTemplate.query(sql, params, rowMapper));
        }
        return results;
    }

    private static final RowMapper<SaleOrderSearchRow<SaleMasterViewModel>> MASTER_ROW_MAPPER = (rs, rowNum) -> {
        SaleMasterViewModel vm = new SaleMasterViewModel();

        vm.setId(getInteger(rs, "Id"));
        vm.setSportsaleorderid(getInteger(rs, "Sportsaleorderid"));
        vm.setInvoiceId(getInteger(rs, "InvoiceId"));
        vm.setRemarks(rs.getString("Remarks"));
        vm.setDestination(rs.getString("Destination"));
        vm.setFlighTime(rs.getString("FlighTime"));
        vm.setOrigin(rs.getString("Origin"));
        vm.setJobMasterRefId(getInteger(rs, "JobMasterRefId"));
        vm.setEmployeeName(rs.getString("EmployeeName"));
        vm.setOffvesselname(rs.getString("Offvesselname"));
        vm.setSname(rs.getString("Sname"));
        vm.setLoadingvesselname(rs.getString("Loadingvesselname"));
        vm.setSPort(rs.getString("SPort"));
        vm.setOPort(rs.getString("OPort"));
        vm.setBillDate(rs.getString("BillDate"));
        vm.setDeta(rs.getString("DETA"));
        vm.setEta(getDateTime(rs, "ETA"));
        vm.setSeta(rs.getString("SETA"));
        vm.setSetb(rs.getString("SETB"));
        vm.setSoeta(rs.getString("SOETA"));
        vm.setSoetb(rs.getString("SOETB"));
        vm.setSPickupDate(rs.getString("SPickupDate"));
        vm.setBillNoDisplay(rs.getString("BillNoDisplay"));
        vm.setBillTime(rs.getString("BillTime"));
        vm.setCustomerName(rs.getString("CustomerName"));
        vm.setJobType(rs.getString("JobType"));
        vm.setNetAmt(getDouble(rs, "NetAmt"));
        vm.setSaleType(rs.getString("SaleType"));
        vm.setBillNo(getInteger(rs, "BillNo"));
        vm.setJobStatus(rs.getString("JobStatus"));
        vm.setInvoiceNo(rs.getString("InvoiceNo"));
        vm.setQneCode(rs.getString("QNECode"));
        vm.setQneId(rs.getString("QNEId"));
        vm.setQuantity(rs.getString("Quantity"));
        vm.setTotalWeight(rs.getString("TotalWeight"));

        return new SaleOrderSearchRow<>(vm,
                getDateTime(rs, "BillTimeSort"),
                getDateTime(rs, "DETASort"),
                vm.getId());
    };

    private static final RowMapper<SaleOrderSearchRow<SaleDetailsViewModel>> DETAIL_ROW_MAPPER = (rs, rowNum) -> {
        SaleDetailsViewModel vm = new SaleDetailsViewModel();

        vm.setDiscountAmt(getDouble(rs, "DiscAmount"));
        vm.setDiscountPercent(getDouble(rs, "DiscPer"));
        vm.setItemQty(getDouble(rs, "ItemQty"));
        vm.setMrp(getDouble(rs, "MRP"));
        vm.setProductName(rs.getString("PName"));
        vm.setSdRemarks(rs.getString("SDRemarks"));
        vm.setSaleRate(getDouble(rs, "SalesRate"));
        vm.setSaleRefId(getInteger(rs, "SaleOrderMasterRefId"));
        vm.setTaxAmt(getDouble(rs, "TaxAmount"));
        vm.setTaxPercent(getDouble(rs, "TaxPercent"));
        vm.setProductCode(rs.getString("Prod_Code"));
        vm.setSAmount(getDouble(rs, "Amount"));
        vm.setCurrencyValue(getDouble(rs, "CurrencyValue"));
        vm.setActualAmount(getDouble(rs, "ActualAmount"));

        return new SaleOrderSearchRow<>(vm, null, null, getInteger(rs, "DetailId"));
    };

    private static Integer getInteger(ResultSet rs, String label) throws SQLException {
        int value = rs.getInt(label);
        return rs.wasNull() ? null : value;
    }

    private static Double getDouble(ResultSet rs, String label) throws SQLException {
        double value = rs.getDouble(label);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime getDateTime(ResultSet rs, String label) throws SQLException {
        Timestamp value = rs.getTimestamp(label);
        return value == null ? null : value.toLocalDateTime();
    }
}

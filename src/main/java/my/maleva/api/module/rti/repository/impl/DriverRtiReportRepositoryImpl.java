package my.maleva.api.module.rti.repository.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.rti.dto.DriverRtiReportDto;
import my.maleva.api.module.rti.dto.DriverRtiReportRequest;
import my.maleva.api.module.rti.repository.DriverRtiReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DriverRtiReportRepositoryImpl implements DriverRtiReportRepository {

    private static final Logger log = LoggerFactory.getLogger(DriverRtiReportRepositoryImpl.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<DriverRtiReportDto> findReport(DriverRtiReportRequest request) {
        List<DriverRtiReportDto> results = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select distinct A.Id, A.CNumberDisplay, A.SaleDate, FORMAT(isnull(A.SaleDate,'1900-01-01'),'dd/MM/yyyy') as SSaleDate, ")
                    .append("A.SleepingAmount, A.Destination, A.PickupAmount, A.DropAmount, A.ExitAmount, A.EmptyDeliveryAmount, A.ManpwAmount, A.Amount, ")
                    .append("B.DriverName, C.TruckName, A.PckHandling, A.Punctuality, A.DocumentSub, A.Created_Date, ")
                    .append("(Select sum(Salary) from RTIDetails where RTIMasterRefId = A.Id) as Salary ")
                    .append("from RTIMaster A with(nolock) ")
                    .append("left join RTIDetails RD with(nolock) on RD.RTIMasterRefId=A.Id ")
                    .append("inner join DriverMaster B with(nolock) on A.DriverRefid=B.Id ")
                    .append("inner join TruckMaster C with(nolock) on A.TruckRefid = C.Id ")
                    .append("where A.CompanyRefId = :comid and A.Active=1 ")
                    .append("and cast(RD.PickupDateD as Date) between :fromDate and :toDate ");

            Map<String, Object> params = new HashMap<>();
            params.put("comid", request.getComid());
            // default boundary values if null
            LocalDate from = request.getFromDate() != null ? request.getFromDate() : LocalDate.now(ZoneId.systemDefault());
            LocalDate to = request.getToDate() != null ? request.getToDate() : LocalDate.now(ZoneId.systemDefault());
            params.put("fromDate", java.sql.Date.valueOf(from));
            params.put("toDate", java.sql.Date.valueOf(to));

            if (request.getDriverId() != null && request.getDriverId() != 0) {
                sql.append(" and A.DriverRefid = :driverId ");
                params.put("driverId", request.getDriverId());
            }
            if (request.getTruckId() != null && request.getTruckId() != 0) {
                sql.append(" and A.TruckRefid = :truckId ");
                params.put("truckId", request.getTruckId());
            }

            List<DriverRtiReportDto> list = jdbcTemplate.query(sql.toString(), params, new RowMapper<DriverRtiReportDto>() {
                @Override
                public DriverRtiReportDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                    DriverRtiReportDto dto = new DriverRtiReportDto();
                    dto.setId(rs.getLong("Id"));
                    dto.setCNumberDisplay(rs.getString("CNumberDisplay"));

                    Timestamp saleTs = rs.getTimestamp("SaleDate");
                    if (saleTs != null) {
                        dto.setSaleDate(saleTs.toLocalDateTime());
                    }
                    dto.setSSaleDate(rs.getString("SSaleDate"));

                    dto.setSleepingAmount(getBigDecimal(rs, "SleepingAmount"));
                    dto.setDestination(rs.getString("Destination"));
                    dto.setPickupAmount(getBigDecimal(rs, "PickupAmount"));
                    dto.setDropAmount(getBigDecimal(rs, "DropAmount"));
                    dto.setExitAmount(getBigDecimal(rs, "ExitAmount"));
                    dto.setEmptyDeliveryAmount(getBigDecimal(rs, "EmptyDeliveryAmount"));
                    dto.setManpwAmount(getBigDecimal(rs, "ManpwAmount"));
                    dto.setAmount(getBigDecimal(rs, "Amount"));

                    dto.setDriverName(rs.getString("DriverName"));
                    dto.setTruckName(rs.getString("TruckName"));

                    dto.setPckHandling(getInteger(rs, "PckHandling"));
                    dto.setPunctuality(getInteger(rs, "Punctuality"));
                    dto.setDocumentSub(getInteger(rs, "DocumentSub"));

                    Timestamp createdTs = rs.getTimestamp("Created_Date");
                    if (createdTs != null) dto.setCreatedDate(createdTs.toLocalDateTime());

                    dto.setSalary(getBigDecimal(rs, "Salary"));

                    return dto;
                }
            });

            results.addAll(list);
        } catch (Exception ex) {
            log.error("Error while executing RTI report query", ex);
            throw ex;
        }
        return results;
    }

    private BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal v = rs.getBigDecimal(column);
        return v != null ? v : BigDecimal.ZERO;
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }
}



package my.maleva.api.module.rti.mapper;

import my.maleva.api.module.rti.dto.RtiEmployeeAssignmentResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class RtiEmployeeAssignmentRowMapper implements RowMapper<RtiEmployeeAssignmentResponse> {

    @Override
    public RtiEmployeeAssignmentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RtiEmployeeAssignmentResponse(
                rs.getObject("Id", Integer.class),
                rs.getObject("RTIMasterRefId", Integer.class),
                rs.getObject("SaleOrderMasterRefId", Integer.class),
                getLocalDateTime(rs, "PickupDateD"),
                getLocalDateTime(rs, "DeliveryDateD"),
                rs.getString("OriginD"),
                rs.getString("DestinationD"),
                rs.getString("RTINumber"),
                rs.getString("Remarks"),
                rs.getObject("DriverRefid", Integer.class),
                rs.getObject("TruckRefid", Integer.class),
                rs.getObject("Active", Integer.class),
                rs.getObject("PickupCount", Integer.class),
                rs.getObject("DropCount", Integer.class),
                rs.getString("SaleOrderNumber"),
                rs.getString("VesselNameRaw"),
                rs.getString("CustomerName"),
                rs.getString("Commodity"),
                rs.getString("Quantity"),
                rs.getString("TruckSize"),
                rs.getString("EmployeeName"),
                rs.getString("DriverName"),
                rs.getString("TruckNumber"),
                rs.getString("TruckType")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
        return (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }
}

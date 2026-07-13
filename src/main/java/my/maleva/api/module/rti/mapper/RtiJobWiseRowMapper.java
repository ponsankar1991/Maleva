package my.maleva.api.module.rti.mapper;

import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class RtiJobWiseRowMapper implements RowMapper<RtiJobWiseViewResponse> {

    @Override
    public RtiJobWiseViewResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDate collectionDate = rs.getObject("CollectionDate", LocalDate.class);
        LocalDate deliveryDate = rs.getObject("DeliveryDate", LocalDate.class);

        return new RtiJobWiseViewResponse(
                rs.getLong("JobRefId"),
                rs.getString("RTINumber"),
                rs.getString("JobNumber"),
                rs.getString("VesselName"),
                rs.getString("CargoDetails"),
                rs.getString("CollectAt"),
                rs.getString("DeliveryAt"),
                collectionDate,
                deliveryDate,
                rs.getString("TruckNumber"),
                rs.getString("DriverName"),
                rs.getString("TruckSize"),
                rs.getInt("PickupCount"),
                rs.getInt("DropCount"),
                rs.getString("Remarks"),
                rs.getInt("LegCount")
        );
    }
}

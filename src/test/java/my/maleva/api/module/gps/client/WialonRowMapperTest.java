package my.maleva.api.module.gps.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.gps.dto.GpsEngineHourRow;
import my.maleva.api.module.gps.dto.GpsFillingRow;
import my.maleva.api.module.gps.dto.wialon.WialonResultRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the parsing the legacy job did inline in GPSJob.cs, using response
 * shapes taken from the Wialon select_result_rows payload.
 */
class WialonRowMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WialonRowMapper mapper = new WialonRowMapper();

    private List<WialonResultRow> parse(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<List<WialonResultRow>>() { });
    }

    @Test
    void flattensGroupedRowsDownToTheLeaves() throws Exception {
        String json = """
                [
                  {"n":0,"d":1,"c":[{"t":"WA 1234 A"}],"r":[
                    {"n":1,"d":0,"c":[{"t":"1"},{"t":"WA 1234 A"},{"t":"19.08.2026 08:09:43"},
                                      {"t":"Port Klang"},{"t":"1"},{"t":"120 l"},{"t":"Ahmad"}]},
                    {"n":2,"d":0,"c":[{"t":"2"},{"t":"WA 1234 A"},{"t":"19.08.2026 18:30:00"},
                                      {"t":"Shah Alam"},{"t":"1"},{"t":"90 l"},{"t":"Ahmad"}]}
                  ]}
                ]
                """;

        List<WialonResultRow> leaves = mapper.flatten(parse(json));

        assertEquals(2, leaves.size());
    }

    @Test
    void keepsTopLevelRowsThatAreAlreadyLeaves() throws Exception {
        String json = """
                [
                  {"n":0,"d":0,"c":[{"t":"1"},{"t":"WA 1234 A"},{"t":"19.08.2026 08:09:43"},
                                    {"t":"Port Klang"},{"t":"1"},{"t":"120 l"},{"t":"Ahmad"}]}
                ]
                """;

        assertEquals(1, mapper.flatten(parse(json)).size());
    }

    @Test
    void mapsAFillingRowByColumnPosition() throws Exception {
        String json = """
                [
                  {"n":0,"d":0,"c":[{"t":"1"},{"t":"WA 1234 A"},{"t":"19.08.2026 08:09:43"},
                                    {"t":"Port Klang"},{"t":"1"},{"t":"120 l"},{"t":"Ahmad"}]}
                ]
                """;

        GpsFillingRow row = mapper.toFillingRow(mapper.flatten(parse(json)).get(0));

        assertEquals("WA 1234 A", row.getVehicle());
        assertEquals(LocalDateTime.of(2026, 8, 19, 8, 9, 43), row.getTime());
        assertEquals("Port Klang", row.getLocation());
        assertEquals("1", row.getCount());
        assertEquals("120 l", row.getFilled());
        assertEquals("Ahmad", row.getDriver());
    }

    @Test
    void mapsAnEngineHourRowByColumnPosition() throws Exception {
        String json = """
                [
                  {"n":0,"d":0,"c":[{"t":"1"},{"t":"19 Aug 2026 06:00:00"},{"t":"Depot"},
                                    {"t":"19 Aug 2026 14:30:00"},{"t":"Port Klang"},{"t":"8:30:00"},
                                    {"t":"6:10:00"},{"t":"2:20:00"},{"t":"312 km"},{"t":"14 l"}]}
                ]
                """;

        GpsEngineHourRow row = mapper.toEngineHourRow(mapper.flatten(parse(json)).get(0));

        assertEquals(LocalDateTime.of(2026, 8, 19, 6, 0, 0), row.getBeginTime());
        assertEquals(LocalDateTime.of(2026, 8, 19, 14, 30, 0), row.getEndTime());
        assertEquals("Depot", row.getBeginLocation());
        assertEquals("Port Klang", row.getEndLocation());
        assertEquals("8:30:00", row.getTotalTime());
        assertEquals("6:10:00", row.getInMotion());
        assertEquals("2:20:00", row.getIdling());
        assertEquals("312 km", row.getMileage());
        assertEquals("14 l", row.getConsumedByFlsInIdleRun());
    }

    @Test
    void returnsNullForRowsWithTooFewCells() throws Exception {
        String json = """
                [{"n":0,"d":0,"c":[{"t":"1"},{"t":"WA 1234 A"}]}]
                """;

        assertNull(mapper.toFillingRow(mapper.flatten(parse(json)).get(0)));
    }

    @Test
    void treatsThePlaceholderAsAMissingValue() {
        assertNull(mapper.parseDate("-----"));
        assertNull(mapper.parseDate(""));
        assertNull(mapper.parseDate(null));
    }

    @Test
    void acceptsEveryDateFormatWialonRenders() {
        assertEquals(LocalDateTime.of(2026, 8, 19, 8, 9, 43),
                mapper.parseDate("19.08.2026 08:09:43"));
        assertEquals(LocalDateTime.of(2026, 8, 19, 8, 9, 43),
                mapper.parseDate("2026-08-19 08:09:43"));
        assertEquals(LocalDateTime.of(2026, 8, 19, 8, 9, 43),
                mapper.parseDate("19-08-2026 08:09:43"));
        assertEquals(LocalDateTime.of(2026, 8, 19, 8, 9, 43),
                mapper.parseDate("19 Aug 2026 08:09:43"));
    }

    @Test
    void returnsNullInsteadOfThrowingOnAnUnknownDateFormat() {
        // The legacy job called DateTime.ParseExact and blew up the whole run here.
        assertNull(mapper.parseDate("sometime last tuesday"));
    }

    @Test
    void stopsRecursingOnDeeplyNestedRows() throws Exception {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 12; i++) {
            json.append("{\"n\":").append(i).append(",\"d\":1,\"r\":[");
        }
        json.append("{\"n\":99,\"d\":0,\"c\":[{\"t\":\"1\"}]}");
        json.append("]}".repeat(12));
        json.append("]");

        // Guarded at depth 10, so the leaf below it is dropped rather than
        // costing an unbounded walk on a malformed payload.
        assertTrue(mapper.flatten(parse(json.toString())).isEmpty());
    }
}

package my.maleva.api.module.rti.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RTIRouteActivitiesRepositoryTest {

    @Autowired
    private RTIRouteActivitiesRepository repository;

    @Test
    public void testGetForwardingPlanningReport() {
        // Inputs mirroring the test script
        Integer companyRefId = 6;
        LocalDateTime fromDate = LocalDateTime.of(2026, 6, 29, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 8, 10, 23, 59, 59);

        System.out.println("=================================================");
        System.out.println("EXECUTING GET FORWARDING PLANNING REPORT QUERY...");
        System.out.println("=================================================");

        // Execute the native query via JPA
        List<Object[]> results = repository.getForwardingPlanningReport(companyRefId, fromDate, toDate);
        
        assertNotNull(results, "The query result should not be null.");

        System.out.println("QUERY SUCCESSFUL! Found " + results.size() + " rows.");
        System.out.println("-------------------------------------------------");

        // Print results dynamically (lorryNo, driverName, agentName, contact, fromLocation, eta, jobType, port, remarks)
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            System.out.println("Row " + (i + 1) + ":");
            System.out.println("  Lorry No      : " + (row[0] != null ? row[0] : "NULL"));
            System.out.println("  Driver Name   : " + (row[1] != null ? row[1] : "NULL"));
            System.out.println("  Agent Name    : " + (row[2] != null ? row[2] : "NULL"));
            System.out.println("  Contact       : " + (row[3] != null ? row[3] : "NULL"));
            System.out.println("  From Location : " + (row[4] != null ? row[4] : "NULL"));
            System.out.println("  ETA           : " + (row[5] != null ? row[5] : "NULL"));
            System.out.println("  Job Type      : " + (row[6] != null ? row[6] : "NULL"));
            System.out.println("  Port          : " + (row[7] != null ? row[7] : "NULL"));
            System.out.println("  Remarks       : " + (row[8] != null ? row[8] : "NULL"));
            System.out.println("-------------------------------------------------");
        }
    }
}

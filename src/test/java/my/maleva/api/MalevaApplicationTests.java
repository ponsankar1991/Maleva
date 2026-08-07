package my.maleva.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import my.maleva.api.module.planning.service.ForwardingPlanningService;
import java.time.LocalDate;

@SpringBootTest
class MalevaApplicationTests {

    @Autowired
    private ForwardingPlanningService service;

    @Test
    void testForwardingQuery() {
        System.out.println("TESTING SERVICE...");
        try {
            service.getForwardingPlanningReport(6, LocalDate.of(2026, 6, 29), LocalDate.of(2026, 8, 10));
            System.out.println("SERVICE SUCCESS!");
        } catch (Exception e) {
            System.out.println("SERVICE ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

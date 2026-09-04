package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Tuning for the planning truck/driver suggestion ({@code module/ai/planning}).
 * Every value has a working default; override under {@code ai.planning.*}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai.planning")
public class PlanningSuggestProperties {

    /** How far back the planning history is read. */
    private int historyDays = 365;

    /** Alternatives returned per row besides the top pick. */
    private int maxAlternatives = 3;

    /** Days back the "ended last trip at" continuity check looks. */
    private int continuityDays = 3;

    /** Events within this many days of the planning date count triple. */
    private int recentDays = 30;

    /** Events within this many days count double; older ones count once. */
    private int midDays = 90;

    /** Weight of a past job for the same customer. */
    private double customerWeight = 3.0;

    /** Weight of a past job on the same origin-destination lane. */
    private double laneWeight = 2.0;

    /** Weight of a past job through the same port. */
    private double portWeight = 1.0;

    /** Bonus when the truck or driver ended its last trip where this job starts. */
    private double continuityBonus = 6.0;

    /** Weight of a past job where this driver drove the chosen truck; the driver's default truck counts too. */
    private double pairingWeight = 1.5;

    /** Penalty per job beyond {@link #jobsBeforeLoadPenalty} already on the truck that day. */
    private double loadPenalty = 0.5;

    private int jobsBeforeLoadPenalty = 2;

    /** Outside (subcontractor) trucks are chosen by hand, never suggested. */
    private boolean excludeOutsideTrucks = true;

    /** Trucks whose insurance, road tax or permit has expired are not suggested. */
    private boolean excludeExpiredTrucks = true;

    /** Longest chain of connected jobs one truck is given in a day. */
    private int maxJobsPerTrip = 5;
}

package my.maleva.api.module.gps.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rules for pairing a GPS fuel filling with a fuel entry.
 *
 * The legacy rule lived inline in the EditFuelEntry SQL:
 * {@code ABS(filled - Aliter) <= Aliter * 0.35}, closest match wins.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fuel.gps-match")
public class FuelGpsMatchProperties {

    /**
     * Allowed relative difference between the GPS litres and the entered litres.
     * 0.35 reproduces the legacy 35% tolerance.
     */
    private double tolerance = 0.35;

    /**
     * Allow one GPS filling to be claimed by only one fuel entry.
     *
     * The legacy query used TOP 1 per entry with nothing stopping two entries
     * from selecting the same filling, so on a day where a truck refuelled more
     * than once the same litres could be counted twice. Set false only to
     * reproduce that behaviour deliberately.
     */
    private boolean oneToOne = true;
}

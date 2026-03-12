package my.maleva.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DateTimeUtil - Utility class for date and time operations
 * Provides consistent formatting and parsing across the application
 * 
 * @author Enterprise Java Team
 * @version 1.0
 * @since 2026-03-12
 */
public class DateTimeUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    // Standard date formats used throughout the application
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_WITH_MILLIS_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateTimeUtil() {
        // Utility class - no instantiation
    }

    /**
     * Format LocalDate to string (dd/MM/yyyy)
     * 
     * @param date the LocalDate to format
     * @return formatted date string, or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            logger.warn("Error formatting date: {}", date, e);
            return "";
        }
    }

    /**
     * Format LocalDateTime to string (dd/MM/yyyy HH:mm:ss)
     * 
     * @param dateTime the LocalDateTime to format
     * @return formatted datetime string, or empty string if dateTime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        try {
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("Error formatting datetime: {}", dateTime, e);
            return "";
        }
    }

    /**
     * Format LocalDateTime to string (dd/MM/yyyy HH:mm:ss)
     * with null-safe handling - returns specified default if null
     * 
     * @param dateTime the LocalDateTime to format
     * @param defaultValue the default value if dateTime is null
     * @return formatted datetime string, or defaultValue if dateTime is null
     */
    public static String formatDateTimeOrDefault(LocalDateTime dateTime, String defaultValue) {
        if (dateTime == null) {
            return defaultValue != null ? defaultValue : "";
        }
        try {
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("Error formatting datetime: {}", dateTime, e);
            return defaultValue != null ? defaultValue : "";
        }
    }

    /**
     * Parse string to LocalDate (dd/MM/yyyy)
     * 
     * @param dateString the date string to parse
     * @return LocalDate object, or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Error parsing date string: {}", dateString, e);
            return null;
        }
    }

    /**
     * Parse string to LocalDateTime (dd/MM/yyyy HH:mm:ss)
     * 
     * @param dateTimeString the datetime string to parse
     * @return LocalDateTime object, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Error parsing datetime string: {}", dateTimeString, e);
            return null;
        }
    }

    /**
     * Check if two dates fall within a range (inclusive)
     * 
     * @param date the date to check
     * @param startDate the range start date
     * @param endDate the range end date
     * @return true if date is between startDate and endDate (inclusive)
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Check if two datetimes fall within a range (inclusive)
     * 
     * @param dateTime the datetime to check
     * @param startDateTime the range start datetime
     * @param endDateTime the range end datetime
     * @return true if datetime is between startDateTime and endDateTime (inclusive)
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    /**
     * Convert LocalDate to LocalDateTime at start of day
     * 
     * @param date the LocalDate to convert
     * @return LocalDateTime at 00:00:00, or null if date is null
     */
    public static LocalDateTime toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Convert LocalDate to LocalDateTime at end of day (23:59:59)
     * 
     * @param date the LocalDate to convert
     * @return LocalDateTime at 23:59:59, or null if date is null
     */
    public static LocalDateTime toEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }
}


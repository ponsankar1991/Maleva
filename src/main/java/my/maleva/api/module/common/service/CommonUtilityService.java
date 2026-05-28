package my.maleva.api.module.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonUtilityService - General utility functions
 * Replaces .NET utility methods from commonfunctions class
 *
 * Includes:
 * - String manipulation utilities
 * - File handling helpers
 * - Content type resolution
 */
@Service
public class CommonUtilityService {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtilityService.class);

    /**
     * Split string into parts of specified length
     * Example: "123456789" with partLength 3 = ["123", "456", "789"]
     */
    public List<String> splitInParts(String text, int partLength) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (partLength <= 0) {
            throw new IllegalArgumentException("Part length must be positive");
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < text.length(); i += partLength) {
            int endIndex = Math.min(i + partLength, text.length());
            parts.add(text.substring(i, endIndex));
        }
        return parts;
    }

    /**
     * Get MIME content type by file extension
     */
    public static String getContentTypeByExtension(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }

        switch (extension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".pdf":
                return "application/pdf";
            case ".xlsx":
            case ".xls":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".docx":
            case ".doc":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".txt":
                return "text/plain";
            case ".csv":
                return "text/csv";
            case ".xml":
                return "application/xml";
            case ".json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Check if file exists at given path
     */
    public boolean fileExists(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            return new File(filePath).exists();
        } catch (Exception ex) {
            logger.warn("Error checking file existence for path: {}", filePath, ex);
            return false;
        }
    }

    /**
     * Delete file from path
     */
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception ex) {
            logger.error("Error deleting file: {}", filePath, ex);
            return false;
        }
    }

    /**
     * Extract file extension from filename
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Extract filename from full path
     */
    public String getFilenameFromPath(String filepath) {
        if (filepath == null) {
            return "";
        }
        return filepath.substring(filepath.lastIndexOf(File.separator) + 1);
    }

    /**
     * Build email recipient list from comma-separated string
     */
    public List<String> parseEmailList(String emailString) {
        List<String> emails = new ArrayList<>();
        if (emailString == null || emailString.isEmpty()) {
            return emails;
        }

        String[] emailArray = emailString.split("[,;]");
        for (String email : emailArray) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.isEmpty() && isValidEmail(trimmedEmail)) {
                emails.add(trimmedEmail);
            }
        }
        return emails;
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Format phone number - remove special characters
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replace("+", "")
                .replace("-", "")
                .replace(" ", "")
                .replace("(", "")
                .replace(")", "")
                .trim();
    }

    /**
     * Safe null-coalescing for strings
     */
    public String coalesceString(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    /**
     * Truncate string to maximum length
     */
    public String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}


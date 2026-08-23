package my.maleva.api.module.filehandling.model;

import my.maleva.api.common.exception.InvalidRequestException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identifies the folder that holds the attachments of one business record.
 *
 * The legacy .NET actions built this path by concatenating four request headers
 * straight into {@code MapPath}, so a {@code SubFolderName} of {@code ../../..}
 * reached any directory the worker process could read. Every segment is
 * validated here instead, and {@link #resolveDirectory(Path)} re-checks the
 * result against the storage root, so a scope can only ever name a directory
 * below it.
 */
public final class AttachmentScope {

    /**
     * Folder names come from the caller and become directory names. Legacy
     * callers pass values such as {@code NewSaleOrderMaster}, {@code PayBills}
     * and {@code Ptwapproval}; nothing outside this alphabet is a real folder.
     */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    private final int companyRefId;
    private final String folderName;
    private final int recordId;
    private final String subFolderName;

    private AttachmentScope(int companyRefId, String folderName, int recordId, String subFolderName) {
        this.companyRefId = companyRefId;
        this.folderName = folderName;
        this.recordId = recordId;
        this.subFolderName = subFolderName;
    }

    public static AttachmentScope of(Integer companyRefId, String folderName, Integer recordId, String subFolderName) {
        int company = requireNonNegative(companyRefId, "companyRefId");
        int record = requireNonNegative(recordId, "recordId");
        String folder = requireSafeSegment(folderName, "folderName");
        String subFolder = blankToNull(subFolderName);
        if (subFolder != null) {
            subFolder = requireSafeSegment(subFolder, "subFolderName");
        }
        return new AttachmentScope(company, folder, record, subFolder);
    }

    public int getCompanyRefId() {
        return companyRefId;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getRecordId() {
        return recordId;
    }

    public String getSubFolderName() {
        return subFolderName;
    }

    /** Storage-root-relative directory, e.g. {@code 6/SalesOrder/12056/Ptw}. */
    public String relativeDirectory() {
        StringBuilder path = new StringBuilder()
                .append(companyRefId).append('/')
                .append(folderName).append('/')
                .append(recordId);
        if (subFolderName != null) {
            path.append('/').append(subFolderName);
        }
        return path.toString();
    }

    /**
     * URL prefix the browser uses, e.g. {@code /uploads/6/SalesOrder/12056/}.
     * Stored file paths are this prefix plus the file name, which is the shape
     * the legacy screens already persist in the {@code FilePath} columns.
     */
    public String publicPathPrefix(String publicUrlPrefix) {
        return trimTrailingSlash(publicUrlPrefix) + "/" + relativeDirectory() + "/";
    }

    /** Absolute directory under {@code storageRoot}, guaranteed not to escape it. */
    public Path resolveDirectory(Path storageRoot) {
        Path root = storageRoot.toAbsolutePath().normalize();
        Path resolved = root.resolve(relativeDirectory()).normalize();
        if (!resolved.startsWith(root)) {
            throw new InvalidRequestException("Attachment scope resolves outside the storage root");
        }
        return resolved;
    }

    private static int requireNonNegative(Integer value, String field) {
        if (value == null || value < 0) {
            throw new InvalidRequestException(field + " is required and must not be negative");
        }
        return value;
    }

    private static String requireSafeSegment(String value, String field) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            throw new InvalidRequestException(field + " is required");
        }
        if (!SAFE_SEGMENT.matcher(trimmed).matches()) {
            throw new InvalidRequestException(
                    field + " may only contain letters, digits, underscore and hyphen");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String trimTrailingSlash(String value) {
        String prefix = (value == null || value.isBlank()) ? "/uploads" : value.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        while (prefix.length() > 1 && prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AttachmentScope scope)) {
            return false;
        }
        return companyRefId == scope.companyRefId
                && recordId == scope.recordId
                && folderName.equals(scope.folderName)
                && Objects.equals(subFolderName, scope.subFolderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyRefId, folderName, recordId, subFolderName);
    }

    @Override
    public String toString() {
        return relativeDirectory();
    }
}

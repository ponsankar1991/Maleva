package my.maleva.api.module.ai.common;

/** The identifying facts a document gives about its issuer, used to find the supplier master row. */
public record SupplierHint(String name, String registrationNo, String gstNo, String sstNo, String tinNo) {

    public static SupplierHint ofName(String name) {
        return new SupplierHint(name, null, null, null, null);
    }
}

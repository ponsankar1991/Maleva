package my.maleva.api.module.inventory.exception;

public class InvalidAssetStateException extends RuntimeException {
    public InvalidAssetStateException(String message) {
        super(message);
    }
}

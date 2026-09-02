package my.maleva.api.integration.llm;

import java.util.List;

/** Task keys used for provider selection, stored preferences and the call log. */
public final class LlmTasks {

    public static final String BILL_EXTRACTION = "bill-extraction";
    public static final String CONNECTION_TEST = "connection-test";

    public record TaskDef(String key, String label) {
    }

    /** Tasks the settings screen lets an admin override per company. */
    public static final List<TaskDef> CONFIGURABLE = List.of(
            new TaskDef(BILL_EXTRACTION, "Bill / receipt reading"));

    private LlmTasks() {
    }

    public static boolean isConfigurable(String key) {
        return CONFIGURABLE.stream().anyMatch(t -> t.key().equals(key));
    }
}

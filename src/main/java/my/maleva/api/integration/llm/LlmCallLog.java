package my.maleva.api.integration.llm;

import my.maleva.api.common.config.LlmProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Bounded, in-memory record of recent model calls: who was called, how long it
 * took and how many tokens it used. Resets on restart by design - this is a
 * cost dashboard for the settings screen, not an audit trail.
 */
@Component
public class LlmCallLog {

    private final int capacity;
    private final Deque<LlmCallRecord> records = new ArrayDeque<>();

    @Autowired
    public LlmCallLog(LlmProperties properties) {
        this(properties.getCallLogSize());
    }

    LlmCallLog(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public synchronized void record(LlmCallRecord record) {
        records.addFirst(record);
        while (records.size() > capacity) {
            records.removeLast();
        }
    }

    /** Newest first. */
    public synchronized List<LlmCallRecord> recent(int limit) {
        List<LlmCallRecord> out = new ArrayList<>();
        Iterator<LlmCallRecord> it = records.iterator();
        while (it.hasNext() && out.size() < Math.max(0, limit)) {
            out.add(it.next());
        }
        return out;
    }

    public synchronized int size() {
        return records.size();
    }
}

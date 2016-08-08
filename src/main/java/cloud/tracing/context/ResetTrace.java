package cloud.tracing.context;

import cloud.tracing.ContextKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Re-initiates predefined params in {@link TraceContext} when requested
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class ResetTrace {
    private static Logger log = LoggerFactory.getLogger(ResetTrace.class);

    /**
     * Create trace for the attributes in @{@link Keys}
     */
    void createTrace() {
        for (ContextKeysEnum key : Keys.values()) {
            TraceContext.addTrace(key, createId());
            log.debug("created {}[{}] - {}", key, key.getKey(), TraceContext.getTrace(key));
        }
    }

    private String createId() {
        return UUID.randomUUID().toString();
    }

    private enum Keys implements ContextKeysEnum {
        REQUEST_ID(TraceContext.Keys.REQUEST_ID.getKey());
        String key;

        Keys(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }
}



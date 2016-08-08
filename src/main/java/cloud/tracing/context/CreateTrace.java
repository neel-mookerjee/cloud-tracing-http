package cloud.tracing.context;

import cloud.tracing.ContextKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Creates/initiates {@link TraceContext} from request headers
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class CreateTrace {
    private static Logger log = LoggerFactory.getLogger(CreateTrace.class);

    void createTraceIfAbsent() {
        for (ContextKeysEnum key : Keys.values()) {
            if (StringUtils.isEmpty(TraceContext.getTrace(key))) {
                TraceContext.addTrace(key, createId());
                log.debug("created {}[{}] - {}", key, key.getKey(), TraceContext.getTrace(key));
            }
        }
    }

    private String createId() {
        return UUID.randomUUID().toString();
    }

    private enum Keys implements ContextKeysEnum {
        REQUEST_ID(TraceContext.Keys.REQUEST_ID.getKey()),
        TRACE_ID(TraceContext.Keys.TRACE_ID.getKey());
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



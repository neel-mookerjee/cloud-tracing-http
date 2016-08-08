package cloud.tracing.context;

import cloud.tracing.ContextLogKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains keys for {@link TraceContext} to avail in MDC
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

interface EventTraceLogUtil {
    String EMPTY_TRACE_DEFAULT_LOG_VALUE = "NONE";
    Logger log = LoggerFactory.getLogger(EventTraceLogUtil.class);

    enum Keys implements ContextLogKeysEnum {
        /*SESSION_ID(TraceContext.Keys.SESSION_ID.getKey(), "SessionId"),*/
        REQUEST_ID(TraceContext.Keys.REQUEST_ID.getKey(), "RequestId"),
        TRACE_ID(TraceContext.Keys.TRACE_ID.getKey(), "TraceId"),
        CUSTOMER_ID(TraceContext.Keys.CUSTOMER_ID.getKey(), "CustomerId"),
        ACCOUNT_ID(TraceContext.Keys.ACCOUNT_ID.getKey(), "AccountId"),
        ORDER_ID(TraceContext.Keys.ORDER_ID.getKey(), "OrderId");
        String key;
        String displayKey;

        Keys(String key, String displayKey) {
            this.key = key;
            this.displayKey = displayKey;
        }

        public String getKey() {
            return this.key;
        }

        public String getDisplayKey() {
            return displayKey;
        }
    }
}


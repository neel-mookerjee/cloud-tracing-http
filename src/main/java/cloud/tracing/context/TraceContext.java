package cloud.tracing.context;

import cloud.tracing.ContextKeysEnum;
import cloud.tracing.ContextLogKeysEnum;
import org.apache.log4j.MDC;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for gathering and reporting Trace Context information.
 * <ul>
 * <li><b>REQUEST_ID</b> - UUID generated for each request or a span - the basic unit of work</li>
 * <li><b>TRACE_ID</b> - UUID generated for the entire operation cycle which may be a series of requests or spans</li>
 * <li><b>SESSION_ID</b> - Session Id of the session created at the UI and passed on to services</li>
 * <li><b>CUSTOMER_ID</b> - Carries Customer Id if it is set by the calling service or component</li>
 * <li><b>ACCOUNT_ID</b> - Carries Account Id if it is set by the calling service or component</li>
 * <li><b>ORDER_ID</b> - Carries Order Id if it is set by the calling service or component</li>
 * <li><b>TOKEN</b> - Provision to carry JWT token with Client Context information (can be leveraged for the Client Context library)</li>
 * </ul>
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public final class TraceContext {

    private static InheritableThreadLocal<Map<String, String>> TRACES =
            new InheritableThreadLocal<Map<String, String>>() {
                @Override
                protected Map<String, String> initialValue() {
                    return new HashMap<>();
                }
                @Override
                protected Map<String, String> childValue(Map<String, String> parentValue){
                    return new HashMap<>(parentValue);
                }
            };

    private TraceContext() {

    }

    static Map<String, String> getCopyOfTraceMap() {
        return new HashMap<>(TRACES.get());
    }

    static void clearTraces() {
        TRACES.get().clear();
        TRACES.remove();
        for (ContextLogKeysEnum keyToLog : EventTraceLogUtil.Keys.values()) {
            EventTraceLogUtil.log.debug("removing from MDC {}[{}] - {}", keyToLog, keyToLog.getDisplayKey(), MDC.get(keyToLog.getDisplayKey()));
            MDC.remove(keyToLog.getDisplayKey());
        }
    }


    /**
     * Adds values to @{@link TraceContext} against the keys bounded in @{@link Keys}
     * <p>Adds the attribute to MDC</p>
     *
     * @param key   a key from @{@link Keys}
     * @param value value @{@link String} against the key
     */

    public static void addTrace(ContextKeysEnum key, String value) {
        TRACES.get().put(key.getKey(), value);
        for (ContextLogKeysEnum keyToLog : EventTraceLogUtil.Keys.values()) {
            if (key.getKey().equals(keyToLog.getKey())) {
                EventTraceLogUtil.log.debug("adding to MDC {}[{}] - {}", key, keyToLog.getDisplayKey(), TraceContext.getTrace(key));
                MDC.put(keyToLog.getDisplayKey(), StringUtils.isEmpty(value) ? EventTraceLogUtil.EMPTY_TRACE_DEFAULT_LOG_VALUE : value);
                break;
            }
        }
        EventTraceLogUtil.log.debug("Checking MDC");
    }

    /**
     * Return info from @{@link TraceContext} against a key from @{@link Keys}
     *
     * @param key a key from @{@link Keys}
     * @return String value with trace info
     */
    public static String getTrace(ContextKeysEnum key) {
        return TRACES.get().get(key.getKey());
    }

    public enum Keys implements ContextKeysEnum {
        SESSION_ID("X-Context-Session-Id"),
        TRACE_ID("X-Context-Trace-Id"),
        REQUEST_ID("X-Context-Request-Id"),
        CUSTOMER_ID("X-Context-Customer-Id"),
        ACCOUNT_ID("X-Context-Account-Id"),
        ORDER_ID("X-Context-Order-Id"),
        TOKEN("X-Context-Token");

        String key;

        Keys(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }
}

package cloud.tracing.instrument.rest;

import cloud.tracing.ContextKeysEnum;
import cloud.tracing.TraceExtractor;
import cloud.tracing.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Trace info extractor that extracts tracing info from {@link HttpServletRequest}
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class HttpServletRequestExtractor implements TraceExtractor<HttpServletRequest> {
    private static Logger log = LoggerFactory.getLogger(HttpServletRequestExtractor.class);

    @Override
    public Map<ContextKeysEnum, String> extractTrace(HttpServletRequest request) {
        {
            Map<ContextKeysEnum, String> headerTraces = new HashMap<>();
            for (ContextKeysEnum key : TraceContext.Keys.values()) {
                log.debug(" extracting {}[{}] - {} ", key, key.getKey(), request.getHeader(key.getKey()));
                headerTraces.put(key, request.getHeader(key.getKey()));
            }
            return headerTraces;
        }
    }
}

package cloud.tracing.instrument.rest;

import cloud.tracing.ContextKeysEnum;
import cloud.tracing.TraceInjector;
import cloud.tracing.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

/**
 * Request injector that injects tracing info to {@link HttpRequest}
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class HttpRequestInjector implements TraceInjector<HttpRequest> {
    private static Logger log = LoggerFactory.getLogger(HttpRequestInjector.class);

    @Override
    public void inject(HttpRequest request) {
        for (ContextKeysEnum key : TraceContext.Keys.values()) {
            log.debug("injecting to header {}[{}] - {}", key, key.getKey(), TraceContext.getTrace(key));
            request.getHeaders().set(key.getKey(), TraceContext.getTrace(key));
        }
    }
}

package cloud.tracing.instrument.rest;

import cloud.tracing.TracingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * A {@link ClientHttpRequestInterceptor} to intercept outgoing http client request from RestTemplate
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class RestHttpClientInterceptor implements ClientHttpRequestInterceptor {
    private static Logger log = LoggerFactory.getLogger(RestHttpClientInterceptor.class);
    protected final TracingHandler tracingHandler;

    public RestHttpClientInterceptor(TracingHandler tracingHandler) {
        super();
        this.tracingHandler = tracingHandler;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug("Intercepting outgoing call");
        tracingHandler.handle(request);
        log.debug("Intercepted outgoing call");
        return execution.execute(request, body);
    }
}

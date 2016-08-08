package cloud.tracing.instrument.rest;

import cloud.tracing.TraceInjector;
import cloud.tracing.Tracer;
import cloud.tracing.TracingHandler;
import org.springframework.http.HttpRequest;

/**
 * Handles outgoing {@link HttpRequest} (i.e. client Http request) for tracing
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class OutgoingRequestTracingHandler implements TracingHandler<HttpRequest> {
    private final TraceInjector injector;
    private final Tracer tracer;

    public OutgoingRequestTracingHandler(TraceInjector injector, Tracer tracer) {
        this.injector = injector;
        this.tracer = tracer;
    }

    @Override
    public void handle(HttpRequest request) {
        tracer.initiateTrace();
        injector.inject(request);
    }
}

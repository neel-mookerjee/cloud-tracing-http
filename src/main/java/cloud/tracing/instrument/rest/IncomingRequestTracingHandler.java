package cloud.tracing.instrument.rest;

import cloud.tracing.ContextKeysEnum;
import cloud.tracing.TraceExtractor;
import cloud.tracing.Tracer;
import cloud.tracing.TracingHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Handles incoming {@link HttpServletRequest} for tracing
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class IncomingRequestTracingHandler implements TracingHandler<HttpServletRequest> {
    protected final TraceExtractor extractor;
    protected final Tracer tracer;

    public IncomingRequestTracingHandler(TraceExtractor extractor, Tracer tracer) {
        this.extractor = extractor;
        this.tracer = tracer;
    }

    @Override
    public void handle(HttpServletRequest request) {
        final Map<ContextKeysEnum, String> traces = extractor.extractTrace(request);
        tracer.initiateTrace(traces);
    }
}

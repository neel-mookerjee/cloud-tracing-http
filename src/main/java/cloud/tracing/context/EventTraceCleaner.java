package cloud.tracing.context;

import cloud.tracing.TraceCleaner;
import cloud.tracing.Tracer;

/**
 * Cleans up {@link TraceContext}
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class EventTraceCleaner implements TraceCleaner {
    final Tracer tracer;

    public EventTraceCleaner(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void cleanupTrace() {
        tracer.removeTrace();
    }
}

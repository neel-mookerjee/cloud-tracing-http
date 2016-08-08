package cloud.tracing;

import cloud.tracing.context.TraceContext;

/**
 * Contract for {@link TraceContext} cleaner
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface TraceCleaner {
    /**
     * Cleans up the map of the trace information and removes it from the thread
     */
    void cleanupTrace();
}

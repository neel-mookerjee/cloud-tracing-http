package cloud.tracing;

import java.util.Map;

/**
 * Contract for trace info extractor from carrier T
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface TraceExtractor<T> {
    /**
     * Extracts the trace information from the carrier @T
     *
     * @param carrier carrier (e.g. http request)
     * @return Map @{@link Map} with extracted header
     */
    Map<ContextKeysEnum, String> extractTrace(T carrier);
}

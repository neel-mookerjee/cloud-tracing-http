package cloud.tracing;

import java.util.Map;

/**
 * Contract for Tracer
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface Tracer {
    /**
     * Initiates tracing with the info in the incoming map
     *
     * @param headers Map @{@link Map} with header attributes
     */
    void initiateTrace(Map<ContextKeysEnum, String> headers);

    /**
     * Initiate traces for known attributes using predefined logic
     */
    void initiateTrace();

    /**
     * Removes traces from the thread
     */
    void removeTrace();
}

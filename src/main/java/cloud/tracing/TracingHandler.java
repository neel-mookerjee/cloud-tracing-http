package cloud.tracing;

/**
 * Contract for trace handler
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface TracingHandler<T> {
    /**
     * Handles trace info for the carrier @T
     *
     * @param carrier carrier (e.g. http request)
     */
    void handle(T carrier);
}

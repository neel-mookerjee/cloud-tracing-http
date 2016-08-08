package cloud.tracing;

/**
 * Contract for trace injector into carrier T
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface TraceInjector<T> {
    /**
     * Injects the trace information to the carrier @T
     *
     * @param carrier carrier (e.g. http request)
     */
    void inject(T carrier);
}
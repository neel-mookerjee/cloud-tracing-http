package cloud.tracing;

/**
 * Contract for log context keys
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public interface ContextLogKeysEnum extends ContextKeysEnum {
    String getDisplayKey();
}
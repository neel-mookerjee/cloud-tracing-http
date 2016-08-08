package cloud.tracing.context;

import cloud.tracing.ContextKeysEnum;
import cloud.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Performs actions on {@link TraceContext}
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class EventTracer implements Tracer {
    private static Logger log = LoggerFactory.getLogger(EventTracer.class);

    private final CreateTrace traceCreator;
    private final ResetTrace traceRecreator;

    public EventTracer(CreateTrace traceCreator, ResetTrace traceRecreator) {
        this.traceRecreator = traceRecreator;
        this.traceCreator = traceCreator;
    }

    @Override
    public void initiateTrace(Map<ContextKeysEnum, String> headerTraces) {
        for (ContextKeysEnum key : TraceContext.Keys.values()) {
            log.debug("Checking trace for {}[{}] and got: {}", key, key.getKey(), TraceContext.getTrace(key));
            if(StringUtils.isEmpty(TraceContext.getTrace(key))) {
                log.debug("initiate {}[{}] - {}", key, key.getKey(), headerTraces.get(key));
                TraceContext.addTrace(key, headerTraces.get(key));
            }
        }
        log.debug("available traces set in context");
        traceCreator.createTraceIfAbsent();
        log.debug("missing traces created");
    }

    @Override
    public void initiateTrace() {
        traceRecreator.createTrace();
        log.debug("missing traces created");
    }

    @Override
    public void removeTrace() {
        TraceContext.clearTraces();
    }

}

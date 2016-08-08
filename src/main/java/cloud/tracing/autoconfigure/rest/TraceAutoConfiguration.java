package cloud.tracing.autoconfigure.rest;

import cloud.tracing.TraceCleaner;
import cloud.tracing.TraceInjector;
import cloud.tracing.Tracer;
import cloud.tracing.context.CreateTrace;
import cloud.tracing.context.EventTraceCleaner;
import cloud.tracing.context.EventTracer;
import cloud.tracing.context.ResetTrace;
import cloud.tracing.instrument.rest.HttpRequestInjector;
import cloud.tracing.instrument.rest.HttpServletRequestExtractor;
import cloud.tracing.instrument.rest.IncomingRequestTracingHandler;
import cloud.tracing.instrument.rest.OutgoingRequestTracingHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration}
 * to enable tracing using cloud-tracing
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(value = "cloud.tracing.enabled", matchIfMissing = true)
public class TraceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public HttpServletRequestExtractor httpServletRequestExtractor() {
        return new HttpServletRequestExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceInjector httpRequestInjector() {
        return new HttpRequestInjector();
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateTrace traceCreator() {
        return new CreateTrace();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResetTrace traceRereator() {
        return new ResetTrace();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceCleaner traceCleaner(Tracer tracer) {
        return new EventTraceCleaner(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer restTracer(CreateTrace traceCreator, ResetTrace recreator) {
        return new EventTracer(traceCreator, recreator);
    }

    @Bean
    @ConditionalOnMissingBean
    public IncomingRequestTracingHandler incomingRequestTracingHandler(HttpServletRequestExtractor extractor, Tracer tracer) {
        return new IncomingRequestTracingHandler(extractor, tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutgoingRequestTracingHandler outgoingRequestTracingHandler(TraceInjector injector, Tracer tracer) {
        return new OutgoingRequestTracingHandler(injector, tracer);
    }
}
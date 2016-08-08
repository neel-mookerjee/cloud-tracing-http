package cloud.tracing.autoconfigure.rest;

import cloud.tracing.TraceCleaner;
import cloud.tracing.instrument.rest.IncomingRequestTracingHandler;
import cloud.tracing.instrument.rest.WebRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration}
 * to enable tracing using cloud-tracing
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

@Configuration
@ConditionalOnWebApplication
@ConditionalOnBean(IncomingRequestTracingHandler.class)
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceWebRequestAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WebRequestInterceptor webRequestInterceptor(IncomingRequestTracingHandler requestTracingHandler, TraceCleaner traceCleaner) {
        return new WebRequestInterceptor(requestTracingHandler, traceCleaner);
    }

    @Configuration
    protected static class TraceWebRequestConfiguration extends WebMvcConfigurerAdapter {
        private static Logger log = LoggerFactory.getLogger(TraceWebRequestConfiguration.class);
        @Autowired
        private WebRequestInterceptor webRequestInterceptor;

        @PostConstruct
        public void init() {
            log.info("Initialized ContextLogging Interceptor");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            log.info("Registering up context logging interceptor");
            registry.addInterceptor(webRequestInterceptor);
        }
    }
}

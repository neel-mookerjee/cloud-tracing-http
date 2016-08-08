package cloud.tracing.autoconfigure.rest;

import cloud.tracing.instrument.rest.OutgoingRequestTracingHandler;
import cloud.tracing.instrument.rest.RestHttpClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration}
 * to enable tracing using cloud-tracing
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

@Configuration
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnBean(OutgoingRequestTracingHandler.class)
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceRestHttpClientAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RestHttpClientInterceptor restHttpClientInterceptor(OutgoingRequestTracingHandler requestTracingHandler) {
        return new RestHttpClientInterceptor(requestTracingHandler);
    }

    @Configuration
    protected static class TraceRestHttpClientConfiguration {
        private static Logger log = LoggerFactory.getLogger(TraceRestHttpClientConfiguration.class);
        @Autowired(required = false)
        private Collection<RestTemplate> restTemplates;
        @Autowired
        private RestHttpClientInterceptor restHttpClientInterceptor;

        @PostConstruct
        public void init() {
            if (this.restTemplates != null) {
                log.info("Registering up http clientcontext logging interceptor");
                for (RestTemplate restTemplate : this.restTemplates) {
                    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(
                            restTemplate.getInterceptors());
                    interceptors.add(this.restHttpClientInterceptor);
                    restTemplate.setInterceptors(interceptors);
                }
            }
        }
    }

}

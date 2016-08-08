package cloud.tracing.context;

import cloud.tracing.autoconfigure.rest.TraceAutoConfiguration;
import cloud.tracing.autoconfigure.rest.TraceRestHttpClientAutoConfiguration;
import cloud.tracing.autoconfigure.rest.TraceWebRequestAutoConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @author arghanil.mukhopadhya
 * @since 1.0.0.RC3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TraceContextOverwritingIssueIntegrationTest.Config.class,
        TraceAutoConfiguration.class, TraceRestHttpClientAutoConfiguration.class,
        TraceWebRequestAutoConfiguration.class})
@WebIntegrationTest(randomPort = true)
public class TraceContextOverwritingIssueIntegrationTest {
    private static final String TEST_TOKEN_MOD = "token";

    private static Logger log = LoggerFactory.getLogger(TraceContextOverwritingIssueIntegrationTest.class);

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    Config config;
    @Autowired
    TestRestController testRestController;

    @Before
    public void setup() {
        TraceContext.clearTraces();
    }

    @After
    public void cleanup() {
        TraceContext.clearTraces();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void traceContextTest1() {

        // check empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check MDC
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));

        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create("http://localhost:" + this.config.port + "/serviceTest"))
                .build();

        ResponseEntity<String> resp = new RestTemplate().exchange(requestEntity,
                String.class);

        assertEquals(resp.getBody(), "[from serviceTest]");

        // check empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check MDC
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));
    }

    @Configuration
    @EnableAutoConfiguration
    static class Config
            implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
        int port;

        @Override
        public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
            this.port = event.getEmbeddedServletContainer().getPort();
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        TestRestController customRestController(Config config, RestTemplate restTemplate) {
            return new TestRestController(config, restTemplate);
        }

        @Bean
        public TestFilter getContextTokenFilter() {
            return new TestFilter();
        }

        @Bean
        public FilterRegistrationBean registerContextTokenFilter(TestFilter testFilter) {
            FilterRegistrationBean registration = new FilterRegistrationBean();
            registration.setFilter(testFilter);
            registration.setOrder(Integer.MAX_VALUE);
            registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
            return registration;
        }
    }

    static class TestFilter extends GenericFilterBean {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            log.info("Inside the dummy filter - changing the TraceContext");
            TraceContext.addTrace(TraceContext.Keys.TOKEN, TEST_TOKEN_MOD);
            chain.doFilter(request, response);
        }
    }

    @RestController
    static class TestRestController {
        Config config;
        RestTemplate restTemplate;

        TestRestController(Config config, RestTemplate restTemplate) {
            this.config = config;
            this.restTemplate = restTemplate;
        }

        @RequestMapping("/serviceTest")
        public String service1() {
            log.info("Inside the controller - verifying the TraceContext");
            // verify TraceContext
            assertNotNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
            assertNotNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
            assertEquals(TraceContext.getTrace(TraceContext.Keys.TOKEN), TEST_TOKEN_MOD);
            return "[from serviceTest]";
        }
    }
}
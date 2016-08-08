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

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * @author arghanil.mukhopadhya
 * @since 1.0.0.RC3
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {InheritableThreadLocalTraceContextTest1.TestConfig.class,
        TraceAutoConfiguration.class, TraceRestHttpClientAutoConfiguration.class,
        TraceWebRequestAutoConfiguration.class})
@WebIntegrationTest(randomPort = true)
public class InheritableThreadLocalTraceContextTest1 {
    private static final String TEST_REQ_ID = "1e89268d-c70c-4a04-9617-reqid";
    private static final String TEST_SESSION_ID = "1e89268d-c70c-4a04-9617-sessionid";
    private static final String TEST_TRACE_ID = "1e89268d-c70c-4a04-9617-traceid";
    private static final String TEST_ACCOUNT_ID = "accountid";
    private static final String TEST_CUSTOMER_ID = "customerid";
    private static final String TEST_ORDER_ID = "orderid";
    private static final String TEST_ORDER_ID_2 = "orderid2";
    private static final String TEST_TOKEN = "token";
    private static Logger log = LoggerFactory.getLogger(InheritableThreadLocalTraceContextTest1.class);
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    TestConfig config;
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
                .get(URI.create("http://localhost:" + this.config.port + "/service1"))
                .header(TraceContext.Keys.REQUEST_ID.getKey(), TEST_REQ_ID)
                .header(TraceContext.Keys.SESSION_ID.getKey(), TEST_SESSION_ID)
                .header(TraceContext.Keys.TRACE_ID.getKey(), TEST_TRACE_ID)
                .header(TraceContext.Keys.CUSTOMER_ID.getKey(), TEST_CUSTOMER_ID)
                .header(TraceContext.Keys.ACCOUNT_ID.getKey(), TEST_ACCOUNT_ID)
                .header(TraceContext.Keys.ORDER_ID.getKey(), TEST_ORDER_ID)
                .header(TraceContext.Keys.TOKEN.getKey(), TEST_TOKEN)
                .build();

        // use new rest template
        ResponseEntity<String> resp = new RestTemplate().exchange(requestEntity,
                String.class);

        // success in response
        assertTrue(resp.getStatusCode().is2xxSuccessful());

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
    static class TestConfig
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
        TestRestController customRestController(TestConfig config, RestTemplate restTemplate) {
            return new TestRestController(config, restTemplate);
        }
    }

    @RestController
    static class TestRestController {
        TestConfig config;
        RestTemplate restTemplate;

        TestRestController(TestConfig config, RestTemplate restTemplate) {
            this.config = config;
            this.restTemplate = restTemplate;
        }

        @RequestMapping("/service1")
        public void service1() {

            // alter param in TraceContext
            TraceContext.addTrace(TraceContext.Keys.ORDER_ID, TEST_ORDER_ID_2);

            // execute tasks using thread pool
            ExecutorService executor = Executors.newFixedThreadPool(10);
            log.info("Initial thread: {}", Thread.currentThread().getName());
            for (int i = 0; i < 100; i++) {
                Runnable worker = new Task(config, "Task-" + i);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            log.info("Finished all threads.");
        }

        @RequestMapping("/service2")
        public String service2() {
            assertEquals(TraceContext.getTrace(TraceContext.Keys.SESSION_ID), TEST_SESSION_ID);
            assertEquals(TraceContext.getTrace(TraceContext.Keys.TRACE_ID), TEST_TRACE_ID);
            assertEquals(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID), TEST_ACCOUNT_ID);
            assertEquals(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID), TEST_CUSTOMER_ID);
            // new order id is present in TraceContext
            assertEquals(TraceContext.getTrace(TraceContext.Keys.ORDER_ID), TEST_ORDER_ID_2);
            assertEquals(TraceContext.getTrace(TraceContext.Keys.TOKEN), TEST_TOKEN);
            // request id will be newly created
            assertNotEquals(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID), TEST_REQ_ID);

            // verify MDC
            //assertEquals(TEST_SESSION_ID, MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
            // new request id will match with the request id in the TraceContext
            assertEquals(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID), MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
            assertEquals(TEST_TRACE_ID, MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
            assertEquals(TEST_ACCOUNT_ID, MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
            assertEquals(TEST_CUSTOMER_ID, MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
            assertEquals(TEST_ORDER_ID_2, MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));

            return "[From service 2]";
        }

        class Task implements Runnable {
            private TestConfig config;
            private String taskName;

            public Task(TestConfig config, String taskName) {
                this.config = config;
                this.taskName = taskName;
            }

            @Override
            public void run() {
                log.info("Start {} for {}", Thread.currentThread().getName(), taskName);
                RequestEntity<?> requestEntity = RequestEntity
                        .get(URI.create("http://localhost:" + this.config.port + "/service2")).build();
                ResponseEntity<String> resp = restTemplate.exchange(requestEntity,
                        String.class);
                log.info("End {} from {} for {}", resp.getBody(), Thread.currentThread().getName(), taskName);
            }
        }
    }
}
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * @author arghanil.mukhopadhya
 * @since 1.0.0.RC3
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {InheritableThreadLocalTraceContextTest2.TestConfig.class,
        TraceAutoConfiguration.class, TraceRestHttpClientAutoConfiguration.class,
        TraceWebRequestAutoConfiguration.class})
@WebIntegrationTest(randomPort = true)
public class InheritableThreadLocalTraceContextTest2 {
    private static final String TEST_REQ_ID = "1e89268d-c70c-4a04-9617-reqid";
    private static final String TEST_SESSION_ID = "1e89268d-c70c-4a04-9617-sessionid";
    private static final String TEST_TRACE_ID = "1e89268d-c70c-4a04-9617-traceid";
    private static final String TEST_ACCOUNT_ID = "accountid";
    private static final String TEST_CUSTOMER_ID = "customerid";
    private static final String TEST_ORDER_ID = "orderid";
    private static final String TEST_TOKEN = "token";
    private static final int TASK_NUM = 10;
    private static Logger log = LoggerFactory.getLogger(InheritableThreadLocalTraceContextTest2.class);
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
        ResponseEntity<TestResult> resp = new RestTemplate().exchange(requestEntity,
                TestResult.class);

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

        // Assert TestResult
        TestResult result = resp.getBody();
        // contexts are there
        assertTrue(result.getResults().size() == TASK_NUM);

        // unique request ids
        Set<String> requestIds = new HashSet<>();

        Map<String, String> parentContextMap;
        Map<String, String> outputContextMap;
        Map<String, String> inputContextMap;
        Map<String, String> mdcContextMap;
        String task;
        for (ThreadTraceTestResult threadTraceTestResult : result.getResults()) {
            parentContextMap = threadTraceTestResult.getParentContext();
            inputContextMap = threadTraceTestResult.getInputContext();
            outputContextMap = threadTraceTestResult.getOutputContext();
            mdcContextMap = threadTraceTestResult.getMdcContext();
            task = threadTraceTestResult.getTaskName();

            log.info("Task - {}: Parent traces: {}", threadTraceTestResult.getTaskName(), parentContextMap);
            log.info("Task - {}: RPC input traces: {}", threadTraceTestResult.getTaskName(), inputContextMap);
            log.info("Task - {}: RPC service MDC traces: {}", threadTraceTestResult.getTaskName(), mdcContextMap);
            log.info("Task - {}: RPC output traces: {}", threadTraceTestResult.getTaskName(), outputContextMap);

            // parent and RPC incoming traces must match except request id
            assertEquals(parentContextMap.get(TraceContext.Keys.SESSION_ID.getKey()), inputContextMap.get(TraceContext.Keys.SESSION_ID.getKey()));
            assertEquals(parentContextMap.get(TraceContext.Keys.TRACE_ID.getKey()), inputContextMap.get(TraceContext.Keys.TRACE_ID.getKey()));
            assertEquals(parentContextMap.get(TraceContext.Keys.TOKEN.getKey()), inputContextMap.get(TraceContext.Keys.TOKEN.getKey()));
            assertEquals(parentContextMap.get(TraceContext.Keys.ORDER_ID.getKey()), inputContextMap.get(TraceContext.Keys.ORDER_ID.getKey()));
            assertEquals(parentContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()), inputContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()));
            assertEquals(parentContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()), inputContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()));
            assertNotEquals(parentContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()), inputContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()));

            // RPC incoming trace must match the RPC service MDC traces
            //assertEquals(inputContextMap.get(TraceContext.Keys.SESSION_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.TRACE_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.ORDER_ID.getKey()), mdcContextMap.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));

            // incoming and outgoing traces must match except request id
            assertEquals(inputContextMap.get(TraceContext.Keys.SESSION_ID.getKey()), outputContextMap.get(TraceContext.Keys.SESSION_ID.getKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.TRACE_ID.getKey()), outputContextMap.get(TraceContext.Keys.TRACE_ID.getKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.TOKEN.getKey()), outputContextMap.get(TraceContext.Keys.TOKEN.getKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.ORDER_ID.getKey()), outputContextMap.get(TraceContext.Keys.ORDER_ID.getKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()), outputContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()));
            assertEquals(inputContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()), outputContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()));
            assertNotEquals(parentContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()), outputContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()));

            // outgoing trace must match with the task specific values
            assertEquals(String.format("%s%s", TEST_ORDER_ID, task.replaceFirst("Task-", "")), outputContextMap.get(TraceContext.Keys.ORDER_ID.getKey()));
            assertEquals(String.format("%s%s", TEST_ACCOUNT_ID, task.replaceFirst("Task-", "")), outputContextMap.get(TraceContext.Keys.ACCOUNT_ID.getKey()));
            assertEquals(String.format("%s%s", TEST_CUSTOMER_ID, task.replaceFirst("Task-", "")), outputContextMap.get(TraceContext.Keys.CUSTOMER_ID.getKey()));

            assertTrue(!requestIds.contains(outputContextMap.get(TraceContext.Keys.REQUEST_ID.getKey())));
            requestIds.add(outputContextMap.get(TraceContext.Keys.REQUEST_ID.getKey()));
        }
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
        public TestResult service1() throws ExecutionException, InterruptedException {
            TestResult testResult = new TestResult();

            // execute tasks using thread pool
            ExecutorService executor = Executors.newFixedThreadPool(10);
            log.info("Initial thread: {}", Thread.currentThread().getName());
            List<Future<ThreadTraceTestResult>> resultList = new ArrayList<>();
            for (int i = 0; i < TASK_NUM; i++) {
                log.info("Loop {}", TraceContext.getCopyOfTraceMap());
                Callable<ThreadTraceTestResult> callable = new Task(config, restTemplate, "Task-" + i, i);
                // should be async response
                Future<ThreadTraceTestResult> future = executor.submit(callable);
                // only add to list
                resultList.add(future);
                log.info("Adding Call: {}", i);
            }
            log.info("Finished all threads.");
            // now get from the future objects
            log.info("After Loop {}", TraceContext.getCopyOfTraceMap());
            for (Future<ThreadTraceTestResult> future : resultList) {
                log.info("Loop for get {}", TraceContext.getCopyOfTraceMap());
                testResult.getResults().add(future.get());
            }
            executor.shutdown();
            return testResult;
        }

        @RequestMapping("/service2/{taskName}")
        public ThreadTraceTestResult service2(@PathVariable String taskName) {
            log.info("service 2 - {} - start {}", taskName, TraceContext.getCopyOfTraceMap());
            ThreadTraceTestResult response = new ThreadTraceTestResult();
            response.setTaskName(taskName);
            response.setMdcContext(MDC.getCopyOfContextMap());
            log.info("service 2 - {} -   MDC {}", taskName, MDC.getCopyOfContextMap());
            response.setInputContext(TraceContext.getCopyOfTraceMap());
            log.info("service 2 - {} -   end {}", taskName, TraceContext.getCopyOfTraceMap());
            return response;
        }

        class Task implements Callable<ThreadTraceTestResult> {
            RestTemplate restTemplate;
            String uri;
            TestConfig config;
            String taskName;
            int index;

            Task(TestConfig config, RestTemplate restTemplate, String taskName, int index) {
                this.config = config;
                this.restTemplate = restTemplate;
                this.uri = "http://localhost:" + this.config.port + "/service2/" + taskName;
                this.taskName = taskName;
                this.index = index;
            }

            public ThreadTraceTestResult call() throws InterruptedException {
                TraceContext.addTrace(TraceContext.Keys.ORDER_ID, TEST_ORDER_ID + index);
                TraceContext.addTrace(TraceContext.Keys.ACCOUNT_ID, TEST_ACCOUNT_ID + index);
                TraceContext.addTrace(TraceContext.Keys.CUSTOMER_ID, TEST_CUSTOMER_ID + index);
                log.info("Inside call: {} - {}", index, TraceContext.getCopyOfTraceMap());
                Map parentContext = TraceContext.getCopyOfTraceMap();
                log.info("Start {} for {} with {} -- Start - {}", Thread.currentThread().getName(), taskName, TraceContext.getCopyOfTraceMap(), taskName);
                ThreadTraceTestResult response = restTemplate.getForObject(uri, ThreadTraceTestResult.class);
                log.info("done   call: {} - {}", index, TraceContext.getCopyOfTraceMap());
                response.setParentContext(parentContext);
                response.setOutputContext(TraceContext.getCopyOfTraceMap());
                log.info("End for {} for {} with {} -- End - {}", Thread.currentThread().getName(), taskName, response.getOutputContext(), taskName);
                return response;
            }
        }
    }

    public static class TestResult {
        List<ThreadTraceTestResult> results = new ArrayList<>();

        public TestResult() {
        }

        public List<ThreadTraceTestResult> getResults() {
            return results;
        }
    }

    public static class ThreadTraceTestResult {
        String taskName;
        Map<String, String> parentContext = new HashMap<>();
        Map<String, String> inputContext = new HashMap<>();
        Map<String, String> outputContext = new HashMap<>();
        Map<String, String> mdcContext = new HashMap<>();

        public ThreadTraceTestResult() {
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public Map<String, String> getParentContext() {
            return parentContext;
        }

        public void setParentContext(Map<String, String> parentContext) {
            this.parentContext = parentContext;
        }

        public Map<String, String> getInputContext() {
            return inputContext;
        }

        public void setInputContext(Map<String, String> inputContext) {
            this.inputContext = inputContext;
        }

        public Map<String, String> getOutputContext() {
            return outputContext;
        }

        public void setOutputContext(Map<String, String> outputContext) {
            this.outputContext = outputContext;
        }

        public Map<String, String> getMdcContext() {
            return mdcContext;
        }

        public void setMdcContext(Map<String, String> mdcContext) {
            this.mdcContext = mdcContext;
        }
    }
}
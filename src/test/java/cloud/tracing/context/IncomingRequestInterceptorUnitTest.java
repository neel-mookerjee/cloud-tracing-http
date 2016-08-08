package cloud.tracing.context;

import cloud.tracing.instrument.rest.HttpServletRequestExtractor;
import cloud.tracing.instrument.rest.IncomingRequestTracingHandler;
import cloud.tracing.instrument.rest.WebRequestInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.junit.Assert.*;

/**
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */
public class IncomingRequestInterceptorUnitTest {
    private static final String TEST_REQ_ID = "1e89268d-c70c-4a04-9617-reqid";
    private static final String TEST_SESSION_ID = "1e89268d-c70c-4a04-9617-sessionid";
    private static final String TEST_TRACE_ID = "1e89268d-c70c-4a04-9617-traceid";
    private static final String TEST_ACCOUNT_ID = "accountid";
    private static final String TEST_CUSTOMER_ID = "customerid";
    private static final String TEST_ORDER_ID = "orderid";
    private static final String TEST_TOKEN = "token";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private WebRequestInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        TraceContext.clearTraces();
        request = new MockHttpServletRequest();
        request.setMethod(WebContentGenerator.METHOD_GET);
        response = new MockHttpServletResponse();
        interceptor = new WebRequestInterceptor(new IncomingRequestTracingHandler(
                new HttpServletRequestExtractor(), new EventTracer(new CreateTrace(), new ResetTrace())),
                new EventTraceCleaner(new EventTracer(new CreateTrace(), new ResetTrace())));
    }

    @After
    public void cleanup() {
        TraceContext.clearTraces();
    }

    @Test
    public void basic() throws Exception {
        // check empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check empty MDC
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));

        // empty headers
        interceptor.preHandle(request, response, null);

        // check TraceContext - trace id and request id generated
        assertNotNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNotNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        // but rest will be null
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check MDC - trace id and request id generated (as in TraceContext)
        assertEquals(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        // but rest will be 'NONE' in MDC
        //assertEquals(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()), EventTraceLogUtil.EMPTY_TRACE_DEFAULT_LOG_VALUE);
        assertEquals(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()), EventTraceLogUtil.EMPTY_TRACE_DEFAULT_LOG_VALUE);
        assertEquals(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()), EventTraceLogUtil.EMPTY_TRACE_DEFAULT_LOG_VALUE);
        assertEquals(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()), EventTraceLogUtil.EMPTY_TRACE_DEFAULT_LOG_VALUE);

        interceptor.postHandle(request, response, null, null);

        // TraceContext is clean
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check empty MDC
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));
    }

    @Test
    public void withHeaders() throws Exception {
        // empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // check empty MDC
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));

        // set headers
        request.addHeader(TraceContext.Keys.SESSION_ID.getKey(), TEST_SESSION_ID);
        request.addHeader(TraceContext.Keys.REQUEST_ID.getKey(), TEST_REQ_ID);
        request.addHeader(TraceContext.Keys.TRACE_ID.getKey(), TEST_TRACE_ID);
        request.addHeader(TraceContext.Keys.CUSTOMER_ID.getKey(), TEST_CUSTOMER_ID);
        request.addHeader(TraceContext.Keys.ACCOUNT_ID.getKey(), TEST_ACCOUNT_ID);
        request.addHeader(TraceContext.Keys.ORDER_ID.getKey(), TEST_ORDER_ID);
        request.addHeader(TraceContext.Keys.TOKEN.getKey(), TEST_TOKEN);

        interceptor.preHandle(request, response, null);

        // verify TraceContext
        assertEquals(TraceContext.getTrace(TraceContext.Keys.SESSION_ID), TEST_SESSION_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID), TEST_REQ_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.TRACE_ID), TEST_TRACE_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID), TEST_ACCOUNT_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID), TEST_CUSTOMER_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.ORDER_ID), TEST_ORDER_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.TOKEN), TEST_TOKEN);

        // check MDC - must match with TraceContext
        //assertEquals(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertEquals(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()), TraceContext.getTrace(TraceContext.Keys.ORDER_ID));

        interceptor.postHandle(request, response, null, null);

        // TraceContext is clean
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // MDC is empty
        assertNull(MDC.get(EventTraceLogUtil.Keys.REQUEST_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.TRACE_ID.getDisplayKey()));
        //assertNull(MDC.get(EventTraceLogUtil.Keys.SESSION_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.CUSTOMER_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ACCOUNT_ID.getDisplayKey()));
        assertNull(MDC.get(EventTraceLogUtil.Keys.ORDER_ID.getDisplayKey()));
    }
}

package cloud.tracing.context;

import cloud.tracing.instrument.rest.HttpServletRequestExtractor;
import cloud.tracing.instrument.rest.IncomingRequestTracingHandler;
import cloud.tracing.instrument.rest.WebRequestInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */
public class IncomingRequestInterceptorTraceContextOverwritingIssueUnitTest {
    private static final String TEST_REQ_ID = "1e89268d-c70c-4a04-9617-reqid";
    private static final String TEST_SESSION_ID = "1e89268d-c70c-4a04-9617-sessionid";
    private static final String TEST_TRACE_ID = "1e89268d-c70c-4a04-9617-traceid";
    private static final String TEST_ACCOUNT_ID = "accountid";
    private static final String TEST_CUSTOMER_ID = "customerid";
    private static final String TEST_ORDER_ID = "orderid";
    private static final String TEST_TOKEN = "token";
    private static final String TEST_ORDER_ID_MOD = "orderid_mod";
    private static final String TEST_TOKEN_MOD = "token_mod";

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
    public void addingTraceContextAfterSettingHeadersAndBeforeInterception() throws Exception {
        // empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // set headers
        request.addHeader(TraceContext.Keys.SESSION_ID.getKey(), TEST_SESSION_ID);
        request.addHeader(TraceContext.Keys.REQUEST_ID.getKey(), TEST_REQ_ID);
        request.addHeader(TraceContext.Keys.TRACE_ID.getKey(), TEST_TRACE_ID);
        request.addHeader(TraceContext.Keys.CUSTOMER_ID.getKey(), TEST_CUSTOMER_ID);
        request.addHeader(TraceContext.Keys.ACCOUNT_ID.getKey(), TEST_ACCOUNT_ID);
        request.addHeader(TraceContext.Keys.ORDER_ID.getKey(), TEST_ORDER_ID);
        request.addHeader(TraceContext.Keys.TOKEN.getKey(), TEST_TOKEN);

        // set values to TraceContext for params
        TraceContext.addTrace(TraceContext.Keys.ORDER_ID, TEST_ORDER_ID_MOD);
        TraceContext.addTrace(TraceContext.Keys.TOKEN, TEST_TOKEN_MOD);

        interceptor.preHandle(request, response, null);

        // verify TraceContext with latest and modified values
        assertEquals(TraceContext.getTrace(TraceContext.Keys.SESSION_ID), TEST_SESSION_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID), TEST_REQ_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.TRACE_ID), TEST_TRACE_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID), TEST_ACCOUNT_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID), TEST_CUSTOMER_ID);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.ORDER_ID), TEST_ORDER_ID_MOD);
        assertEquals(TraceContext.getTrace(TraceContext.Keys.TOKEN), TEST_TOKEN_MOD);

        interceptor.postHandle(request, response, null, null);

        // TraceContext is clean
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));
    }

    @Test
    public void addingTraceContextBeforeInterception() throws Exception {
        // empty TraceContext
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));

        // set values to TraceContext for params
        TraceContext.addTrace(TraceContext.Keys.TOKEN, TEST_TOKEN_MOD);

        interceptor.preHandle(request, response, null);

        // verify TraceContext with latest and modified values
        assertNotNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNotNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertEquals(TraceContext.getTrace(TraceContext.Keys.TOKEN), TEST_TOKEN_MOD);

        interceptor.postHandle(request, response, null, null);

        // TraceContext is clean
        assertNull(TraceContext.getTrace(TraceContext.Keys.REQUEST_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TRACE_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.SESSION_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.CUSTOMER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ACCOUNT_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.ORDER_ID));
        assertNull(TraceContext.getTrace(TraceContext.Keys.TOKEN));
    }

}

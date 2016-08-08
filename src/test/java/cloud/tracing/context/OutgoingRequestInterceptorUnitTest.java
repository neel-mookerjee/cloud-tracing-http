package cloud.tracing.context;

import cloud.tracing.instrument.rest.HttpRequestInjector;
import cloud.tracing.instrument.rest.OutgoingRequestTracingHandler;
import cloud.tracing.instrument.rest.RestHttpClientInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */
public class OutgoingRequestInterceptorUnitTest {
    private static final String TEST_REQ_ID = "1e89268d-c70c-4a04-9617-reqid";
    private static final String TEST_SESSION_ID = "1e89268d-c70c-4a04-9617-sessionid";
    private static final String TEST_TRACE_ID = "1e89268d-c70c-4a04-9617-traceid";
    private static final String TEST_ACCOUNT_ID = "accountid";
    private static final String TEST_CUSTOMER_ID = "customerid";
    private static final String TEST_ORDER_ID = "orderid";
    private static final String TEST_ORDER_ID_2 = "orderid2";
    private static final String TEST_TOKEN = "token";

    private InterceptingClientHttpRequestFactory requestFactory;
    private RequestFactoryMock requestFactoryMock;
    private RequestMock requestMock;
    private ResponseMock responseMock;

    @Before
    public void setUp() throws Exception {
        TraceContext.clearTraces();
        requestFactoryMock = new RequestFactoryMock();
        requestMock = new RequestMock();
        responseMock = new ResponseMock();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RestHttpClientInterceptor(new OutgoingRequestTracingHandler(new HttpRequestInjector(), new EventTracer(new CreateTrace(), new ResetTrace()))));
        requestFactory = new InterceptingClientHttpRequestFactory(requestFactoryMock, interceptors);
    }

    @After
    public void cleanup() {
        TraceContext.clearTraces();
    }

    @Test
    public void basic() throws Exception {
        ClientHttpRequest request = requestFactory.createRequest(new URI("http://example.com"), HttpMethod.GET);

        // no attribute in incoming header
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.REQUEST_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.SESSION_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.TRACE_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.CUSTOMER_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.ACCOUNT_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.ORDER_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.TOKEN.getKey()));

        request.execute();

        // only request id gets generated
        assertNotNull(request.getHeaders().getFirst(TraceContext.Keys.REQUEST_ID.getKey()));

        // all other fields have null
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.SESSION_ID.getKey()));
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.TRACE_ID.getKey()));
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.CUSTOMER_ID.getKey()));
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.ACCOUNT_ID.getKey()));
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.ORDER_ID.getKey()));
        assertNull(request.getHeaders().getFirst(TraceContext.Keys.TOKEN.getKey()));
    }

    @Test
    public void withHeaders() throws Exception {
        ClientHttpRequest request = requestFactory.createRequest(new URI("http://example.com"), HttpMethod.GET);

        // no attribute in incoming header
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.REQUEST_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.SESSION_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.TRACE_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.CUSTOMER_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.ACCOUNT_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.ORDER_ID.getKey()));
        assertFalse(request.getHeaders().containsKey(TraceContext.Keys.TOKEN.getKey()));

        // set TraceContext
        TraceContext.addTrace(TraceContext.Keys.SESSION_ID, TEST_SESSION_ID);
        TraceContext.addTrace(TraceContext.Keys.TRACE_ID, TEST_TRACE_ID);
        TraceContext.addTrace(TraceContext.Keys.REQUEST_ID, TEST_REQ_ID);
        TraceContext.addTrace(TraceContext.Keys.CUSTOMER_ID, TEST_CUSTOMER_ID);
        TraceContext.addTrace(TraceContext.Keys.ACCOUNT_ID, TEST_ACCOUNT_ID);
        TraceContext.addTrace(TraceContext.Keys.ORDER_ID, TEST_ORDER_ID);
        TraceContext.addTrace(TraceContext.Keys.TOKEN, TEST_TOKEN);

        request.execute();

        // verify in outgoing request header all trace param
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.SESSION_ID.getKey()), TEST_SESSION_ID);
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.TRACE_ID.getKey()), TEST_TRACE_ID);
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.CUSTOMER_ID.getKey()), TEST_CUSTOMER_ID);
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.ACCOUNT_ID.getKey()), TEST_ACCOUNT_ID);
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.ORDER_ID.getKey()), TEST_ORDER_ID);
        assertEquals(request.getHeaders().getFirst(TraceContext.Keys.TOKEN.getKey()), TEST_TOKEN);
        // but request id is reset
        assertNotEquals(request.getHeaders().getFirst(TraceContext.Keys.REQUEST_ID.getKey()), TEST_REQ_ID);
    }

    private static class ResponseMock implements ClientHttpResponse {
        private HttpStatus statusCode = HttpStatus.OK;
        private String statusText = "";
        private HttpHeaders headers = new HttpHeaders();

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return statusCode;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return statusCode.value();
        }

        @Override
        public String getStatusText() throws IOException {
            return statusText;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public InputStream getBody() throws IOException {
            return null;
        }

        @Override
        public void close() {
        }
    }

    private class RequestFactoryMock implements ClientHttpRequestFactory {
        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            requestMock.setURI(uri);
            requestMock.setMethod(httpMethod);
            return requestMock;
        }
    }

    private class RequestMock implements ClientHttpRequest {
        private URI uri;
        private HttpMethod method;
        private HttpHeaders headers = new HttpHeaders();
        private ByteArrayOutputStream body = new ByteArrayOutputStream();
        private boolean executed = false;

        private RequestMock() {
        }

        @Override
        public URI getURI() {
            return uri;
        }

        public void setURI(URI uri) {
            this.uri = uri;
        }

        @Override
        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public OutputStream getBody() throws IOException {
            return body;
        }

        @Override
        public ClientHttpResponse execute() throws IOException {
            executed = true;
            return responseMock;
        }
    }
}
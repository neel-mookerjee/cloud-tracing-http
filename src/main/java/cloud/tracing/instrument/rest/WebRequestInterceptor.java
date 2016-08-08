package cloud.tracing.instrument.rest;

import cloud.tracing.TraceCleaner;
import cloud.tracing.TracingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A {@link HandlerInterceptorAdapter} to intercept incoming http calls
 *
 * @author arghanil.mukhopadhya
 * @since 0.0.1
 */

public class WebRequestInterceptor extends HandlerInterceptorAdapter {
    private static Logger log = LoggerFactory.getLogger(WebRequestInterceptor.class);
    protected final TracingHandler tracingHandler;
    protected final TraceCleaner traceCleaner;

    public WebRequestInterceptor(TracingHandler tracingHandler, TraceCleaner traceCleaner) {
        super();
        this.traceCleaner = traceCleaner;
        this.tracingHandler = tracingHandler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        log.debug("Intercepting incoming call");
        tracingHandler.handle(request);
        log.debug("Intercepted incoming call");
        return true;
    }

    /*
     * Must clean up the Trace Context
     */
    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView)
            throws Exception {
        removeTrace();
    }

    /*
     * Must clean up the Trace Context
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        removeTrace();
        super.afterCompletion(request, response, handler, ex);
    }

    private void removeTrace() {
        log.debug("cleaning up traces");
        traceCleaner.cleanupTrace();
        log.debug("cleaned up traces");
    }
}

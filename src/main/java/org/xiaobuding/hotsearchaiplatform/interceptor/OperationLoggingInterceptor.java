package org.xiaobuding.hotsearchaiplatform.interceptor;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.*;
@Component
public class OperationLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(OperationLoggingInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        logger.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Request completed: {} {} - Duration: {}ms", request.getMethod(), request.getRequestURI(), duration);
    }
}

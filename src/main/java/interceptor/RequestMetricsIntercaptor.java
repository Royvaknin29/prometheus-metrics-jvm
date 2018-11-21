package interceptor;

import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import io.prometheus.client.Histogram;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestMetricsIntercaptor extends HandlerInterceptorAdapter {

    String METRICS_REQUEST_START_TIME_ATTRIBUTE = "METRICS_REQUEST_START_TIME";

    static final Histogram histogram = Histogram.build().buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1, 5, 15, 30, 60, 120).name("http_request_duration_seconds").help("Duration of HTTP requests in seconds.").labelNames("method", "route", "code").register();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(METRICS_REQUEST_START_TIME_ATTRIBUTE, startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(METRICS_REQUEST_START_TIME_ATTRIBUTE);
        String[] path = normalizeUrl((String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).split("/");
        double durationInSeconds = (double) (System.currentTimeMillis() - startTime) / 1000;
        histogram.labels(request.getMethod(), "/" + path[path.length - 1], "" + response.getStatus()).observe(durationInSeconds);
    }

    private String normalizeUrl(String url) {
        String normalizedUrl = url.toLowerCase();
        if (normalizedUrl.charAt(normalizedUrl.length() - 1) == '/') {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        return normalizedUrl;
    }
}

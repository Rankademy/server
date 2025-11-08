package maruhxn.rankademy.adapter.webapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String REQUEST_METHOD_MDC_KEY = "requestMethod";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final List<String> URL_WHITELIST = List.of(
            "/favicon.ico",
            "/**/swagger-ui/**",
            "/**/v3/api-docs/**"
    );

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return URL_WHITELIST.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper req = wrapRequest(request);
        ContentCachingResponseWrapper res = wrapResponse(response);

        String traceId = StringUtils.hasText(req.getHeader(REQUEST_ID_HEADER))
                ? req.getHeader(REQUEST_ID_HEADER)
                : UUID.randomUUID().toString();
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        MDC.put(REQUEST_URI_MDC_KEY, resolveFullUri(req));
        MDC.put(REQUEST_METHOD_MDC_KEY, req.getMethod());
        res.setHeader(REQUEST_ID_HEADER, traceId);

        long startTime = System.currentTimeMillis();
        logRequestLineAndHeaders(req);

        Exception error = null;
        try {
            filterChain.doFilter(req, res);
        } catch (Exception ex) {
            error = ex;
            throw ex;
        } finally {
            logCompletion(req, res, startTime, error);
            res.copyBodyToResponse();
            MDC.clear();
        }
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper cached) {
            return cached;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper cached) {
            return cached;
        }
        return new ContentCachingResponseWrapper(response);
    }

    private void logRequestLineAndHeaders(ContentCachingRequestWrapper request) {
        String fullUri = resolveFullUri(request);

        String remoteIp = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer = request.getHeader(HttpHeaders.REFERER);

        log.info(
                "[{}] uri={} remoteIp={} forwardedFor={} userAgent={} referer={} contentType={} contentLength={}",
                request.getMethod(),
                fullUri,
                remoteIp,
                forwardedFor,
                userAgent,
                referer,
                request.getContentType(),
                request.getContentLengthLong()
        );
    }

    private void logCompletion(
            ContentCachingRequestWrapper req,
            ContentCachingResponseWrapper res,
            long startTime,
            Exception exception
    ) {
        long duration = System.currentTimeMillis() - startTime;
        int status = res.getStatus();
        int responseSize = res.getContentSize();

        String requestPayload = extractPayload(
                req.getContentAsByteArray(),
                resolveCharset(req.getCharacterEncoding()),
                req.getContentType()
        );

        String responsePayload = extractPayload(
                res.getContentAsByteArray(),
                resolveCharset(res.getCharacterEncoding()),
                res.getContentType()
        );

        if (StringUtils.hasText(requestPayload)) {
            log.debug("Request payload={}", requestPayload);
        }

        if (exception == null) {
            log.info(
                    "Completed request status={} duration={}ms responseSize={} contentType={} payload={}",
                    status,
                    duration,
                    responseSize,
                    res.getContentType(),
                    responsePayload
            );
        } else {
            log.error(
                    "Request failed status={} duration={}ms responseSize={} payload={} contentType={} error={}",
                    status,
                    duration,
                    responseSize,
                    res.getContentType(),
                    responsePayload,
                    exception.getMessage(),
                    exception
            );
        }
    }

    private Charset resolveCharset(String encoding) {
        if (!StringUtils.hasText(encoding)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException ex) {
            return StandardCharsets.UTF_8;
        }
    }

    private String extractPayload(byte[] content, Charset charset, String contentType) {
        if (content == null || content.length == 0) {
            return "";
        }

        if (!isReadableContentType(contentType)) {
            return "[binary content omitted]";
        }


        return new String(content, charset);
    }

    private boolean isReadableContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        String lowerCase = contentType.toLowerCase();
        return lowerCase.startsWith("text")
                || lowerCase.contains("json")
                || lowerCase.contains("xml")
                || lowerCase.contains("form")
                || lowerCase.contains("javascript")
                || lowerCase.contains("html");
    }

    private String resolveFullUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return query == null ? uri : uri + '?' + query;
    }
}

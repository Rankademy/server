package maruhxn.rankademy.adapter.webapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final int MAX_PAYLOAD_LENGTH = 2000;

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
        ContentCachingRequestWrapper requestWrapper = wrapRequest(request);
        ContentCachingResponseWrapper responseWrapper = wrapResponse(response);

        String traceId = resolveTraceId(requestWrapper);
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        attachTraceHeader(responseWrapper, traceId);

        long startTime = System.currentTimeMillis();
        logIncomingRequest(requestWrapper, traceId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            logCompletion(requestWrapper, responseWrapper, traceId, startTime, null);
            responseWrapper.copyBodyToResponse();
        } catch (Exception ex) {
            logCompletion(requestWrapper, responseWrapper, traceId, startTime, ex);
            responseWrapper.copyBodyToResponse();
            throw ex;
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
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

    private void attachTraceHeader(HttpServletResponse response, String traceId) {
        if (!response.containsHeader(REQUEST_ID_HEADER)) {
            response.addHeader(REQUEST_ID_HEADER, traceId);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerTraceId = request.getHeader(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(headerTraceId)) {
            headerTraceId = request.getHeader(CORRELATION_ID_HEADER);
            return headerTraceId;
        }
        return UUID.randomUUID().toString();
    }

    private void logIncomingRequest(ContentCachingRequestWrapper request, String traceId) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = query == null ? uri : uri + '?' + query;

        String remoteIp = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        long contentLength = request.getContentLengthLong();

        log.info(
                "Incoming request traceId={} method={} uri={} remoteIp={} forwardedFor={} userAgent={} referer={} contentType={} contentLength={}",
                traceId,
                request.getMethod(),
                fullUri,
                remoteIp,
                forwardedFor,
                userAgent,
                referer,
                request.getContentType(),
                contentLength
        );
    }

    private void logCompletion(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            String traceId,
            long startTime,
            Exception exception
    ) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();
        int responseSize = response.getContentSize();

        if (exception == null) {
            log.info(
                    "Completed request traceId={} status={} duration={}ms responseSize={} contentType={}",
                    traceId,
                    status,
                    duration,
                    responseSize,
                    response.getContentType()
            );
        } else {
            log.error(
                    "Request failed traceId={} status={} duration={}ms responseSize={} contentType={} error={}",
                    traceId,
                    status,
                    duration,
                    responseSize,
                    response.getContentType(),
                    exception.getMessage(),
                    exception
            );
        }

        String requestPayload = extractPayload(
                request.getContentAsByteArray(),
                resolveCharset(request.getCharacterEncoding()),
                request.getContentType()
        );

        if (StringUtils.hasText(requestPayload)) {
            log.info("Request payload traceId={} payload={}", traceId, requestPayload);
        }

        String responsePayload = extractPayload(
                response.getContentAsByteArray(),
                resolveCharset(response.getCharacterEncoding()),
                response.getContentType()
        );

        if (StringUtils.hasText(responsePayload)) {
            log.info("Response payload traceId={} payload={}", traceId, responsePayload);
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

        int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);
        String payload = new String(content, 0, length, charset);

        if (content.length > MAX_PAYLOAD_LENGTH) {
            payload = payload + "...(truncated)";
        }

        return payload;
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
}

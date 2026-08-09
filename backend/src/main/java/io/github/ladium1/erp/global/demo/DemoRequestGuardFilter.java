package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** reset/write, upload, login/write rate 경계를 요청 본문 파싱 전에 적용한다. */
public class DemoRequestGuardFilter extends OncePerRequestFilter {

    private static final String STATUS_PATH = "/api/v1/demo/status";
    private static final RequestMatcher LOGIN_REQUEST =
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login");
    private static final Pattern CODE_RULE_PREVIEW_PATH =
            Pattern.compile("^/api/v1/code-rules/[^/]+/preview$");
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> UPLOAD_PATHS = Set.of(
            "/api/v1/files",
            "/api/v1/drive/files",
            "/api/v1/customers/excel/upload",
            "/api/v1/sales-contacts/excel/upload"
    );

    private final DemoProperties properties;
    private final DemoProtectionPolicy protectionPolicy;
    private final DemoRateLimiter rateLimiter;
    private final ObjectProvider<HandlerExceptionResolver> exceptionResolver;

    public DemoRequestGuardFilter(
            DemoProperties properties,
            DemoProtectionPolicy protectionPolicy,
            DemoRateLimiter rateLimiter,
            ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        this.properties = properties;
        this.protectionPolicy = protectionPolicy;
        this.rateLimiter = rateLimiter;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String path = servletPath(request);
            String method = request.getMethod().toUpperCase(Locale.ROOT);

            if ("GET".equals(method) && STATUS_PATH.equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!WRITE_METHODS.contains(method)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (LOGIN_REQUEST.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            if ("POST".equals(method) && CODE_RULE_PREVIEW_PATH.matcher(path).matches()) {
                filterChain.doFilter(request, response);
                return;
            }

            protectionPolicy.assertWriteAvailable();
            if (isUploadRequest(request, path)) {
                protectionPolicy.assertUploadAllowed();
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                requireRateLimit(
                        "write",
                        authentication.getName(),
                        properties.getRateLimit().getWriteLimit(),
                        response
                );
            }
            filterChain.doFilter(request, response);
        } catch (BusinessException guarded) {
            exceptionResolver.getObject().resolveException(request, response, null, guarded);
        }
    }

    private void requireRateLimit(String namespace, String identity, int limit, HttpServletResponse response) {
        if (!rateLimiter.tryAcquire(namespace, identity, limit)) {
            response.setHeader("Retry-After", Long.toString(properties.getRateLimit().getWindow().toSeconds()));
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
    }

    private boolean isUploadRequest(HttpServletRequest request, String path) {
        String contentType = request.getContentType();
        return UPLOAD_PATHS.contains(path)
                || (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/"));
    }

    private static String servletPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }

}

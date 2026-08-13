package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/** JWT/계정 DB 조회 전 공개 API의 process-wide ingress 비용을 제한한다. */
public final class DemoIngressGuardFilter extends OncePerRequestFilter {

    private static final String GLOBAL_IDENTITY = "demo-global";
    private static final RequestMatcher API_REQUEST =
            PathPatternRequestMatcher.withDefaults().matcher("/api/**");
    private static final RequestMatcher STATUS_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/demo/status"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/demo/status")
    );

    private final DemoProperties properties;
    private final DemoRateLimiter rateLimiter;
    private final DemoRequestConcurrencyLimiter concurrencyLimiter;
    private final ObjectProvider<HandlerExceptionResolver> exceptionResolver;

    public DemoIngressGuardFilter(
            DemoProperties properties,
            DemoRateLimiter rateLimiter,
            DemoRequestConcurrencyLimiter concurrencyLimiter,
            ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.concurrencyLimiter = concurrencyLimiter;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled() || !isProtectedApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        DemoRequestConcurrencyLimiter.Lease lease = null;
        try {
            DemoProperties.RateLimit limits = properties.getRateLimit();
            String identity = trustedPeerAddress(request);
            if (!rateLimiter.tryAcquireBoth(
                    "ingress",
                    identity,
                    limits.getIngressLimit(),
                    "ingress-global",
                    GLOBAL_IDENTITY,
                    limits.getIngressGlobalLimit(),
                    1,
                    limits.getWindow()
            )) {
                response.setHeader("Retry-After", Long.toString(limits.getWindow().toSeconds()));
                throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
            }
            lease = concurrencyLimiter.tryAcquireIngress();
            if (lease == null) {
                response.setHeader("Retry-After", "1");
                throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
            }
            filterChain.doFilter(request, response);
        } catch (BusinessException guarded) {
            exceptionResolver.getObject().resolveException(request, response, null, guarded);
        } finally {
            if (lease != null) {
                lease.close();
            }
        }
    }

    private static boolean isProtectedApiRequest(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        return !"OPTIONS".equals(method)
                && API_REQUEST.matches(request)
                && !STATUS_REQUEST.matches(request);
    }

    /** Caddy가 외부 값을 덮어쓴 단일 IP만 신뢰한다. 쉼표·hostname·잘못된 값은 peer로 fail-closed 한다. */
    private static String trustedPeerAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank() && !forwarded.contains(",") && isIpLiteral(forwarded.trim())) {
            return forwarded.trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }

    private static boolean isIpLiteral(String value) {
        if (value.length() > 45 || (!value.contains(":") && !value.matches("[0-9.]+"))) {
            return false;
        }
        try {
            InetAddress.getByName(value);
            return true;
        } catch (UnknownHostException invalid) {
            return false;
        }
    }

}

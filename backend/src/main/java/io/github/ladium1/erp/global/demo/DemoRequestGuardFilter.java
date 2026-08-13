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
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** startup 검증, reset/write, rate/concurrency 경계를 요청 본문 파싱 전에 적용한다. */
public class DemoRequestGuardFilter extends OncePerRequestFilter {

    private static final String STATUS_PATH = "/api/v1/demo/status";
    private static final RequestMatcher LOGIN_REQUEST =
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login");
    private static final RequestMatcher DOWNLOAD_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/drive/files/{id}/download"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/drive/files/{id}/download"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/boards/{id}/attachments/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/boards/{id}/attachments/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/approvals/{id}/attachments/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/approvals/{id}/attachments/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/expenses/{id}/receipts/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/expenses/{id}/receipts/{fileId}"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/customers/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/customers/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/customers/excel/template"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/customers/excel/template"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/sales-contacts/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/sales-contacts/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/sales-contacts/excel/template"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/sales-contacts/excel/template"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/contracts/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/contracts/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/employees/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/employees/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/equipments/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/equipments/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/v1/after-services/excel"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/v1/after-services/excel")
    );
    private static final String GLOBAL_IDENTITY = "demo-global";
    private static final RequestMatcher CODE_RULE_PREVIEW_REQUEST =
            PathPatternRequestMatcher.pathPattern(
                    HttpMethod.POST,
                    "/api/v1/code-rules/{target}/preview"
            );
    private static final RequestMatcher API_READ_REQUEST = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/**"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.HEAD, "/api/**")
    );
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> UPLOAD_PATHS = Set.of(
            "/api/v1/files",
            "/api/v1/drive/files",
            "/api/v1/customers/excel/upload",
            "/api/v1/sales-contacts/excel/upload"
    );
    private static final Set<String> EXCEL_UPLOAD_PATHS = Set.of(
            "/api/v1/customers/excel/upload",
            "/api/v1/sales-contacts/excel/upload"
    );

    private final DemoProperties properties;
    private final DemoStartupVerificationGate startupVerificationGate;
    private final DemoProtectionPolicy protectionPolicy;
    private final DemoRateLimiter rateLimiter;
    private final DemoTransferLimiter transferLimiter;
    private final DemoRequestConcurrencyLimiter requestConcurrencyLimiter;
    private final ObjectProvider<HandlerExceptionResolver> exceptionResolver;

    public DemoRequestGuardFilter(
            DemoProperties properties,
            DemoStartupVerificationGate startupVerificationGate,
            DemoProtectionPolicy protectionPolicy,
            DemoRateLimiter rateLimiter,
            DemoTransferLimiter transferLimiter,
            DemoRequestConcurrencyLimiter requestConcurrencyLimiter,
            ObjectProvider<HandlerExceptionResolver> exceptionResolver
    ) {
        this.properties = properties;
        this.startupVerificationGate = startupVerificationGate;
        this.protectionPolicy = protectionPolicy;
        this.rateLimiter = rateLimiter;
        this.transferLimiter = transferLimiter;
        this.requestConcurrencyLimiter = requestConcurrencyLimiter;
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

        DemoTransferLimiter.Lease transferLease = null;
        DemoRequestConcurrencyLimiter.Lease requestLease = null;
        try {
            String path = servletPath(request);
            String method = request.getMethod().toUpperCase(Locale.ROOT);

            if (isMultipartRequest(request) && !isAllowedMultipartUpload(method, path)) {
                throw new BusinessException(DemoErrorCode.DEMO_UNSUPPORTED_MULTIPART);
            }

            if ("GET".equals(method) && STATUS_PATH.equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (isAllowedMultipartUpload(method, path) && !isAuthenticated(authentication)) {
                exceptionResolver.getObject().resolveException(
                        request,
                        response,
                        null,
                        new InsufficientAuthenticationException("인증된 demo upload 계정이 필요합니다.")
                );
                return;
            }
            if (DOWNLOAD_REQUEST.matches(request)) {
                if (isAuthenticated(authentication)) {
                    requirePairedRateLimit(
                            "download",
                            authentication.getName(),
                            properties.getRateLimit().getDownloadLimit(),
                            "download-global",
                            properties.getRateLimit().getDownloadGlobalLimit(),
                            response
                    );
                    transferLease = transferLimiter.tryAcquireDownload(authentication.getName());
                    requireTransferLease(transferLease, response);
                }
                filterChain.doFilter(request, response);
                return;
            }
            if (CODE_RULE_PREVIEW_REQUEST.matches(request)) {
                if (isAuthenticated(authentication)) {
                    requirePairedRateLimit(
                            "preview",
                            authentication.getName(),
                            properties.getRateLimit().getPreviewLimit(),
                            "preview-global",
                            properties.getRateLimit().getPreviewGlobalLimit(),
                            response
                    );
                    requestLease = requestConcurrencyLimiter.tryAcquirePreview();
                    requirePermit(requestLease, response);
                }
                filterChain.doFilter(request, response);
                return;
            }
            if (API_READ_REQUEST.matches(request)) {
                if (isAuthenticated(authentication)) {
                    requirePairedRateLimit(
                            "read",
                            authentication.getName(),
                            properties.getRateLimit().getReadLimit(),
                            "read-global",
                            properties.getRateLimit().getReadGlobalLimit(),
                            response
                    );
                    requestLease = requestConcurrencyLimiter.tryAcquireRead();
                    requirePermit(requestLease, response);
                }
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
            startupVerificationGate.assertWriteReady();
            protectionPolicy.assertWriteAvailable();
            if (isUploadRequest(request, path)) {
                protectionPolicy.assertUploadAllowed();
            }

            if (isAuthenticated(authentication)) {
                requirePairedRateLimit(
                        "write",
                        authentication.getName(),
                        properties.getRateLimit().getWriteLimit(),
                        "write-global",
                        properties.getRateLimit().getWriteGlobalLimit(),
                        response
                );
                requestLease = requestConcurrencyLimiter.tryAcquireWrite();
                requirePermit(requestLease, response);
                if (isUploadRequest(request, path)) {
                    if (EXCEL_UPLOAD_PATHS.contains(path)) {
                        requirePairedRateLimit(
                                "excel-upload",
                                authentication.getName(),
                                properties.getRateLimit().getExcelUploadLimit(),
                                "excel-upload-global",
                                properties.getRateLimit().getExcelUploadGlobalLimit(),
                                response
                        );
                    } else {
                        requirePairedRateLimit(
                                "upload",
                                authentication.getName(),
                                properties.getRateLimit().getUploadLimit(),
                                "upload-global",
                                properties.getRateLimit().getUploadGlobalLimit(),
                                response
                        );
                    }
                    transferLease = transferLimiter.tryAcquireUpload(authentication.getName());
                    requireTransferLease(transferLease, response);
                }
            }
            filterChain.doFilter(request, response);
        } catch (BusinessException guarded) {
            exceptionResolver.getObject().resolveException(request, response, null, guarded);
        } finally {
            if (transferLease != null) {
                transferLease.close();
            }
            if (requestLease != null) {
                requestLease.close();
            }
        }
    }

    private void requireRateLimit(String namespace, String identity, int limit, HttpServletResponse response) {
        if (!rateLimiter.tryAcquire(namespace, identity, limit)) {
            response.setHeader("Retry-After", Long.toString(properties.getRateLimit().getWindow().toSeconds()));
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
    }

    private void requirePairedRateLimit(
            String accountNamespace,
            String accountIdentity,
            int accountLimit,
            String globalNamespace,
            int globalLimit,
            HttpServletResponse response
    ) {
        if (!rateLimiter.tryAcquireBoth(
                accountNamespace,
                accountIdentity,
                accountLimit,
                globalNamespace,
                GLOBAL_IDENTITY,
                globalLimit,
                1,
                properties.getRateLimit().getWindow()
        )) {
            response.setHeader("Retry-After", Long.toString(properties.getRateLimit().getWindow().toSeconds()));
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
    }

    private void requireTransferLease(
            DemoTransferLimiter.Lease transferLease,
            HttpServletResponse response
    ) {
        if (transferLease == null) {
            response.setHeader("Retry-After", "1");
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
    }

    private void requirePermit(Object lease, HttpServletResponse response) {
        if (lease == null) {
            response.setHeader("Retry-After", "1");
            throw new BusinessException(DemoErrorCode.DEMO_RATE_LIMIT_EXCEEDED);
        }
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !authentication.getName().isBlank();
    }

    private boolean isUploadRequest(HttpServletRequest request, String path) {
        return UPLOAD_PATHS.contains(path)
                || isMultipartRequest(request);
    }

    private static boolean isAllowedMultipartUpload(String method, String path) {
        return "POST".equals(method) && UPLOAD_PATHS.contains(path);
    }

    private static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private static String servletPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }

}

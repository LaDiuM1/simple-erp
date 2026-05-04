package io.github.ladium1.erp.global.audit.internal.aspect;

import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.audit.internal.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditableAspect {

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final AuditService auditService;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "returned")
    public void recordAfterReturning(JoinPoint joinPoint, Auditable auditable, Object returned) {
        auditService.record(
                auditable.menu(),
                auditable.action(),
                resolveActorLoginId(),
                resolveActorId(),
                auditable.targetType(),
                resolveTargetId(joinPoint, auditable, returned),
                MDC.get(TRACE_ID_KEY),
                resolveIpAddress()
        );
    }

    private static String resolveActorLoginId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return auth.getName();
        }
        return SYSTEM_ACTOR;
    }

    // JwtTokenProvider 가 principal 에 username 만 저장 -> 추후 확장 시 채움
    private static Long resolveActorId() {
        return null;
    }

    private static Long resolveTargetId(JoinPoint joinPoint, Auditable auditable, Object returned) {
        if (auditable.targetIdFromReturn() && returned instanceof Number n) {
            return n.longValue();
        }
        if (!auditable.targetIdParam().isBlank()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramNames[i].equals(auditable.targetIdParam()) && args[i] instanceof Number n) {
                        return n.longValue();
                    }
                }
            }
        }
        return null;
    }

    private static String resolveIpAddress() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

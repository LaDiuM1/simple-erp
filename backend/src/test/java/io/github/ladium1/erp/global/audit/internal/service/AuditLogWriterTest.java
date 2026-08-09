package io.github.ladium1.erp.global.audit.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import io.github.ladium1.erp.global.audit.internal.repository.AuditLogRepository;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.menu.Menu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditLogWriterTest {

    @Test
    @DisplayName("감사 로그 트랜잭션의 커밋 실패가 원 업무로 전파되지 않는다")
    void commit_failure_is_swallowed_by_facade() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditLogWriter target = new AuditLogWriter(repository);
        TransactionInterceptor interceptor = new TransactionInterceptor(
                (TransactionManager) new FailingCommitTransactionManager(),
                new AnnotationTransactionAttributeSource()
        );
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(interceptor);
        AuditLogWriter transactionalWriter = (AuditLogWriter) proxyFactory.getProxy();
        AuditService auditService = new AuditService(transactionalWriter, mock(DemoProtectionPolicy.class));

        assertThatCode(() -> auditService.record(
                Menu.EMPLOYEES, AuditAction.CREATE,
                "admin", 12L, "Employee", 42L, null, null
        )).doesNotThrowAnyException();

        verify(repository).saveAndFlush(any(AuditLog.class));
    }

    private static final class FailingCommitTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            throw new IllegalStateException("commit failed");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }
}

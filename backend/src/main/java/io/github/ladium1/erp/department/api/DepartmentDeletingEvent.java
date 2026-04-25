package io.github.ladium1.erp.department.api;

/**
 * 부서 삭제 직전에 동기로 발행되는 이벤트.
 * 다른 모듈은 @EventListener 로 받아 사용 중이면 BusinessException 을 던져 삭제를 거부한다.
 * (모듈리스 양방향 의존을 피하기 위한 cross-module 검증 채널)
 */
public record DepartmentDeletingEvent(Long departmentId) {
}

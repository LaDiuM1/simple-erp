package io.github.ladium1.erp.role.api;

/**
 * 권한 삭제 직전에 동기로 발행되는 이벤트.
 * 다른 모듈은 @EventListener 로 받아 사용 중이면 BusinessException 을 던져 삭제를 거부한다.
 */
public record RoleDeletingEvent(Long roleId) {
}

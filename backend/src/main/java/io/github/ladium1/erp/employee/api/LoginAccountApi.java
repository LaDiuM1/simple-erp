package io.github.ladium1.erp.employee.api;

/**
 * 인증 단계에서 로그인 계정의 현재 사용 가능 여부만 확인하는 경계.
 */
public interface LoginAccountApi {

    /**
     * 계정이 존재하고 퇴사 상태가 아니면 {@code true}를 반환한다.
     * 휴직 계정은 기존 로그인 정책에 따라 사용할 수 있다.
     */
    boolean isLoginAllowed(String loginId);
}

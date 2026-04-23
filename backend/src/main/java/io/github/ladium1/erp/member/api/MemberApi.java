package io.github.ladium1.erp.member.api;

public interface MemberApi {

    /**
     * 로그인 ID로 직원의 권한(역할) 식별자 반환
     */
    Long getRoleIdByLoginId(String loginId);
}

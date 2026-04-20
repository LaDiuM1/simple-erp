package io.github.ladium1.erp.members.internal.repository;

import io.github.ladium1.erp.members.internal.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 시 퇴사자를 제외한 로그인 ID 확인
    @Query("""
           select m from Member m
           where m.loginId = :loginId
           and m.status != MemberStatus.RESIGNED
           """)
    Optional<Member> findForLoginByLoginId(String loginId);
    boolean existsByLoginId(String loginId);

}
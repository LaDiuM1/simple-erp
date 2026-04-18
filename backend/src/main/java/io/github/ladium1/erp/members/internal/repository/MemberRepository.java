package io.github.ladium1.erp.members.internal.repository;

import io.github.ladium1.erp.members.internal.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByLoginId(String loginId);

}
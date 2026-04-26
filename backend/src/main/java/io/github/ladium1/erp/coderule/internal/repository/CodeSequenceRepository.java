package io.github.ladium1.erp.coderule.internal.repository;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.internal.entity.CodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CodeSequenceRepository extends JpaRepository<CodeSequence, Long> {

    /**
     * 시퀀스 락 + 증가용 조회. 호출 트랜잭션 종료까지 동일 (target, scopeKey) 행을 직렬화한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CodeSequence s where s.target = :target and s.scopeKey = :scopeKey")
    Optional<CodeSequence> findForUpdate(@Param("target") CodeRuleTarget target,
                                         @Param("scopeKey") String scopeKey);

    Optional<CodeSequence> findByTargetAndScopeKey(CodeRuleTarget target, String scopeKey);
}

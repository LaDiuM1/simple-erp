package io.github.ladium1.erp.coderule.internal.entity;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "code_sequences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_code_sequences_target_scope",
                columnNames = {"target", "scope_key"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeSequence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50,
            comment = "채번 대상")
    private CodeRuleTarget target;

    @Column(name = "scope_key", nullable = false, length = 200,
            comment = "초기화 정책 + parentScoped 조합 키 (예: 2026, 2026-04|D001)")
    private String scopeKey;

    @Column(nullable = false,
            comment = "현재까지 발급된 시퀀스 (다음 발급 = currentSeq + 1)")
    private long currentSeq;

    @Version
    private Long version;

    private CodeSequence(CodeRuleTarget target, String scopeKey, long currentSeq) {
        this.target = target;
        this.scopeKey = scopeKey;
        this.currentSeq = currentSeq;
    }

    /**
     * 신규 시퀀스 row 생성 — currentSeq=1 (즉시 첫 번째 발급).
     */
    public static CodeSequence first(CodeRuleTarget target, String scopeKey) {
        return new CodeSequence(target, scopeKey, 1L);
    }

    public void increment() {
        this.currentSeq += 1L;
    }
}

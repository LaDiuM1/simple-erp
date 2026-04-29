package io.github.ladium1.erp.coderule.internal.entity;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "code_rules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true,
            comment = "채번 대상 (DEPARTMENT 등)")
    private CodeRuleTarget target;

    @Column(nullable = false, length = 200,
            comment = "코드 패턴 (예: D-{YYYY}-{SEQ:4}). literal 문자열 + 토큰 조합")
    private String pattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "코드 입력 방식")
    private InputMode inputMode;

    @Column(length = 500,
            comment = "사용자 메모")
    private String description;

    @Builder
    CodeRule(CodeRuleTarget target,
             String pattern,
             InputMode inputMode,
             String description) {
        this.target = target;
        this.pattern = pattern;
        this.inputMode = inputMode;
        this.description = description;
    }

    public void update(String pattern,
                       InputMode inputMode,
                       String description) {
        this.pattern = pattern;
        this.inputMode = inputMode;
        this.description = description;
    }
}

package io.github.ladium1.erp.coderule.internal.entity;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
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

    @Column(length = 50,
            comment = "{PREFIX} 토큰 치환값")
    private String prefix;

    @Column(nullable = false, length = 200,
            comment = "코드 패턴 (예: {PREFIX}-{YYYY}-{SEQ:4})")
    private String pattern;

    @Column(nullable = false,
            comment = "{SEQ} 토큰 사용 시 기본 zero-pad 자릿수")
    private Integer defaultSeqLength;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "시퀀스 초기화 주기")
    private ResetPolicy resetPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "코드 입력 방식")
    private InputMode inputMode;

    @Column(nullable = false,
            comment = "true 면 시퀀스를 부모 코드별로 분리 관리")
    private boolean parentScoped;

    @Column(length = 500,
            comment = "사용자 메모")
    private String description;

    @Builder
    CodeRule(CodeRuleTarget target,
             String prefix,
             String pattern,
             Integer defaultSeqLength,
             ResetPolicy resetPolicy,
             InputMode inputMode,
             boolean parentScoped,
             String description) {
        this.target = target;
        this.prefix = prefix;
        this.pattern = pattern;
        this.defaultSeqLength = defaultSeqLength;
        this.resetPolicy = resetPolicy;
        this.inputMode = inputMode;
        this.parentScoped = parentScoped;
        this.description = description;
    }

    public void update(String prefix,
                       String pattern,
                       Integer defaultSeqLength,
                       ResetPolicy resetPolicy,
                       InputMode inputMode,
                       boolean parentScoped,
                       String description) {
        this.prefix = prefix;
        this.pattern = pattern;
        this.defaultSeqLength = defaultSeqLength;
        this.resetPolicy = resetPolicy;
        this.inputMode = inputMode;
        this.parentScoped = parentScoped;
        this.description = description;
    }
}

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "code_rule_attribute_mappings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_carm_target_key_source",
                columnNames = {"target", "attribute_key", "source_value"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeRuleAttributeMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50,
            comment = "채번 대상")
    private CodeRuleTarget target;

    @Column(name = "attribute_key", nullable = false, length = 50,
            comment = "도메인 attribute key (대문자, 예: TYPE)")
    private String attributeKey;

    @Column(name = "source_value", nullable = false, length = 100,
            comment = "도메인 enum/분류 값 (예: GENERAL)")
    private String sourceValue;

    @Column(name = "code_value", nullable = false, length = 50,
            comment = "코드 안에 치환될 문자열 (예: G)")
    private String codeValue;

    @Builder
    CodeRuleAttributeMapping(CodeRuleTarget target, String attributeKey, String sourceValue, String codeValue) {
        this.target = target;
        this.attributeKey = attributeKey;
        this.sourceValue = sourceValue;
        this.codeValue = codeValue;
    }

    public void updateCodeValue(String codeValue) {
        this.codeValue = codeValue;
    }
}

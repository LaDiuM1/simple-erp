package io.github.ladium1.erp.coderule.internal.repository;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.internal.entity.CodeRuleAttributeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodeRuleAttributeMappingRepository extends JpaRepository<CodeRuleAttributeMapping, Long> {

    List<CodeRuleAttributeMapping> findByTarget(CodeRuleTarget target);

    Optional<CodeRuleAttributeMapping> findByTargetAndAttributeKeyAndSourceValue(
            CodeRuleTarget target, String attributeKey, String sourceValue);
}

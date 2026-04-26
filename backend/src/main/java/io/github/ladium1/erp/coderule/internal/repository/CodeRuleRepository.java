package io.github.ladium1.erp.coderule.internal.repository;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRuleRepository extends JpaRepository<CodeRule, Long> {

    Optional<CodeRule> findByTarget(CodeRuleTarget target);

    boolean existsByTarget(CodeRuleTarget target);
}

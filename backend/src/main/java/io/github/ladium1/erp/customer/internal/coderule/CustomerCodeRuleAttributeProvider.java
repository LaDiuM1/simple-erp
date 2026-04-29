package io.github.ladium1.erp.customer.internal.coderule;

import io.github.ladium1.erp.coderule.api.CodeRuleAttributeProvider;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleAttributeDescriptor;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 고객사 도메인의 분류 attribute 등록.
 * <p>
 * 고객사는 {@link CustomerType} (POTENTIAL/GENERAL/KEY_ACCOUNT/PARTNER) 분류를 가지며,
 * 사용자는 채번 규칙 화면에서 각 분류값에 매핑할 코드 prefix 를 정의할 수 있다.
 * 토큰명은 {@code {TYPE}}.
 */
@Component
public class CustomerCodeRuleAttributeProvider implements CodeRuleAttributeProvider {

    @Override
    public CodeRuleTarget target() {
        return CodeRuleTarget.CUSTOMER;
    }

    @Override
    public CodeRuleAttributeDescriptor descriptor() {
        List<CodeRuleAttributeDescriptor.AttributeValue> values = Arrays.stream(CustomerType.values())
                .map(t -> new CodeRuleAttributeDescriptor.AttributeValue(t.name(), t.getDescription()))
                .toList();
        return CodeRuleAttributeDescriptor.builder()
                .key("TYPE")
                .label("고객사 분류")
                .values(values)
                .build();
    }
}

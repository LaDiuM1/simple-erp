package io.github.ladium1.erp.coderule.internal.mapper;

import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodeRuleMapper {

    CodeRuleInfo toInfo(CodeRule rule);
}

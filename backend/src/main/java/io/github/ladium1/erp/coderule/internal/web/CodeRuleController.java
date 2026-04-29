package io.github.ladium1.erp.coderule.internal.web;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleAttributeDescriptor;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleAttributeMappingPayload;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewRequest;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleUpdateRequest;
import io.github.ladium1.erp.coderule.internal.service.CodeRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/code-rules")
@RequiredArgsConstructor
public class CodeRuleController {

    private static final String MENU_CODE = "CODE_RULES";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final CodeRuleService codeRuleService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<CodeRuleResponse> findAll() {
        return codeRuleService.findAll();
    }

    @GetMapping("/{target}")
    @PreAuthorize(CAN_READ)
    public CodeRuleResponse get(@PathVariable CodeRuleTarget target) {
        return codeRuleService.get(target);
    }

    @GetMapping("/{target}/attributes")
    @PreAuthorize(CAN_READ)
    public List<CodeRuleAttributeDescriptor> getAttributes(@PathVariable CodeRuleTarget target) {
        return codeRuleService.getAttributes(target);
    }

    @GetMapping("/{target}/attribute-mappings")
    @PreAuthorize(CAN_READ)
    public List<CodeRuleAttributeMappingPayload> getMappings(@PathVariable CodeRuleTarget target) {
        return codeRuleService.getMappings(target);
    }

    @PutMapping("/{target}")
    @PreAuthorize(CAN_WRITE)
    public CodeRuleResponse update(@PathVariable CodeRuleTarget target,
                                   @Valid @RequestBody CodeRuleUpdateRequest request) {
        return codeRuleService.update(target, request);
    }

    @PostMapping("/{target}/preview")
    @PreAuthorize(CAN_WRITE)
    public CodeRulePreviewResponse preview(@PathVariable CodeRuleTarget target,
                                           @Valid @RequestBody CodeRulePreviewRequest request) {
        return codeRuleService.previewFromRequest(target, request);
    }
}

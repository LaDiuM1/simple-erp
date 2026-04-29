package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleAttributeProvider;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleAttributeDescriptor;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleAttributeMappingPayload;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewRequest;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleUpdateRequest;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import io.github.ladium1.erp.coderule.internal.entity.CodeRuleAttributeMapping;
import io.github.ladium1.erp.coderule.internal.entity.CodeSequence;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.mapper.CodeRuleMapper;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleAttributeMappingRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeSequenceRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeRuleService implements CodeRuleApi {

    private static final int SAMPLE_COUNT = 5;

    private final CodeRuleRepository codeRuleRepository;
    private final CodeSequenceRepository codeSequenceRepository;
    private final CodeRuleAttributeMappingRepository attributeMappingRepository;
    private final CodeRuleMapper codeRuleMapper;
    private final PatternCompiler patternCompiler;
    private final ScopeKeyResolver scopeKeyResolver;
    private final List<CodeRuleAttributeProvider> attributeProviders;

    // ===== CodeRuleApi =====

    @Override
    public CodeRuleInfo getRule(CodeRuleTarget target) {
        return codeRuleMapper.toInfo(findRuleOrThrow(target));
    }

    @Override
    @Transactional
    public String generate(CodeRuleTarget target, CodeGenerationContext context) {
        CodeRule rule = findRuleOrThrow(target);
        Map<String, String> sourceAttributes = context != null ? context.attributes() : Map.of();
        String parentCode = context != null ? context.parentCode() : null;
        LocalDate now = LocalDate.now();

        Set<String> attributeKeys = attributeKeys(target);
        Set<String> usedKeys = patternCompiler.usedAttributeKeys(rule.getPattern(), attributeKeys);
        Map<String, String> codeAttributes = mapAttributesToCodeValues(target, usedKeys, sourceAttributes);

        String scopeKey = scopeKeyResolver.resolve(
                rule.getPattern(), parentCode, sourceAttributes, now, usedKeys);
        long seq = nextSequence(target, scopeKey);
        return render(rule, seq, parentCode, codeAttributes, now);
    }

    @Override
    public String preview(CodeRuleTarget target, CodeGenerationContext context) {
        CodeRule rule = findRuleOrThrow(target);
        Map<String, String> sourceAttributes = context != null ? context.attributes() : Map.of();
        String parentCode = context != null ? context.parentCode() : null;
        LocalDate now = LocalDate.now();

        Set<String> attributeKeys = attributeKeys(target);
        Set<String> usedKeys = patternCompiler.usedAttributeKeys(rule.getPattern(), attributeKeys);
        Map<String, String> codeAttributes = mapAttributesToCodeValues(target, usedKeys, sourceAttributes);

        String scopeKey = scopeKeyResolver.resolve(
                rule.getPattern(), parentCode, sourceAttributes, now, usedKeys);
        long nextSeq = codeSequenceRepository.findByTargetAndScopeKey(target, scopeKey)
                .map(CodeSequence::getCurrentSeq)
                .orElse(0L) + 1L;
        return render(rule, nextSeq, parentCode, codeAttributes, now);
    }

    @Override
    public void validate(CodeRuleTarget target, String code) {
        CodeRule rule = findRuleOrThrow(target);
        if (!patternCompiler.matches(rule.getPattern(), code)) {
            throw new BusinessException(CodeRuleErrorCode.CODE_FORMAT_MISMATCH);
        }
    }

    @Override
    public List<CodeRuleAttributeDescriptor> getAttributes(CodeRuleTarget target) {
        return providersFor(target).stream()
                .map(CodeRuleAttributeProvider::descriptor)
                .toList();
    }

    // ===== 관리 화면용 =====

    public List<CodeRuleResponse> findAll() {
        LocalDate now = LocalDate.now();
        return codeRuleRepository.findAll().stream()
                .sorted(Comparator.comparing(r -> r.getTarget().name()))
                .map(rule -> toResponse(rule, now))
                .toList();
    }

    public CodeRuleResponse get(CodeRuleTarget target) {
        return toResponse(findRuleOrThrow(target), LocalDate.now());
    }

    public List<CodeRuleAttributeMappingPayload> getMappings(CodeRuleTarget target) {
        return attributeMappingRepository.findByTarget(target).stream()
                .sorted(Comparator
                        .comparing(CodeRuleAttributeMapping::getAttributeKey)
                        .thenComparing(CodeRuleAttributeMapping::getSourceValue))
                .map(m -> new CodeRuleAttributeMappingPayload(
                        m.getAttributeKey(), m.getSourceValue(), m.getCodeValue()))
                .toList();
    }

    @Transactional
    public CodeRuleResponse update(CodeRuleTarget target, CodeRuleUpdateRequest request) {
        Set<String> attributeKeys = attributeKeys(target);
        patternCompiler.validate(request.pattern(), attributeKeys);

        CodeRule rule = findRuleOrThrow(target);
        rule.update(request.pattern(), request.inputMode(), request.description());

        if (request.attributeMappings() != null) {
            replaceMappings(target, attributeKeys, request.attributeMappings());
        }

        return toResponse(rule, LocalDate.now());
    }

    public CodeRulePreviewResponse previewFromRequest(CodeRuleTarget target, CodeRulePreviewRequest request) {
        Set<String> attributeKeys = attributeKeys(target);
        patternCompiler.validate(request.pattern(), attributeKeys);
        LocalDate now = LocalDate.now();

        Set<String> usedKeys = patternCompiler.usedAttributeKeys(request.pattern(), attributeKeys);
        Map<String, String> sourceAttributes = request.previewAttributes() != null
                ? request.previewAttributes() : Map.of();
        Map<String, String> codeAttributes = mapAttributesForPreview(
                target, usedKeys, sourceAttributes, request.attributeMappings());

        String scopeKey = scopeKeyResolver.resolve(
                request.pattern(), request.parentCode(), sourceAttributes, now, usedKeys);
        long currentSeq = codeSequenceRepository.findByTargetAndScopeKey(target, scopeKey)
                .map(CodeSequence::getCurrentSeq)
                .orElse(0L);
        String nextCode = renderRequest(request, currentSeq + 1, codeAttributes, now);
        List<String> samples = LongStream.rangeClosed(1, SAMPLE_COUNT)
                .mapToObj(seq -> renderRequest(request, seq, codeAttributes, now))
                .toList();
        return CodeRulePreviewResponse.builder()
                .nextCode(nextCode)
                .samples(samples)
                .build();
    }

    // ===== 내부 =====

    private CodeRule findRuleOrThrow(CodeRuleTarget target) {
        return codeRuleRepository.findByTarget(target)
                .orElseThrow(() -> new BusinessException(CodeRuleErrorCode.RULE_NOT_FOUND));
    }

    private List<CodeRuleAttributeProvider> providersFor(CodeRuleTarget target) {
        return attributeProviders.stream()
                .filter(p -> p.target() == target)
                .toList();
    }

    private Set<String> attributeKeys(CodeRuleTarget target) {
        Set<String> keys = new HashSet<>();
        for (CodeRuleAttributeProvider p : providersFor(target)) {
            keys.add(p.descriptor().key());
        }
        return keys;
    }

    private long nextSequence(CodeRuleTarget target, String scopeKey) {
        var existing = codeSequenceRepository.findForUpdate(target, scopeKey);
        if (existing.isPresent()) {
            existing.get().increment();
            return existing.get().getCurrentSeq();
        }
        try {
            CodeSequence created = codeSequenceRepository.saveAndFlush(CodeSequence.first(target, scopeKey));
            return created.getCurrentSeq();
        } catch (DataIntegrityViolationException duplicate) {
            CodeSequence again = codeSequenceRepository.findForUpdate(target, scopeKey)
                    .orElseThrow(() -> duplicate);
            again.increment();
            return again.getCurrentSeq();
        }
    }

    private String render(CodeRule rule, long seq, String parentCode, Map<String, String> codeAttributes, LocalDate now) {
        return patternCompiler.render(rule.getPattern(), new PatternCompiler.RenderContext(
                seq, parentCode, now, codeAttributes
        ));
    }

    private String renderRequest(CodeRulePreviewRequest request, long seq, Map<String, String> codeAttributes, LocalDate now) {
        return patternCompiler.render(request.pattern(), new PatternCompiler.RenderContext(
                seq, request.parentCode(), now, codeAttributes
        ));
    }

    private Map<String, String> mapAttributesToCodeValues(CodeRuleTarget target,
                                                          Set<String> usedKeys,
                                                          Map<String, String> sourceAttributes) {
        if (usedKeys.isEmpty()) return Map.of();
        Map<String, String> result = new HashMap<>();
        for (String key : usedKeys) {
            String source = sourceAttributes.get(key);
            if (source == null || source.isBlank()) {
                throw new BusinessException(CodeRuleErrorCode.MISSING_ATTRIBUTE_MAPPING);
            }
            String code = attributeMappingRepository
                    .findByTargetAndAttributeKeyAndSourceValue(target, key, source)
                    .map(CodeRuleAttributeMapping::getCodeValue)
                    .orElseThrow(() -> new BusinessException(CodeRuleErrorCode.MISSING_ATTRIBUTE_MAPPING));
            result.put(key, code);
        }
        return result;
    }

    private Map<String, String> mapAttributesForPreview(CodeRuleTarget target,
                                                        Set<String> usedKeys,
                                                        Map<String, String> sourceAttributes,
                                                        List<CodeRuleAttributeMappingPayload> formMappings) {
        if (usedKeys.isEmpty()) return Map.of();
        Map<String, String> result = new HashMap<>();
        for (String key : usedKeys) {
            String source = sourceAttributes.get(key);
            String code = "";
            if (source != null && !source.isBlank()) {
                code = lookupFormMapping(formMappings, key, source);
                if (code == null) {
                    code = attributeMappingRepository
                            .findByTargetAndAttributeKeyAndSourceValue(target, key, source)
                            .map(CodeRuleAttributeMapping::getCodeValue)
                            .orElse("");
                }
            }
            result.put(key, code);
        }
        return result;
    }

    private static String lookupFormMapping(List<CodeRuleAttributeMappingPayload> mappings,
                                            String key, String source) {
        if (mappings == null) return null;
        for (CodeRuleAttributeMappingPayload m : mappings) {
            if (key.equals(m.attributeKey()) && source.equals(m.sourceValue())) {
                return m.codeValue();
            }
        }
        return null;
    }

    private void replaceMappings(CodeRuleTarget target,
                                 Set<String> attributeKeys,
                                 List<CodeRuleAttributeMappingPayload> payloads) {
        for (CodeRuleAttributeMappingPayload p : payloads) {
            if (!attributeKeys.contains(p.attributeKey())) {
                throw new BusinessException(CodeRuleErrorCode.UNKNOWN_ATTRIBUTE_KEY);
            }
        }
        List<CodeRuleAttributeMapping> existing = attributeMappingRepository.findByTarget(target);
        attributeMappingRepository.deleteAll(existing);
        attributeMappingRepository.flush();
        List<CodeRuleAttributeMapping> next = new ArrayList<>();
        for (CodeRuleAttributeMappingPayload p : payloads) {
            next.add(CodeRuleAttributeMapping.builder()
                    .target(target)
                    .attributeKey(p.attributeKey())
                    .sourceValue(p.sourceValue())
                    .codeValue(p.codeValue())
                    .build());
        }
        attributeMappingRepository.saveAll(next);
    }

    private CodeRuleResponse toResponse(CodeRule rule, LocalDate now) {
        Set<String> attributeKeys = attributeKeys(rule.getTarget());
        Set<String> usedKeys = patternCompiler.usedAttributeKeys(rule.getPattern(), attributeKeys);
        boolean hasParentToken = patternCompiler.usesParentToken(rule.getPattern());
        boolean canRenderNext = usedKeys.isEmpty() && !hasParentToken;

        String nextCode = null;
        if (canRenderNext) {
            String scopeKey = scopeKeyResolver.resolve(
                    rule.getPattern(), null, Map.of(), now, Set.of());
            long nextSeq = codeSequenceRepository.findByTargetAndScopeKey(rule.getTarget(), scopeKey)
                    .map(CodeSequence::getCurrentSeq)
                    .orElse(0L) + 1L;
            nextCode = render(rule, nextSeq, null, Map.of(), now);
        }

        return CodeRuleResponse.builder()
                .id(rule.getId())
                .target(rule.getTarget())
                .targetLabel(rule.getTarget().getLabel())
                .pattern(rule.getPattern())
                .inputMode(rule.getInputMode())
                .hasParent(rule.getTarget().isHasParent())
                .description(rule.getDescription())
                .nextCode(nextCode)
                .build();
    }
}

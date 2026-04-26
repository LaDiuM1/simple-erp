package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewRequest;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleUpdateRequest;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import io.github.ladium1.erp.coderule.internal.entity.CodeSequence;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.mapper.CodeRuleMapper;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeSequenceRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeRuleService implements CodeRuleApi {

    private static final int SAMPLE_COUNT = 5;

    private final CodeRuleRepository codeRuleRepository;
    private final CodeSequenceRepository codeSequenceRepository;
    private final CodeRuleMapper codeRuleMapper;
    private final PatternCompiler patternCompiler;
    private final ScopeKeyResolver scopeKeyResolver;

    // ===== CodeRuleApi =====

    @Override
    public CodeRuleInfo getRule(CodeRuleTarget target) {
        return codeRuleMapper.toInfo(findRuleOrThrow(target));
    }

    @Override
    @Transactional
    public String generate(CodeRuleTarget target, CodeGenerationContext context) {
        CodeRule rule = findRuleOrThrow(target);
        String parentCode = context != null ? context.parentCode() : null;
        LocalDate now = LocalDate.now();
        String scopeKey = scopeKeyResolver.resolve(rule, parentCode, now);
        long seq = nextSequence(target, scopeKey);
        return render(rule, seq, parentCode, now);
    }

    @Override
    public String preview(CodeRuleTarget target, CodeGenerationContext context) {
        CodeRule rule = findRuleOrThrow(target);
        String parentCode = context != null ? context.parentCode() : null;
        LocalDate now = LocalDate.now();
        String scopeKey = scopeKeyResolver.resolve(rule, parentCode, now);
        long nextSeq = codeSequenceRepository.findByTargetAndScopeKey(target, scopeKey)
                .map(CodeSequence::getCurrentSeq)
                .orElse(0L) + 1L;
        return render(rule, nextSeq, parentCode, now);
    }

    @Override
    public void validate(CodeRuleTarget target, String code) {
        CodeRule rule = findRuleOrThrow(target);
        if (!patternCompiler.matches(rule.getPattern(), rule.getPrefix(), code)) {
            throw new BusinessException(CodeRuleErrorCode.CODE_FORMAT_MISMATCH);
        }
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

    @Transactional
    public CodeRuleResponse update(CodeRuleTarget target, CodeRuleUpdateRequest request) {
        patternCompiler.validate(request.pattern(), request.defaultSeqLength());
        CodeRule rule = findRuleOrThrow(target);
        rule.update(
                request.prefix(),
                request.pattern(),
                request.defaultSeqLength(),
                request.resetPolicy(),
                request.inputMode(),
                request.parentScoped(),
                request.description()
        );
        return toResponse(rule, LocalDate.now());
    }

    public CodeRulePreviewResponse previewFromRequest(CodeRuleTarget target, CodeRulePreviewRequest request) {
        patternCompiler.validate(request.pattern(), request.defaultSeqLength());
        LocalDate now = LocalDate.now();
        String scopeKey = scopeKeyResolver.resolve(
                request.resetPolicy(),
                request.parentScoped(),
                request.parentCode(),
                now
        );
        long currentSeq = codeSequenceRepository.findByTargetAndScopeKey(target, scopeKey)
                .map(CodeSequence::getCurrentSeq)
                .orElse(0L);
        String nextCode = renderRequest(request, currentSeq + 1, now);
        List<String> samples = LongStream.rangeClosed(1, SAMPLE_COUNT)
                .mapToObj(seq -> renderRequest(request, seq, now))
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

    /**
     * 시퀀스 atomic 증가 — PESSIMISTIC_WRITE 락 + 신규 row 경쟁 시 unique constraint 재시도.
     */
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

    private String render(CodeRule rule, long seq, String parentCode, LocalDate now) {
        return patternCompiler.render(rule.getPattern(), new PatternCompiler.RenderContext(
                rule.getPrefix(), seq, rule.getDefaultSeqLength(), parentCode, now
        ));
    }

    private String renderRequest(CodeRulePreviewRequest request, long seq, LocalDate now) {
        return patternCompiler.render(request.pattern(), new PatternCompiler.RenderContext(
                request.prefix(), seq, request.defaultSeqLength(), request.parentCode(), now
        ));
    }

    private CodeRuleResponse toResponse(CodeRule rule, LocalDate now) {
        String scopeKey = scopeKeyResolver.resolve(rule, null, now);
        long nextSeq = codeSequenceRepository.findByTargetAndScopeKey(rule.getTarget(), scopeKey)
                .map(CodeSequence::getCurrentSeq)
                .orElse(0L) + 1L;
        String nextCode = patternCompiler.usesParentToken(rule.getPattern())
                ? null
                : render(rule, nextSeq, null, now);
        return CodeRuleResponse.builder()
                .id(rule.getId())
                .target(rule.getTarget())
                .targetLabel(rule.getTarget().getLabel())
                .prefix(rule.getPrefix())
                .pattern(rule.getPattern())
                .defaultSeqLength(rule.getDefaultSeqLength())
                .resetPolicy(rule.getResetPolicy())
                .inputMode(rule.getInputMode())
                .parentScoped(rule.isParentScoped())
                .description(rule.getDescription())
                .nextCode(nextCode)
                .build();
    }
}

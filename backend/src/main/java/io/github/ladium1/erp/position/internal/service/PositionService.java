package io.github.ladium1.erp.position.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.PositionDeletingEvent;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.dto.PositionCreateRequest;
import io.github.ladium1.erp.position.internal.dto.PositionDetailResponse;
import io.github.ladium1.erp.position.internal.dto.PositionRankingRequest;
import io.github.ladium1.erp.position.internal.dto.PositionSearchCondition;
import io.github.ladium1.erp.position.internal.dto.PositionSummaryResponse;
import io.github.ladium1.erp.position.internal.dto.PositionUpdateRequest;
import io.github.ladium1.erp.position.internal.entity.Position;
import io.github.ladium1.erp.position.internal.exception.PositionErrorCode;
import io.github.ladium1.erp.position.internal.mapper.PositionMapper;
import io.github.ladium1.erp.position.internal.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService implements PositionApi {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CodeRuleApi codeRuleApi;

    @Override
    public PositionInfo getById(Long id) {
        return positionRepository.findById(id)
                .map(positionMapper::toPositionInfo)
                .orElseThrow(() -> new BusinessException(PositionErrorCode.POSITION_NOT_FOUND));
    }

    @Override
    public List<PositionInfo> findAll() {
        return positionMapper.toPositionInfos(
                positionRepository.findAll(Sort.by("rankLevel").ascending())
        );
    }

    @Override
    public List<PositionInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return positionMapper.toPositionInfos(positionRepository.findAllById(ids));
    }

    public PageResponse<PositionSummaryResponse> search(PositionSearchCondition condition, Pageable pageable) {
        Page<Position> page = positionRepository.search(condition, pageable);
        return PageResponse.of(page.map(positionMapper::toSummaryResponse));
    }

    public PositionDetailResponse getDetail(Long id) {
        return positionRepository.findById(id)
                .map(positionMapper::toDetailResponse)
                .orElseThrow(() -> new BusinessException(PositionErrorCode.POSITION_NOT_FOUND));
    }

    public List<PositionSummaryResponse> findAllByRanking() {
        return positionRepository.findAll(Sort.by("rankLevel").ascending()).stream()
                .map(positionMapper::toSummaryResponse)
                .toList();
    }

    /**
     * 사용자 입력 코드의 사용 가능 여부 — 등록 화면의 디바운스 중복 검사 용.
     */
    public boolean isCodeAvailable(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return !positionRepository.existsByCode(code.trim());
    }

    @Transactional
    public Long create(PositionCreateRequest request) {
        String code = resolveCode(request.code());

        if (positionRepository.existsByCode(code)) {
            throw new BusinessException(PositionErrorCode.DUPLICATE_CODE);
        }

        int nextRank = positionRepository.findTopByOrderByRankLevelDesc()
                .map(p -> p.getRankLevel() + 1)
                .orElse(1);

        Position position = Position.builder()
                .code(code)
                .name(request.name())
                .rankLevel(nextRank)
                .description(request.description())
                .build();

        return positionRepository.save(position).getId();
    }

    /**
     * 채번 규칙의 inputMode 에 따라 최종 코드 결정.
     * AUTO: 항상 시스템 생성 / MANUAL: 사용자 입력 필수 + 패턴 검증 / AUTO_OR_MANUAL: 입력 있으면 검증, 없으면 생성.
     */
    private String resolveCode(String requested) {
        CodeRuleInfo rule = codeRuleApi.getRule(CodeRuleTarget.POSITION);
        InputMode mode = rule.inputMode();
        boolean hasInput = requested != null && !requested.isBlank();

        if (mode == InputMode.AUTO || (mode == InputMode.AUTO_OR_MANUAL && !hasInput)) {
            return codeRuleApi.generate(CodeRuleTarget.POSITION, CodeGenerationContext.empty());
        }
        if (!hasInput) {
            throw new BusinessException(PositionErrorCode.CODE_REQUIRED);
        }
        String trimmed = requested.trim();
        codeRuleApi.validate(CodeRuleTarget.POSITION, trimmed);
        return trimmed;
    }

    @Transactional
    public void update(Long id, PositionUpdateRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PositionErrorCode.POSITION_NOT_FOUND));

        position.update(request.name(), request.description());
    }

    @Transactional
    public void delete(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new BusinessException(PositionErrorCode.POSITION_NOT_FOUND);
        }
        // 다른 모듈 (직원 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new PositionDeletingEvent(id));
        positionRepository.deleteById(id);
    }

    /**
     * 서열 일괄 재배치.
     * <p>
     * 요청 배열은 전체 직책을 모두 포함해야 한다 (중복/누락 거부). 상위→하위 순으로 1, 2, 3 ... 부여.
     */
    @Transactional
    public void reorder(PositionRankingRequest request) {
        List<Long> orderedIds = request.orderedIds();
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BusinessException(PositionErrorCode.INVALID_RANKING_PAYLOAD);
        }
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw new BusinessException(PositionErrorCode.INVALID_RANKING_PAYLOAD);
        }

        List<Position> all = positionRepository.findAll();
        if (all.size() != orderedIds.size()) {
            throw new BusinessException(PositionErrorCode.INVALID_RANKING_PAYLOAD);
        }
        Map<Long, Position> byId = all.stream()
                .collect(Collectors.toMap(Position::getId, Function.identity()));
        if (!byId.keySet().equals(uniqueIds)) {
            throw new BusinessException(PositionErrorCode.INVALID_RANKING_PAYLOAD);
        }

        int rank = 1;
        for (Long id : orderedIds) {
            byId.get(id).changeRankLevel(rank++);
        }
    }
}

package io.github.ladium1.erp.position.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleApi;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.position.api.PositionDeletingEvent;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.dto.PositionCreateRequest;
import io.github.ladium1.erp.position.internal.dto.PositionDetailResponse;
import io.github.ladium1.erp.position.internal.dto.PositionRankingRequest;
import io.github.ladium1.erp.position.internal.dto.PositionSummaryResponse;
import io.github.ladium1.erp.position.internal.dto.PositionUpdateRequest;
import io.github.ladium1.erp.position.internal.entity.Position;
import io.github.ladium1.erp.position.internal.exception.PositionErrorCode;
import io.github.ladium1.erp.position.internal.mapper.PositionMapper;
import io.github.ladium1.erp.position.internal.repository.PositionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @InjectMocks
    private PositionService positionService;

    @Mock private PositionRepository positionRepository;
    @Mock private PositionMapper positionMapper;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CodeRuleApi codeRuleApi;

    @Test
    @DisplayName("getById 성공 — Mapper 가 변환한 Info 반환")
    void get_by_id_success() {
        // given
        Position position = mockPosition("P001", "이사", 1);
        PositionInfo info = PositionInfo.builder().id(1L).code("P001").name("이사").build();
        given(positionRepository.findById(1L)).willReturn(Optional.of(position));
        given(positionMapper.toPositionInfo(position)).willReturn(info);

        // when
        PositionInfo actual = positionService.getById(1L);

        // then
        assertThat(actual).isEqualTo(info);
    }

    @Test
    @DisplayName("getById 실패 — POSITION_NOT_FOUND")
    void get_by_id_fail_not_found() {
        // given
        given(positionRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> positionService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.POSITION_NOT_FOUND);
    }

    @Test
    @DisplayName("findByIds — 빈 리스트 입력 시 빈 결과 반환 (DB 미조회)")
    void find_by_ids_empty() {
        // when
        List<PositionInfo> result = positionService.findByIds(List.of());

        // then
        assertThat(result).isEmpty();
        verify(positionRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("getDetail 성공")
    void get_detail_success() {
        // given
        Position position = mockPosition("P001", "이사", 1);
        PositionDetailResponse detail = PositionDetailResponse.builder()
                .id(1L).code("P001").name("이사").rankLevel(1).build();
        given(positionRepository.findById(1L)).willReturn(Optional.of(position));
        given(positionMapper.toDetailResponse(position)).willReturn(detail);

        // when
        PositionDetailResponse actual = positionService.getDetail(1L);

        // then
        assertThat(actual).isEqualTo(detail);
    }

    @Test
    @DisplayName("getDetail 실패 — POSITION_NOT_FOUND")
    void get_detail_fail_not_found() {
        // given
        given(positionRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> positionService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.POSITION_NOT_FOUND);
    }

    @Test
    @DisplayName("isCodeAvailable — 미사용 코드면 true")
    void is_code_available_true() {
        given(positionRepository.existsByCode("P999")).willReturn(false);
        assertThat(positionService.isCodeAvailable("P999")).isTrue();
    }

    @Test
    @DisplayName("isCodeAvailable — 사용 중 코드면 false")
    void is_code_available_false() {
        given(positionRepository.existsByCode("P001")).willReturn(true);
        assertThat(positionService.isCodeAvailable("P001")).isFalse();
    }

    @Test
    @DisplayName("isCodeAvailable — 빈 문자열은 false (DB 미조회)")
    void is_code_available_blank_returns_false() {
        assertThat(positionService.isCodeAvailable("")).isFalse();
        assertThat(positionService.isCodeAvailable("   ")).isFalse();
        assertThat(positionService.isCodeAvailable(null)).isFalse();
        verify(positionRepository, never()).existsByCode(any());
    }

    @Test
    @DisplayName("create — AUTO 모드에서 시스템이 채번한 코드로 저장 + max+1 rankLevel 자동 부여")
    void create_auto_mode_success() {
        // given
        PositionCreateRequest request = new PositionCreateRequest(null, "신규직책", "설명");
        given(codeRuleApi.getRule(CodeRuleTarget.POSITION)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.POSITION), any())).willReturn("P010");
        given(positionRepository.existsByCode("P010")).willReturn(false);
        given(positionRepository.findTopByOrderByRankLevelDesc())
                .willReturn(Optional.of(mockPosition("P006", "사원", 6)));
        Position saved = mockPosition("P010", "신규직책", 7);
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(positionRepository.save(any(Position.class))).willReturn(saved);

        // when
        Long id = positionService.create(request);

        // then
        assertThat(id).isEqualTo(100L);
    }

    @Test
    @DisplayName("create — 등록된 직책이 없으면 rankLevel 1 부터 시작")
    void create_first_position_starts_from_rank_1() {
        // given
        PositionCreateRequest request = new PositionCreateRequest(null, "최초", null);
        given(codeRuleApi.getRule(CodeRuleTarget.POSITION)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.POSITION), any())).willReturn("P001");
        given(positionRepository.existsByCode("P001")).willReturn(false);
        given(positionRepository.findTopByOrderByRankLevelDesc()).willReturn(Optional.empty());
        Position saved = mockPosition("P001", "최초", 1);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(positionRepository.save(any(Position.class))).willReturn(saved);

        // when
        Long id = positionService.create(request);

        // then
        assertThat(id).isEqualTo(1L);
        verify(positionRepository).save(any(Position.class));
    }

    @Test
    @DisplayName("create — MANUAL 모드에서 사용자 입력 코드 검증 후 저장")
    void create_manual_mode_success() {
        // given
        PositionCreateRequest request = new PositionCreateRequest("CUSTOM", "직접입력", null);
        given(codeRuleApi.getRule(CodeRuleTarget.POSITION)).willReturn(ruleWithMode(InputMode.MANUAL));
        given(positionRepository.existsByCode("CUSTOM")).willReturn(false);
        given(positionRepository.findTopByOrderByRankLevelDesc()).willReturn(Optional.empty());
        Position saved = mockPosition("CUSTOM", "직접입력", 1);
        ReflectionTestUtils.setField(saved, "id", 5L);
        given(positionRepository.save(any(Position.class))).willReturn(saved);

        // when
        Long id = positionService.create(request);

        // then
        assertThat(id).isEqualTo(5L);
        verify(codeRuleApi).validate(CodeRuleTarget.POSITION, "CUSTOM");
        verify(codeRuleApi, never()).generate(any(), any());
    }

    @Test
    @DisplayName("create 실패 — MANUAL 모드인데 코드 미입력 시 CODE_REQUIRED")
    void create_manual_mode_missing_code() {
        // given
        PositionCreateRequest request = new PositionCreateRequest(null, "이름", null);
        given(codeRuleApi.getRule(CodeRuleTarget.POSITION)).willReturn(ruleWithMode(InputMode.MANUAL));

        // when & then
        assertThatThrownBy(() -> positionService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.CODE_REQUIRED);
    }

    @Test
    @DisplayName("create 실패 — 중복 코드 시 DUPLICATE_CODE")
    void create_fail_duplicate_code() {
        // given
        PositionCreateRequest request = new PositionCreateRequest(null, "신규", null);
        given(codeRuleApi.getRule(CodeRuleTarget.POSITION)).willReturn(ruleWithMode(InputMode.AUTO));
        given(codeRuleApi.generate(eq(CodeRuleTarget.POSITION), any())).willReturn("P001");
        given(positionRepository.existsByCode("P001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> positionService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.DUPLICATE_CODE);
        verify(positionRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 엔티티의 update 호출")
    void update_success() {
        // given
        Position position = mockPosition("P001", "이사", 1);
        given(positionRepository.findById(1L)).willReturn(Optional.of(position));
        PositionUpdateRequest request = new PositionUpdateRequest("대표이사", "최상위 직책");

        // when
        positionService.update(1L, request);

        // then
        assertThat(position.getName()).isEqualTo("대표이사");
        assertThat(position.getDescription()).isEqualTo("최상위 직책");
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 직책")
    void update_fail_not_found() {
        // given
        given(positionRepository.findById(99L)).willReturn(Optional.empty());
        PositionUpdateRequest request = new PositionUpdateRequest("이름", null);

        // when & then
        assertThatThrownBy(() -> positionService.update(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.POSITION_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — PositionDeletingEvent 발행 후 deleteById 호출")
    void delete_success() {
        // given
        given(positionRepository.existsById(1L)).willReturn(true);

        // when
        positionService.delete(1L);

        // then
        verify(eventPublisher).publishEvent(new PositionDeletingEvent(1L));
        verify(positionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 직책 (이벤트 미발행)")
    void delete_fail_not_found() {
        // given
        given(positionRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> positionService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.POSITION_NOT_FOUND);
        verify(eventPublisher, never()).publishEvent(any());
        verify(positionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("reorder 성공 — 요청 순서대로 1, 2, 3 재부여")
    void reorder_success() {
        // given
        Position p1 = mockPosition("P001", "이사", 1);
        Position p2 = mockPosition("P002", "부장", 2);
        Position p3 = mockPosition("P003", "과장", 3);
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p3, "id", 3L);
        given(positionRepository.findAll()).willReturn(List.of(p1, p2, p3));

        // 부장 → 이사 → 과장 순으로 재배치
        PositionRankingRequest request = new PositionRankingRequest(List.of(2L, 1L, 3L));

        // when
        positionService.reorder(request);

        // then
        assertThat(p2.getRankLevel()).isEqualTo(1);
        assertThat(p1.getRankLevel()).isEqualTo(2);
        assertThat(p3.getRankLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("reorder 실패 — 빈 ID 배열")
    void reorder_fail_empty() {
        PositionRankingRequest request = new PositionRankingRequest(List.of());

        assertThatThrownBy(() -> positionService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.INVALID_RANKING_PAYLOAD);
    }

    @Test
    @DisplayName("reorder 실패 — 중복된 ID 포함")
    void reorder_fail_duplicate_ids() {
        PositionRankingRequest request = new PositionRankingRequest(List.of(1L, 2L, 1L));

        assertThatThrownBy(() -> positionService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.INVALID_RANKING_PAYLOAD);
    }

    @Test
    @DisplayName("reorder 실패 — DB 의 직책 수와 요청 ID 수 불일치")
    void reorder_fail_size_mismatch() {
        // given — DB 에는 3건, 요청에는 2건
        Position p1 = mockPosition("P001", "이사", 1);
        Position p2 = mockPosition("P002", "부장", 2);
        Position p3 = mockPosition("P003", "과장", 3);
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p3, "id", 3L);
        given(positionRepository.findAll()).willReturn(List.of(p1, p2, p3));
        PositionRankingRequest request = new PositionRankingRequest(List.of(1L, 2L));

        // when & then
        assertThatThrownBy(() -> positionService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.INVALID_RANKING_PAYLOAD);
    }

    @Test
    @DisplayName("reorder 실패 — DB 에 없는 ID 가 섞여 있음")
    void reorder_fail_unknown_id() {
        // given — DB 에는 1, 2, 3 인데 요청에는 1, 2, 999
        Position p1 = mockPosition("P001", "이사", 1);
        Position p2 = mockPosition("P002", "부장", 2);
        Position p3 = mockPosition("P003", "과장", 3);
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p3, "id", 3L);
        given(positionRepository.findAll()).willReturn(List.of(p1, p2, p3));
        PositionRankingRequest request = new PositionRankingRequest(List.of(1L, 2L, 999L));

        // when & then
        assertThatThrownBy(() -> positionService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", PositionErrorCode.INVALID_RANKING_PAYLOAD);
    }

    @Test
    @DisplayName("findAllByRanking — rankLevel 오름차순으로 SummaryResponse 리스트 반환")
    void find_all_by_ranking_returns_summaries() {
        // given
        Position p1 = mockPosition("P001", "이사", 1);
        Position p2 = mockPosition("P002", "부장", 2);
        given(positionRepository.findAll(any(Sort.class)))
                .willReturn(List.of(p1, p2));
        PositionSummaryResponse s1 = PositionSummaryResponse.builder().id(1L).code("P001").name("이사").rankLevel(1).build();
        PositionSummaryResponse s2 = PositionSummaryResponse.builder().id(2L).code("P002").name("부장").rankLevel(2).build();
        given(positionMapper.toSummaryResponse(p1)).willReturn(s1);
        given(positionMapper.toSummaryResponse(p2)).willReturn(s2);

        // when
        List<PositionSummaryResponse> result = positionService.findAllByRanking();

        // then
        assertThat(result).containsExactly(s1, s2);
    }

    private Position mockPosition(String code, String name, int rankLevel) {
        return Position.builder()
                .code(code)
                .name(name)
                .rankLevel(rankLevel)
                .description(null)
                .build();
    }

    private CodeRuleInfo ruleWithMode(InputMode mode) {
        return CodeRuleInfo.builder()
                .target(CodeRuleTarget.POSITION)
                .pattern("P{SEQ:3}")
                .inputMode(mode)
                .description("테스트")
                .build();
    }
}

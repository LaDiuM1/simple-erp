package io.github.ladium1.erp.afterservice.internal.service;

import io.github.ladium1.erp.afterservice.internal.dto.EngineerRequest;
import io.github.ladium1.erp.afterservice.internal.dto.EngineerResponse;
import io.github.ladium1.erp.afterservice.internal.entity.Engineer;
import io.github.ladium1.erp.afterservice.internal.entity.EngineerType;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.repository.AfterServiceRepository;
import io.github.ladium1.erp.afterservice.internal.repository.EngineerRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceExpenseRepository;
import io.github.ladium1.erp.afterservice.internal.repository.ServiceVisitRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EngineerServiceTest {

    @InjectMocks
    private EngineerService engineerService;

    @Mock private EngineerRepository engineerRepository;
    @Mock private AfterServiceRepository afterServiceRepository;
    @Mock private ServiceVisitRepository visitRepository;
    @Mock private ServiceExpenseRepository expenseRepository;
    @Mock private EmployeeApi employeeApi;

    @Test
    @DisplayName("findAll — 구분 / 이름 순 목록 반환")
    void find_all_success() {
        // given
        given(engineerRepository.findAllByOrderByTypeAscNameAsc())
                .willReturn(List.of(mockEngineer("박기술", EngineerType.INTERNAL)));

        // when
        List<EngineerResponse> engineers = engineerService.findAll();

        // then
        assertThat(engineers).hasSize(1);
        assertThat(engineers.getFirst().name()).isEqualTo("박기술");
        assertThat(engineers.getFirst().type()).isEqualTo(EngineerType.INTERNAL);
    }

    @Test
    @DisplayName("findNamesByIds — 빈 입력은 빈 맵 (DB 미조회)")
    void find_names_by_ids_empty_input() {
        assertThat(engineerService.findNamesByIds(List.of())).isEmpty();
        assertThat(engineerService.findNamesByIds(null)).isEmpty();
        verify(engineerRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("findNamesByIds — id → 이름 매핑 반환")
    void find_names_by_ids_success() {
        // given
        Engineer engineer = mockEngineer("김기사", EngineerType.OUTSOURCED);
        ReflectionTestUtils.setField(engineer, "id", 5L);
        given(engineerRepository.findAllById(List.of(5L))).willReturn(List.of(engineer));

        // when
        Map<Long, String> names = engineerService.findNamesByIds(List.of(5L));

        // then
        assertThat(names).containsEntry(5L, "김기사");
    }

    @Test
    @DisplayName("create 성공 — 이름 trim 후 저장")
    void create_success() {
        // given
        EngineerRequest request = new EngineerRequest(" 김기사 ", EngineerType.OUTSOURCED, "문영테크", null, null, true);
        Engineer saved = mockEngineer("김기사", EngineerType.OUTSOURCED);
        ReflectionTestUtils.setField(saved, "id", 5L);
        given(engineerRepository.save(any(Engineer.class))).willReturn(saved);

        // when
        Long id = engineerService.create(request);

        // then
        assertThat(id).isEqualTo(5L);
        verify(employeeApi, never()).getById(any());
    }

    @Test
    @DisplayName("create — 내부 엔지니어의 직원 링크는 존재 검증")
    void create_internal_validates_employee_link() {
        // given
        EngineerRequest request = new EngineerRequest("박기술", EngineerType.INTERNAL, "기술부", null, 3L, true);
        given(employeeApi.getById(3L)).willReturn(EmployeeInfo.builder().id(3L).name("박기술").build());
        given(engineerRepository.save(any(Engineer.class))).willReturn(mockEngineer("박기술", EngineerType.INTERNAL));

        // when
        engineerService.create(request);

        // then
        verify(employeeApi).getById(3L);
    }

    @Test
    @DisplayName("update 성공 — 필드 반영")
    void update_success() {
        // given
        Engineer engineer = mockEngineer("김기사", EngineerType.OUTSOURCED);
        given(engineerRepository.findById(5L)).willReturn(Optional.of(engineer));
        EngineerRequest request = new EngineerRequest("김기사", EngineerType.OUTSOURCED, "금광이엔지", "010-1234-5678", null, false);

        // when
        engineerService.update(5L, request);

        // then
        assertThat(engineer.getAffiliation()).isEqualTo("금광이엔지");
        assertThat(engineer.isActive()).isFalse();
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 엔지니어")
    void update_fail_not_found() {
        given(engineerRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> engineerService.update(99L,
                new EngineerRequest("김기사", EngineerType.OUTSOURCED, null, null, null, true)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.ENGINEER_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 참조 없는 엔지니어")
    void delete_success() {
        // given
        given(engineerRepository.existsById(5L)).willReturn(true);
        given(afterServiceRepository.existsByAssignedEngineerId(5L)).willReturn(false);
        given(visitRepository.existsByEngineerId(5L)).willReturn(false);
        given(expenseRepository.existsByEngineerId(5L)).willReturn(false);

        // when
        engineerService.delete(5L);

        // then
        verify(engineerRepository).deleteById(5L);
    }

    @Test
    @DisplayName("delete 실패 — 방문 일지가 참조 중이면 ENGINEER_IN_USE")
    void delete_fail_in_use() {
        // given
        given(engineerRepository.existsById(5L)).willReturn(true);
        given(afterServiceRepository.existsByAssignedEngineerId(5L)).willReturn(false);
        given(visitRepository.existsByEngineerId(5L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> engineerService.delete(5L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.ENGINEER_IN_USE);
        verify(engineerRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("validateId 실패 — ENGINEER_NOT_FOUND")
    void validate_id_fail_not_found() {
        given(engineerRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> engineerService.validateId(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.ENGINEER_NOT_FOUND);
    }

    private Engineer mockEngineer(String name, EngineerType type) {
        return Engineer.builder()
                .name(name)
                .type(type)
                .affiliation("문영테크")
                .active(true)
                .build();
    }
}

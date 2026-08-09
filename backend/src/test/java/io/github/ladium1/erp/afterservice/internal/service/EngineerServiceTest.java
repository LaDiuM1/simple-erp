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
        Engineer engineer = Engineer.builder()
                .name("박기술")
                .type(EngineerType.INTERNAL)
                .affiliation("기술부")
                .employeeId(3L)
                .active(true)
                .build();
        given(engineerRepository.findAllByOrderByTypeAscNameAsc())
                .willReturn(List.of(engineer));
        given(employeeApi.findByIds(List.of(3L))).willReturn(List.of(
                EmployeeInfo.builder().id(3L).name("박기술").build()));

        // when
        List<EngineerResponse> engineers = engineerService.findAll();

        // then
        assertThat(engineers).hasSize(1);
        assertThat(engineers.getFirst().name()).isEqualTo("박기술");
        assertThat(engineers.getFirst().type()).isEqualTo(EngineerType.INTERNAL);
        assertThat(engineers.getFirst().employeeName()).isEqualTo("박기술");
        verify(employeeApi).findByIds(List.of(3L));
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
        verify(employeeApi, never()).isEligibleForNewWorkReference(any());
    }

    @Test
    @DisplayName("create — 내부 엔지니어의 직원 링크는 존재 검증")
    void create_internal_validates_employee_link() {
        // given
        EngineerRequest request = new EngineerRequest("박기술", EngineerType.INTERNAL, "기술부", null, 3L, true);
        given(employeeApi.isEligibleForNewWorkReference(3L)).willReturn(true);
        given(engineerRepository.save(any(Engineer.class))).willReturn(mockEngineer("박기술", EngineerType.INTERNAL));

        // when
        engineerService.create(request);

        // then
        verify(employeeApi).isEligibleForNewWorkReference(3L);
    }

    @Test
    @DisplayName("create 실패 — 퇴사자와 복구 운영 계정은 내부 엔지니어로 연결 불가")
    void create_rejects_inactive_or_recovery_operator_link() {
        EngineerRequest request = new EngineerRequest(
                "복구 운영자", EngineerType.INTERNAL, "기술부", null, 3L, true);
        given(employeeApi.isEligibleForNewWorkReference(3L)).willReturn(false);

        assertThatThrownBy(() -> engineerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
        verify(engineerRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 외부 엔지니어에는 직원 링크를 둘 수 없음")
    void create_rejects_external_employee_link() {
        EngineerRequest request = new EngineerRequest(
                "김기사", EngineerType.OUTSOURCED, "협력사", null, 3L, true);

        assertThatThrownBy(() -> engineerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
        verify(employeeApi, never()).isEligibleForNewWorkReference(any());
    }

    @Test
    @DisplayName("create 실패 — 내부 엔지니어는 재직 직원 링크 필수")
    void create_rejects_internal_without_employee_link() {
        EngineerRequest request = new EngineerRequest(
                "박기술", EngineerType.INTERNAL, "기술부", null, null, true);

        assertThatThrownBy(() -> engineerService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
        verify(employeeApi, never()).isEligibleForNewWorkReference(any());
        verify(engineerRepository, never()).save(any());
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
    @DisplayName("update 성공 — 기존 직원 연결은 재직 상태가 바뀌어도 유지")
    void update_keeps_existing_ineligible_employee_reference() {
        Engineer engineer = Engineer.builder()
                .name("박기술")
                .type(EngineerType.INTERNAL)
                .affiliation("기술부")
                .employeeId(3L)
                .active(true)
                .build();
        given(engineerRepository.findById(5L)).willReturn(Optional.of(engineer));

        engineerService.update(5L,
                new EngineerRequest("박기술", EngineerType.INTERNAL, "AS부", null, 3L, true));

        assertThat(engineer.getAffiliation()).isEqualTo("AS부");
        verify(employeeApi, never()).isEligibleForNewWorkReference(any());
    }

    @Test
    @DisplayName("update 실패 — 비활성 직원으로 연결을 변경할 수 없음")
    void update_rejects_new_ineligible_employee_reference() {
        Engineer engineer = Engineer.builder()
                .name("박기술")
                .type(EngineerType.INTERNAL)
                .affiliation("기술부")
                .employeeId(3L)
                .active(true)
                .build();
        given(engineerRepository.findById(5L)).willReturn(Optional.of(engineer));
        given(employeeApi.isEligibleForNewWorkReference(4L)).willReturn(false);

        assertThatThrownBy(() -> engineerService.update(5L,
                new EngineerRequest("박기술", EngineerType.INTERNAL, "기술부", null, 4L, true)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", AfterServiceErrorCode.INVALID_ENGINEER_EMPLOYEE);
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
    @DisplayName("validateWorkReference 실패 — 존재하지 않는 엔지니어")
    void validate_work_reference_fail_not_found() {
        given(engineerRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> engineerService.validateWorkReference(99L, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.ENGINEER_NOT_FOUND);
    }

    @Test
    @DisplayName("validateWorkReference 실패 — 비활성 엔지니어에 새 참조 생성 불가")
    void validate_work_reference_rejects_inactive_engineer() {
        Engineer engineer = mockEngineer("김기사", EngineerType.OUTSOURCED);
        ReflectionTestUtils.setField(engineer, "id", 5L);
        ReflectionTestUtils.setField(engineer, "active", false);
        given(engineerRepository.findById(5L)).willReturn(Optional.of(engineer));

        assertThatThrownBy(() -> engineerService.validateWorkReference(5L, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AfterServiceErrorCode.INACTIVE_ENGINEER);
    }

    @Test
    @DisplayName("validateWorkReference 성공 — 기존 비활성 엔지니어 참조는 유지 가능")
    void validate_work_reference_keeps_existing_inactive_engineer() {
        Engineer engineer = mockEngineer("김기사", EngineerType.OUTSOURCED);
        ReflectionTestUtils.setField(engineer, "id", 5L);
        ReflectionTestUtils.setField(engineer, "active", false);
        given(engineerRepository.findById(5L)).willReturn(Optional.of(engineer));

        engineerService.validateWorkReference(5L, 5L);

        verify(engineerRepository).findById(5L);
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

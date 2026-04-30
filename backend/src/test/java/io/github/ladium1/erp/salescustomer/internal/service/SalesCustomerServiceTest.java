package io.github.ladium1.erp.salescustomer.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentTerminateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerAggregate;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerDetailResponse;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivity;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
import io.github.ladium1.erp.salescustomer.internal.exception.SalesCustomerErrorCode;
import io.github.ladium1.erp.salescustomer.internal.mapper.SalesCustomerMapper;
import io.github.ladium1.erp.salescustomer.internal.repository.AggregatedActivityCount;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesActivityRepository;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalesCustomerServiceTest {

    @InjectMocks
    private SalesCustomerService salesCustomerService;

    @Mock private SalesActivityRepository activityRepository;
    @Mock private SalesAssignmentRepository assignmentRepository;
    @Mock private SalesCustomerMapper salesCustomerMapper;
    @Mock private CustomerApi customerApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private io.github.ladium1.erp.salescontact.api.SalesContactApi salesContactApi;

    @Test
    @DisplayName("getDetail 성공 — customer + activities + assignments 통합 반환")
    void get_detail_success() {
        // given
        given(customerApi.getById(1L)).willReturn(
                CustomerInfo.builder().id(1L).code("C0001").name("대성상사").build());
        given(activityRepository.findByCustomerIdOrderByActivityDateDesc(1L)).willReturn(List.of());
        given(assignmentRepository.findByCustomerIdOrderByEndDateAscStartDateDesc(1L)).willReturn(List.of());
        given(employeeApi.findByIds(any())).willReturn(List.of());
        given(salesContactApi.findByIds(any())).willReturn(List.of());

        // when
        SalesCustomerDetailResponse detail = salesCustomerService.getDetail(1L);

        // then
        assertThat(detail.customerId()).isEqualTo(1L);
        assertThat(detail.customerCode()).isEqualTo("C0001");
        assertThat(detail.customerName()).isEqualTo("대성상사");
        assertThat(detail.activities()).isEmpty();
        assertThat(detail.assignments()).isEmpty();
    }

    @Test
    @DisplayName("aggregateByCustomerIds — 빈 입력 시 즉시 빈 리스트 (DB 미조회)")
    void aggregate_empty() {
        assertThat(salesCustomerService.aggregateByCustomerIds(List.of())).isEmpty();
        verify(activityRepository, never()).aggregateByCustomerIds(any());
    }

    @Test
    @DisplayName("aggregateByCustomerIds — 활동 카운트 + 활성 담당자 + primary 담당자명 매핑")
    void aggregate_success() {
        // given
        Long customerId = 1L;
        AggregatedActivityCount activityCount = new AggregatedActivityCount(
                customerId, 5L, LocalDateTime.of(2026, 4, 20, 10, 0));
        given(activityRepository.aggregateByCustomerIds(List.of(customerId)))
                .willReturn(List.of(activityCount));

        SalesAssignment primaryAssignment = mockAssignment(customerId, 10L, true);
        SalesAssignment secondaryAssignment = mockAssignment(customerId, 11L, false);
        given(assignmentRepository.findByCustomerIdInAndEndDateIsNull(List.of(customerId)))
                .willReturn(List.of(primaryAssignment, secondaryAssignment));

        EmployeeInfo primaryEmployee = EmployeeInfo.builder()
                .id(10L).loginId("primary").name("김주담").build();
        EmployeeInfo secondaryEmployee = EmployeeInfo.builder()
                .id(11L).loginId("secondary").name("이부담").build();
        given(employeeApi.findByIds(List.of(10L, 11L)))
                .willReturn(List.of(primaryEmployee, secondaryEmployee));

        // when
        List<SalesCustomerAggregate> result = salesCustomerService.aggregateByCustomerIds(List.of(customerId));

        // then
        assertThat(result).hasSize(1);
        SalesCustomerAggregate agg = result.getFirst();
        assertThat(agg.customerId()).isEqualTo(customerId);
        assertThat(agg.primaryAssigneeId()).isEqualTo(10L);
        assertThat(agg.primaryAssigneeName()).isEqualTo("김주담");
        assertThat(agg.activeAssigneeCount()).isEqualTo(2L);
        assertThat(agg.activityCount()).isEqualTo(5L);
        assertThat(agg.lastActivityDate()).isEqualTo(LocalDateTime.of(2026, 4, 20, 10, 0));
    }

    @Test
    @DisplayName("createActivity 성공 — customer / employee 무결성 검증 후 저장")
    void create_activity_success() {
        // given
        SalesActivityCreateRequest request = baseActivityCreateRequest(1L, 10L);
        SalesActivity saved = mockActivity();
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(activityRepository.save(any(SalesActivity.class))).willReturn(saved);

        // when
        Long id = salesCustomerService.createActivity(request);

        // then
        assertThat(id).isEqualTo(100L);
        verify(customerApi).getById(1L);
        verify(employeeApi).getById(10L);
    }

    @Test
    @DisplayName("updateActivity 실패 — 존재하지 않는 활동")
    void update_activity_fail_not_found() {
        given(activityRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                salesCustomerService.updateActivity(99L, baseActivityUpdateRequest(10L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.ACTIVITY_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteActivity 실패 — 존재하지 않는 활동")
    void delete_activity_fail_not_found() {
        given(activityRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> salesCustomerService.deleteActivity(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.ACTIVITY_NOT_FOUND);
        verify(activityRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("createAssignment 성공 — primary 신규 시 기존 primary 자동 해제")
    void create_assignment_clears_existing_primary() {
        // given
        SalesAssignment existingPrimary = mockAssignment(1L, 99L, true);
        given(assignmentRepository.findByCustomerIdAndPrimaryTrueAndEndDateIsNull(1L))
                .willReturn(List.of(existingPrimary));
        SalesAssignment saved = mockAssignment(1L, 10L, true);
        ReflectionTestUtils.setField(saved, "id", 200L);
        given(assignmentRepository.save(any(SalesAssignment.class))).willReturn(saved);

        SalesAssignmentCreateRequest request = new SalesAssignmentCreateRequest(
                1L, 10L, LocalDate.of(2026, 4, 1), true, "신규 주담당 배정");

        // when
        Long id = salesCustomerService.createAssignment(request);

        // then
        assertThat(id).isEqualTo(200L);
        assertThat(existingPrimary.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("createAssignment — primary 가 false 면 기존 primary 그대로 유지")
    void create_assignment_keeps_existing_primary() {
        // given
        SalesAssignmentCreateRequest request = new SalesAssignmentCreateRequest(
                1L, 10L, LocalDate.of(2026, 4, 1), false, "보조 담당");
        SalesAssignment saved = mockAssignment(1L, 10L, false);
        ReflectionTestUtils.setField(saved, "id", 200L);
        given(assignmentRepository.save(any(SalesAssignment.class))).willReturn(saved);

        // when
        salesCustomerService.createAssignment(request);

        // then
        verify(assignmentRepository, never()).findByCustomerIdAndPrimaryTrueAndEndDateIsNull(any());
    }

    @Test
    @DisplayName("updateAssignment 실패 — 이미 종료된 배정")
    void update_assignment_fail_already_terminated() {
        SalesAssignment terminated = SalesAssignment.builder()
                .customerId(1L).employeeId(10L)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .primary(false).reason("종료").build();
        given(assignmentRepository.findById(7L)).willReturn(Optional.of(terminated));

        SalesAssignmentUpdateRequest request = new SalesAssignmentUpdateRequest(
                LocalDate.of(2024, 1, 1), true, "변경");

        assertThatThrownBy(() -> salesCustomerService.updateAssignment(7L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.ASSIGNMENT_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("terminateAssignment 성공 — endDate / reason 설정, primary 해제")
    void terminate_assignment_success() {
        // given
        SalesAssignment active = mockAssignment(1L, 10L, true);
        given(assignmentRepository.findById(7L)).willReturn(Optional.of(active));
        SalesAssignmentTerminateRequest request = new SalesAssignmentTerminateRequest(
                LocalDate.of(2026, 4, 30), "이직");

        // when
        salesCustomerService.terminateAssignment(7L, request);

        // then
        assertThat(active.isActive()).isFalse();
        assertThat(active.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(active.getReason()).isEqualTo("이직");
        assertThat(active.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("terminateAssignment 실패 — endDate < startDate")
    void terminate_assignment_fail_invalid_end_date() {
        SalesAssignment active = SalesAssignment.builder()
                .customerId(1L).employeeId(10L)
                .startDate(LocalDate.of(2026, 5, 1))
                .primary(true).reason(null).build();
        given(assignmentRepository.findById(7L)).willReturn(Optional.of(active));

        SalesAssignmentTerminateRequest request = new SalesAssignmentTerminateRequest(
                LocalDate.of(2026, 4, 1), "잘못 입력");

        assertThatThrownBy(() -> salesCustomerService.terminateAssignment(7L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.INVALID_END_DATE);
    }

    @Test
    @DisplayName("terminateAssignment 실패 — 이미 종료된 배정")
    void terminate_assignment_fail_already_terminated() {
        SalesAssignment terminated = SalesAssignment.builder()
                .customerId(1L).employeeId(10L)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .primary(false).reason("종료").build();
        given(assignmentRepository.findById(7L)).willReturn(Optional.of(terminated));

        SalesAssignmentTerminateRequest request = new SalesAssignmentTerminateRequest(
                LocalDate.of(2026, 4, 30), "이직");

        assertThatThrownBy(() -> salesCustomerService.terminateAssignment(7L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.ASSIGNMENT_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("deleteAssignment 실패 — 존재하지 않는 배정")
    void delete_assignment_fail_not_found() {
        given(assignmentRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> salesCustomerService.deleteAssignment(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesCustomerErrorCode.ASSIGNMENT_NOT_FOUND);
    }

    private SalesActivity mockActivity() {
        return SalesActivity.builder()
                .customerId(1L)
                .type(SalesActivityType.VISIT)
                .activityDate(LocalDateTime.now())
                .subject("미팅")
                .ourEmployeeId(10L)
                .build();
    }

    private SalesAssignment mockAssignment(Long customerId, Long employeeId, boolean primary) {
        return SalesAssignment.builder()
                .customerId(customerId)
                .employeeId(employeeId)
                .startDate(LocalDate.of(2026, 1, 1))
                .primary(primary)
                .reason("테스트")
                .build();
    }

    private SalesActivityCreateRequest baseActivityCreateRequest(Long customerId, Long employeeId) {
        return new SalesActivityCreateRequest(
                customerId, SalesActivityType.VISIT, LocalDateTime.now(),
                "신규 미팅", "내용", employeeId, null
        );
    }

    private SalesActivityUpdateRequest baseActivityUpdateRequest(Long employeeId) {
        return new SalesActivityUpdateRequest(
                SalesActivityType.CALL, LocalDateTime.now(),
                "수정된 제목", "수정된 내용", employeeId, null
        );
    }
}
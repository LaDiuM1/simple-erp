package io.github.ladium1.erp.salescontact.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSummaryResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.exception.SalesContactErrorCode;
import io.github.ladium1.erp.salescontact.internal.mapper.SalesContactMapper;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactSourceRepository;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SalesContactServiceTest {

    @InjectMocks
    private SalesContactService salesContactService;

    @Mock private SalesContactRepository contactRepository;
    @Mock private SalesContactEmploymentRepository employmentRepository;
    @Mock private SalesContactSourceRepository contactSourceRepository;
    @Mock private SalesContactMapper salesContactMapper;
    @Mock private CustomerApi customerApi;
    @Mock private AcquisitionSourceService acquisitionSourceService;

    @Test
    @DisplayName("고객사 담당자 판정은 종료되지 않은 현재 재직 관계만 사용")
    void active_employment_at_customer_contract() {
        given(employmentRepository.existsByContactIdAndCustomerIdAndEndDateIsNull(7L, 10L))
                .willReturn(true);

        assertThat(salesContactService.hasActiveEmploymentAtCustomer(7L, 10L)).isTrue();
        assertThat(salesContactService.hasActiveEmploymentAtCustomer(7L, null)).isFalse();
    }

    @Test
    @DisplayName("고객사 필터 검색은 동시 재직자의 해당 고객사 직책·부서로 보강")
    void search_customer_filter_uses_matching_employment() {
        SalesContact contact = mockContact("정대성");
        ReflectionTestUtils.setField(contact, "id", 7L);
        SalesContactEmployment matching = SalesContactEmployment.builder()
                .contactId(7L).customerId(20L).position("B사 팀장").department("B영업")
                .startDate(LocalDate.of(2026, 1, 1)).build();
        SalesContactSearchCondition condition = new SalesContactSearchCondition(
                null, null, null, null, 20L);
        PageRequest pageable = PageRequest.of(0, 10);
        given(contactRepository.search(condition, pageable))
                .willReturn(new PageImpl<>(List.of(contact), pageable, 1));
        given(employmentRepository.findByContactIdInAndCustomerIdAndEndDateIsNull(
                List.of(7L), 20L)).willReturn(List.of(matching));
        given(customerApi.findByIds(List.of(20L))).willReturn(List.of(
                CustomerInfo.builder().id(20L).name("B사").build()));
        given(contactSourceRepository.findByContactIdIn(List.of(7L))).willReturn(List.of());

        PageResponse<SalesContactSummaryResponse> result =
                salesContactService.search(condition, pageable);

        assertThat(result.content()).singleElement().satisfies(summary -> {
            assertThat(summary.currentCompanyName()).isEqualTo("B사");
            assertThat(summary.currentPosition()).isEqualTo("B사 팀장");
            assertThat(summary.currentDepartment()).isEqualTo("B영업");
        });
        verify(employmentRepository, never()).findByContactIdInAndEndDateIsNull(any());
    }

    @Test
    @DisplayName("고객 범위 밖 명부 검색은 repository 조회 전에 NOT_FOUND")
    void search_customer_filter_rejects_invisible_customer_before_query() {
        SalesContactSearchCondition condition = new SalesContactSearchCondition(
                null, null, null, null, 20L);
        PageRequest pageable = PageRequest.of(0, 10);
        willThrow(new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND))
                .given(customerApi).assertVisibleToCurrentViewer(Menu.SALES_CUSTOMERS, 20L);

        assertThatThrownBy(() -> salesContactService.search(condition, pageable))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);

        verify(contactRepository, never()).search(any(), any());
        verifyNoInteractions(employmentRepository, contactSourceRepository);
    }

    @Test
    @DisplayName("고객 범위 밖 재직 이력은 고객 존재·재직 조회 전에 NOT_FOUND")
    void find_employments_rejects_invisible_customer_before_lookup() {
        willThrow(new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND))
                .given(customerApi).assertVisibleToCurrentViewer(Menu.SALES_CUSTOMERS, 20L);

        assertThatThrownBy(() -> salesContactService.findEmploymentsByCustomerId(20L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomerErrorCode.CUSTOMER_NOT_FOUND);

        verify(customerApi, never()).getById(any());
        verifyNoInteractions(employmentRepository, contactRepository);
    }

    @Test
    @DisplayName("create 성공 — 명부 마스터 저장")
    void create_success() {
        // given
        SalesContact saved = mockContact("정대성");
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(contactRepository.save(any(SalesContact.class))).willReturn(saved);

        // when
        Long id = salesContactService.create(new SalesContactCreateRequest(
                "정대성", null, "010-0000-0000", null, "ds@daesung.co.kr", null,
                LocalDate.of(2026, 4, 1), List.of(), null
        ));

        // then
        assertThat(id).isEqualTo(100L);
    }

    @Test
    @DisplayName("create는 컨택 경로 20개를 넘으면 저장 전에 거절한다")
    void create_fail_too_many_sources() {
        List<Long> sourceIds = LongStream.rangeClosed(1, 21).boxed().toList();
        SalesContactCreateRequest request = new SalesContactCreateRequest(
                "정대성", null, null, null, null, null, null, sourceIds, null);

        assertThatThrownBy(() -> salesContactService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.INVALID_SOURCE_SELECTION);
        verify(acquisitionSourceService, never()).validateIds(any());
        verify(contactRepository, never()).save(any());
    }

    @Test
    @DisplayName("update는 null 컨택 경로 식별자를 조회 전에 거절한다")
    void update_fail_null_source_id() {
        SalesContactUpdateRequest request = new SalesContactUpdateRequest(
                "정대성", null, null, null, null, null, null,
                java.util.Arrays.asList(1L, null), null);

        assertThatThrownBy(() -> salesContactService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.INVALID_SOURCE_SELECTION);
        verify(contactRepository, never()).findById(any());
        verify(acquisitionSourceService, never()).validateIds(any());
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 명부")
    void update_fail_not_found() {
        // given
        given(contactRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> salesContactService.update(99L, baseUpdate("이름")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.CONTACT_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 명부 + 재직 이력 함께 제거")
    void delete_success_cascades_employments() {
        // given
        given(contactRepository.existsById(1L)).willReturn(true);
        given(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(1L))
                .willReturn(List.of(mockEmployment(1L, 10L, null)));

        // when
        salesContactService.delete(1L);

        // then
        verify(employmentRepository).deleteAll(any());
        verify(contactRepository).deleteById(1L);
    }

    @Test
    @DisplayName("createEmployment 성공 — customerId 만 채워짐")
    void create_employment_with_customer_id() {
        // given
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));
        given(customerApi.getById(10L)).willReturn(CustomerInfo.builder().id(10L).name("대성상사").build());

        SalesContactEmployment saved = mockEmployment(1L, 10L, null);
        ReflectionTestUtils.setField(saved, "id", 200L);
        given(employmentRepository.save(any(SalesContactEmployment.class))).willReturn(saved);

        // when
        Long id = salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                10L, "무시되어야 할 외부 회사명", "팀장", "영업1팀", LocalDate.of(2026, 4, 1)
        ));

        // then
        assertThat(id).isEqualTo(200L);
    }

    @Test
    @DisplayName("createEmployment 성공 — externalCompanyName 만 채워짐")
    void create_employment_with_external_company() {
        // given
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));
        SalesContactEmployment saved = mockEmployment(1L, null, "외부회사");
        ReflectionTestUtils.setField(saved, "id", 201L);
        given(employmentRepository.save(any(SalesContactEmployment.class))).willReturn(saved);

        // when
        Long id = salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                null, "외부회사", null, null, LocalDate.of(2026, 4, 1)
        ));

        // then
        assertThat(id).isEqualTo(201L);
        verify(customerApi, never()).getById(any());
    }

    @Test
    @DisplayName("createEmployment 실패 — 회사 정보 둘 다 없음 (COMPANY_REQUIRED)")
    void create_employment_fail_company_required() {
        // given
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));

        // when & then
        assertThatThrownBy(() -> salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                null, null, null, null, LocalDate.of(2026, 4, 1)
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.COMPANY_REQUIRED);
    }

    @Test
    @DisplayName("updateEmployment 실패 — 이미 종료된 재직")
    void update_employment_fail_already_terminated() {
        // given
        SalesContactEmployment terminated = SalesContactEmployment.builder()
                .contactId(1L).customerId(10L).startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();
        given(employmentRepository.findById(7L)).willReturn(Optional.of(terminated));

        // when & then
        assertThatThrownBy(() -> salesContactService.updateEmployment(7L, new SalesContactEmploymentUpdateRequest(
                10L, null, "수정", null, LocalDate.of(2024, 1, 1)
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("terminateEmployment 성공 — endDate / departureType / 메모 설정")
    void terminate_employment_success() {
        // given
        SalesContactEmployment active = mockEmployment(1L, 10L, null);
        given(employmentRepository.findById(7L)).willReturn(Optional.of(active));

        // when
        salesContactService.terminateEmployment(7L, new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 30), DepartureType.JOB_CHANGE, "이직"
        ));

        // then
        assertThat(active.isActive()).isFalse();
        assertThat(active.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(active.getDepartureType()).isEqualTo(DepartureType.JOB_CHANGE);
        assertThat(active.getDepartureNote()).isEqualTo("이직");
    }

    @Test
    @DisplayName("terminateEmployment 실패 — endDate < startDate")
    void terminate_employment_fail_invalid_end_date() {
        // given
        SalesContactEmployment active = SalesContactEmployment.builder()
                .contactId(1L).customerId(10L).startDate(LocalDate.of(2026, 5, 1))
                .build();
        given(employmentRepository.findById(7L)).willReturn(Optional.of(active));

        // when & then
        assertThatThrownBy(() -> salesContactService.terminateEmployment(7L, new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 1), DepartureType.OTHER, null
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.INVALID_END_DATE);
    }

    @Test
    @DisplayName("deleteEmployment 실패 — 존재하지 않음")
    void delete_employment_fail_not_found() {
        // given
        given(employmentRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> salesContactService.deleteEmployment(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.EMPLOYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("getDetail 성공 — 외부 회사 재직 (customerId == null) 섞여도 NPE 없음")
    void get_detail_external_company_employment() {
        // given
        SalesContact contact = mockContact("정대성");
        ReflectionTestUtils.setField(contact, "id", 1L);
        given(contactRepository.findById(1L)).willReturn(Optional.of(contact));
        given(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(1L))
                .willReturn(List.of(mockEmployment(1L, null, "외부회사")));
        given(contactSourceRepository.findByContactIdIn(List.of(1L))).willReturn(List.of());

        // when
        salesContactService.getDetail(1L);

        // then
        // customerApi.findByIds 는 호출되지 않아야 함 — customerId 가 모두 null 이므로 lookup 스킵.
        verify(customerApi, never()).findByIds(any());
    }

    private SalesContact mockContact(String name) {
        return SalesContact.builder().name(name).build();
    }

    private SalesContactEmployment mockEmployment(Long contactId, Long customerId, String externalCompanyName) {
        return SalesContactEmployment.builder()
                .contactId(contactId)
                .customerId(customerId)
                .externalCompanyName(externalCompanyName)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    private SalesContactUpdateRequest baseUpdate(String name) {
        return new SalesContactUpdateRequest(name, null, null, null, null, null, null, null, null);
    }
}

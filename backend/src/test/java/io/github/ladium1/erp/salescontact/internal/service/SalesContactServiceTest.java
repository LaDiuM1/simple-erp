package io.github.ladium1.erp.salescontact.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.exception.SalesContactErrorCode;
import io.github.ladium1.erp.salescontact.internal.mapper.SalesContactMapper;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactSourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalesContactServiceTest {

    @InjectMocks
    private SalesContactService salesContactService;

    @Mock private SalesContactRepository contactRepository;
    @Mock private SalesContactEmploymentRepository employmentRepository;
    @Mock private SalesContactSourceRepository contactSourceRepository;
    @Mock private SalesContactMapper salesContactMapper;
    @Mock private CustomerApi customerApi;
    @Mock private io.github.ladium1.erp.salescontact.internal.service.AcquisitionSourceService acquisitionSourceService;

    @Test
    @DisplayName("create 성공 — 명부 마스터 저장")
    void create_success() {
        SalesContact saved = mockContact("정대성");
        ReflectionTestUtils.setField(saved, "id", 100L);
        given(contactRepository.save(any(SalesContact.class))).willReturn(saved);

        Long id = salesContactService.create(new SalesContactCreateRequest(
                "정대성", null, "010-0000-0000", null, "ds@daesung.co.kr", null,
                LocalDate.of(2026, 4, 1), List.of(), null
        ));

        assertThat(id).isEqualTo(100L);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 명부")
    void update_fail_not_found() {
        given(contactRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> salesContactService.update(99L, baseUpdate("이름")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.CONTACT_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 명부 + 재직 이력 함께 제거")
    void delete_success_cascades_employments() {
        given(contactRepository.existsById(1L)).willReturn(true);
        given(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(1L))
                .willReturn(List.of(mockEmployment(1L, 10L, null)));

        salesContactService.delete(1L);

        verify(employmentRepository).deleteAll(any());
        verify(contactRepository).deleteById(1L);
    }

    @Test
    @DisplayName("createEmployment 성공 — customerId 만 채워짐 (외부 회사명 무시)")
    void create_employment_with_customer_id() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));
        given(customerApi.getById(10L)).willReturn(CustomerInfo.builder().id(10L).name("대성상사").build());

        SalesContactEmployment saved = mockEmployment(1L, 10L, null);
        ReflectionTestUtils.setField(saved, "id", 200L);
        given(employmentRepository.save(any(SalesContactEmployment.class))).willReturn(saved);

        Long id = salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                10L, "무시되어야 할 외부 회사명", "팀장", "영업1팀", LocalDate.of(2026, 4, 1)
        ));

        assertThat(id).isEqualTo(200L);
    }

    @Test
    @DisplayName("createEmployment 성공 — externalCompanyName 만 채워짐")
    void create_employment_with_external_company() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));
        SalesContactEmployment saved = mockEmployment(1L, null, "외부회사");
        ReflectionTestUtils.setField(saved, "id", 201L);
        given(employmentRepository.save(any(SalesContactEmployment.class))).willReturn(saved);

        Long id = salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                null, "외부회사", null, null, LocalDate.of(2026, 4, 1)
        ));

        assertThat(id).isEqualTo(201L);
        verify(customerApi, never()).getById(any());
    }

    @Test
    @DisplayName("createEmployment 실패 — customerId / externalCompanyName 둘 다 없음 → COMPANY_REQUIRED")
    void create_employment_fail_company_required() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(mockContact("정대성")));

        assertThatThrownBy(() -> salesContactService.createEmployment(1L, new SalesContactEmploymentCreateRequest(
                null, null, null, null, LocalDate.of(2026, 4, 1)
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.COMPANY_REQUIRED);
    }

    @Test
    @DisplayName("updateEmployment 실패 — 이미 종료된 재직")
    void update_employment_fail_already_terminated() {
        SalesContactEmployment terminated = SalesContactEmployment.builder()
                .contactId(1L).customerId(10L).startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();
        given(employmentRepository.findById(7L)).willReturn(Optional.of(terminated));

        assertThatThrownBy(() -> salesContactService.updateEmployment(7L, new SalesContactEmploymentUpdateRequest(
                10L, null, "수정", null, LocalDate.of(2024, 1, 1)
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
    }

    @Test
    @DisplayName("terminateEmployment 성공 — endDate / departureType / 메모 설정")
    void terminate_employment_success() {
        SalesContactEmployment active = mockEmployment(1L, 10L, null);
        given(employmentRepository.findById(7L)).willReturn(Optional.of(active));

        salesContactService.terminateEmployment(7L, new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 30), DepartureType.JOB_CHANGE, "이직"
        ));

        assertThat(active.isActive()).isFalse();
        assertThat(active.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(active.getDepartureType()).isEqualTo(DepartureType.JOB_CHANGE);
        assertThat(active.getDepartureNote()).isEqualTo("이직");
    }

    @Test
    @DisplayName("terminateEmployment 실패 — endDate < startDate")
    void terminate_employment_fail_invalid_end_date() {
        SalesContactEmployment active = SalesContactEmployment.builder()
                .contactId(1L).customerId(10L).startDate(LocalDate.of(2026, 5, 1))
                .build();
        given(employmentRepository.findById(7L)).willReturn(Optional.of(active));

        assertThatThrownBy(() -> salesContactService.terminateEmployment(7L, new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 1), DepartureType.OTHER, null
        ))).isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.INVALID_END_DATE);
    }

    @Test
    @DisplayName("deleteEmployment 실패 — 존재하지 않음")
    void delete_employment_fail_not_found() {
        given(employmentRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> salesContactService.deleteEmployment(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SalesContactErrorCode.EMPLOYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("getDetail 성공 — 외부 회사 재직 (customerId == null) 가 섞여도 NPE 없이 응답")
    void get_detail_external_company_employment() {
        SalesContact contact = mockContact("정대성");
        ReflectionTestUtils.setField(contact, "id", 1L);
        given(contactRepository.findById(1L)).willReturn(Optional.of(contact));
        given(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(1L))
                .willReturn(List.of(mockEmployment(1L, null, "외부회사")));
        given(contactSourceRepository.findByContactIdIn(List.of(1L))).willReturn(List.of());

        salesContactService.getDetail(1L);

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

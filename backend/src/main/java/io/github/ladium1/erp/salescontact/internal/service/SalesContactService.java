package io.github.ladium1.erp.salescontact.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescontact.api.dto.RecentSalesContactInfo;
import io.github.ladium1.erp.salescontact.api.dto.SalesContactInfo;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactDetailResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSummaryResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.exception.SalesContactErrorCode;
import io.github.ladium1.erp.salescontact.internal.mapper.SalesContactMapper;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesContactService implements SalesContactApi {

    private final SalesContactRepository contactRepository;
    private final SalesContactEmploymentRepository employmentRepository;
    private final SalesContactMapper salesContactMapper;
    private final CustomerApi customerApi;

    @Override
    public SalesContactInfo getById(Long id) {
        SalesContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));
        SalesContactEmployment active = employmentRepository.findByContactIdAndEndDateIsNull(id)
                .stream().findFirst().orElse(null);
        String companyName = resolveCompanyName(active);
        String position = active == null ? null : active.getPosition();
        return SalesContactInfo.builder()
                .id(contact.getId())
                .name(contact.getName())
                .currentCompanyName(companyName)
                .currentPosition(position)
                .build();
    }

    @Override
    public List<SalesContactInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<SalesContact> contacts = contactRepository.findAllById(ids);
        EmploymentRefs refs = loadActiveEmployments(contacts.stream().map(SalesContact::getId).toList());
        return contacts.stream()
                .map(c -> {
                    SalesContactEmployment active = refs.activeByContact.get(c.getId());
                    return SalesContactInfo.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .currentCompanyName(refs.companyName(active))
                            .currentPosition(active == null ? null : active.getPosition())
                            .build();
                })
                .toList();
    }

    @Override
    public long count() {
        return contactRepository.count();
    }

    @Override
    public List<RecentSalesContactInfo> findRecent(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<SalesContact> contacts = contactRepository.findAll(pageable).getContent();
        EmploymentRefs refs = loadActiveEmployments(contacts.stream().map(SalesContact::getId).toList());
        return contacts.stream()
                .map(c -> {
                    SalesContactEmployment active = refs.activeByContact.get(c.getId());
                    return RecentSalesContactInfo.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .currentCompanyName(refs.companyName(active))
                            .currentPosition(active == null ? null : active.getPosition())
                            .metAt(c.getMetAt())
                            .createdAt(c.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public PageResponse<SalesContactSummaryResponse> search(SalesContactSearchCondition condition, Pageable pageable) {
        Page<SalesContact> page = contactRepository.search(condition, pageable);
        EmploymentRefs refs = loadActiveEmployments(page.getContent().stream().map(SalesContact::getId).toList());
        return PageResponse.of(page.map(c -> {
            SalesContactEmployment active = refs.activeByContact.get(c.getId());
            return SalesContactSummaryResponse.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .mobilePhone(c.getMobilePhone())
                    .email(c.getEmail())
                    .currentCompanyName(refs.companyName(active))
                    .currentPosition(active == null ? null : active.getPosition())
                    .currentDepartment(active == null ? null : active.getDepartment())
                    .metAt(c.getMetAt())
                    .build();
        }));
    }

    public SalesContactDetailResponse getDetail(Long id) {
        SalesContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));
        List<SalesContactEmployment> employments = employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(id);
        Map<Long, String> customerNames = loadCustomerNames(employments);

        List<SalesContactEmploymentResponse> employmentResponses = employments.stream()
                .map(e -> salesContactMapper.toEmploymentResponse(e, contact, customerNames.get(e.getCustomerId())))
                .toList();

        return SalesContactDetailResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .nameEn(contact.getNameEn())
                .mobilePhone(contact.getMobilePhone())
                .officePhone(contact.getOfficePhone())
                .email(contact.getEmail())
                .personalEmail(contact.getPersonalEmail())
                .metAt(contact.getMetAt())
                .metVia(contact.getMetVia())
                .note(contact.getNote())
                .employments(employmentResponses)
                .build();
    }

    /**
     * 고객사 영업 상세 페이지가 호출하는 — 해당 고객사 소속 영업 명부 재직 이력.
     */
    public List<SalesContactEmploymentResponse> findEmploymentsByCustomerId(Long customerId) {
        customerApi.getById(customerId);
        List<SalesContactEmployment> employments = employmentRepository.findByCustomerIdOrderByEndDateAscStartDateDesc(customerId);
        Map<Long, SalesContact> contactMap = contactRepository
                .findAllById(employments.stream().map(SalesContactEmployment::getContactId).distinct().toList())
                .stream()
                .collect(toMap(SalesContact::getId, c -> c));
        Map<Long, String> customerNames = loadCustomerNames(employments);

        return employments.stream()
                .map(e -> salesContactMapper.toEmploymentResponse(
                        e,
                        contactMap.get(e.getContactId()),
                        customerNames.get(e.getCustomerId())
                ))
                .toList();
    }

    @Transactional
    public Long create(SalesContactCreateRequest request) {
        SalesContact contact = SalesContact.builder()
                .name(request.name())
                .nameEn(request.nameEn())
                .mobilePhone(request.mobilePhone())
                .officePhone(request.officePhone())
                .email(request.email())
                .personalEmail(request.personalEmail())
                .metAt(request.metAt())
                .metVia(request.metVia())
                .note(request.note())
                .build();
        return contactRepository.save(contact).getId();
    }

    @Transactional
    public void update(Long id, SalesContactUpdateRequest request) {
        SalesContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));
        contact.update(
                request.name(),
                request.nameEn(),
                request.mobilePhone(),
                request.officePhone(),
                request.email(),
                request.personalEmail(),
                request.metAt(),
                request.metVia(),
                request.note()
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND);
        }
        // 재직 이력도 함께 제거 (물리 삭제 — 영업 명부 자체를 지우는 케이스)
        employmentRepository.deleteAll(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(id));
        contactRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    @Transactional
    public Long createEmployment(Long contactId, SalesContactEmploymentCreateRequest request) {
        SalesContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));
        Long customerId = validateCompany(request.customerId(), request.externalCompanyName());

        SalesContactEmployment employment = SalesContactEmployment.builder()
                .contactId(contact.getId())
                .customerId(customerId)
                .externalCompanyName(customerId == null ? trimToNull(request.externalCompanyName()) : null)
                .position(request.position())
                .department(request.department())
                .startDate(request.startDate())
                .build();
        return employmentRepository.save(employment).getId();
    }

    @Transactional
    public void updateEmployment(Long id, SalesContactEmploymentUpdateRequest request) {
        SalesContactEmployment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.EMPLOYMENT_NOT_FOUND));
        if (!employment.isActive()) {
            throw new BusinessException(SalesContactErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
        }
        Long customerId = validateCompany(request.customerId(), request.externalCompanyName());

        employment.update(
                customerId,
                customerId == null ? trimToNull(request.externalCompanyName()) : null,
                request.position(),
                request.department(),
                request.startDate()
        );
    }

    @Transactional
    public void terminateEmployment(Long id, SalesContactEmploymentTerminateRequest request) {
        SalesContactEmployment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.EMPLOYMENT_NOT_FOUND));
        if (!employment.isActive()) {
            throw new BusinessException(SalesContactErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
        }
        if (request.endDate().isBefore(employment.getStartDate())) {
            throw new BusinessException(SalesContactErrorCode.INVALID_END_DATE);
        }
        employment.terminate(request.endDate(), request.departureType(), request.departureNote());
    }

    @Transactional
    public void deleteEmployment(Long id) {
        if (!employmentRepository.existsById(id)) {
            throw new BusinessException(SalesContactErrorCode.EMPLOYMENT_NOT_FOUND);
        }
        employmentRepository.deleteById(id);
    }

    /**
     * customerId / externalCompanyName 중 한쪽만 채워지도록 정규화 — customerId 가 있으면 customer 무결성 검증 + externalCompanyName 무시.
     */
    private Long validateCompany(Long customerId, String externalCompanyName) {
        if (customerId != null) {
            customerApi.getById(customerId);
            return customerId;
        }
        if (!StringUtils.hasText(externalCompanyName)) {
            throw new BusinessException(SalesContactErrorCode.COMPANY_REQUIRED);
        }
        return null;
    }

    private String resolveCompanyName(SalesContactEmployment employment) {
        if (employment == null) return null;
        if (employment.getCustomerId() != null) {
            return Optional.ofNullable(customerApi.getById(employment.getCustomerId()))
                    .map(CustomerInfo::name).orElse(null);
        }
        return employment.getExternalCompanyName();
    }

    private EmploymentRefs loadActiveEmployments(List<Long> contactIds) {
        if (contactIds.isEmpty()) {
            return new EmploymentRefs(Map.of(), Map.of());
        }
        List<SalesContactEmployment> active = employmentRepository.findByContactIdInAndEndDateIsNull(contactIds);
        Map<Long, SalesContactEmployment> activeByContact = new HashMap<>();
        for (SalesContactEmployment e : active) {
            // 중복 활성 재직이 있어도 첫 번째만 — 단순화. 한 사람에 다수 동시 재직 케이스는 적음.
            activeByContact.putIfAbsent(e.getContactId(), e);
        }
        Map<Long, String> customerNames = loadCustomerNames(active);
        return new EmploymentRefs(activeByContact, customerNames);
    }

    private Map<Long, String> loadCustomerNames(List<SalesContactEmployment> employments) {
        List<Long> customerIds = employments.stream()
                .map(SalesContactEmployment::getCustomerId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (customerIds.isEmpty()) {
            return Map.of();
        }
        return customerApi.findByIds(customerIds).stream()
                .collect(toMap(CustomerInfo::id, CustomerInfo::name));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record EmploymentRefs(Map<Long, SalesContactEmployment> activeByContact,
                                  Map<Long, String> customerNames) {
        String companyName(SalesContactEmployment employment) {
            if (employment == null) return null;
            if (employment.getCustomerId() != null) {
                return customerNames.get(employment.getCustomerId());
            }
            return employment.getExternalCompanyName();
        }
    }
}

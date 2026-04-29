package io.github.ladium1.erp.salescontact.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescontact.api.dto.RecentSalesContactInfo;
import io.github.ladium1.erp.salescontact.api.dto.SalesContactInfo;
import io.github.ladium1.erp.salescontact.internal.dto.AcquisitionSourceInfo;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactDetailResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactExcelRow;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSummaryResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactUpdateRequest;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContactSource;
import io.github.ladium1.erp.salescontact.internal.excel.SalesContactExcelExporter;
import io.github.ladium1.erp.salescontact.internal.excel.SalesContactExcelImporter;
import io.github.ladium1.erp.salescontact.internal.excel.SalesContactExcelImporter.Holder;
import io.github.ladium1.erp.salescontact.internal.exception.SalesContactErrorCode;
import io.github.ladium1.erp.salescontact.internal.mapper.SalesContactMapper;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactEmploymentRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactRepository;
import io.github.ladium1.erp.salescontact.internal.repository.SalesContactSourceRepository;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRow;
import io.github.ladium1.erp.global.excel.ExcelImporter.ParsedRows;
import io.github.ladium1.erp.global.excel.ExcelRowError;
import io.github.ladium1.erp.global.excel.ExcelUploadResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesContactService implements SalesContactApi {

    private final SalesContactRepository contactRepository;
    private final SalesContactEmploymentRepository employmentRepository;
    private final SalesContactSourceRepository contactSourceRepository;
    private final SalesContactMapper salesContactMapper;
    private final SalesContactExcelExporter excelExporter;
    private final SalesContactExcelImporter excelImporter;
    private final CustomerApi customerApi;
    private final AcquisitionSourceService acquisitionSourceService;
    private final Validator validator;

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
        List<Long> contactIds = page.getContent().stream().map(SalesContact::getId).toList();
        EmploymentRefs refs = loadActiveEmployments(contactIds);
        Map<Long, List<AcquisitionSourceInfo>> sourcesByContact = loadSourcesByContact(contactIds);
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
                    .sources(sourcesByContact.getOrDefault(c.getId(), List.of()))
                    .build();
        }));
    }

    /**
     * 검색 조건 + 정렬 그대로 전체 페이지를 .xlsx 바이트로 직렬화. 페이지네이션 무시 — 필터링된 전체.
     */
    public byte[] exportExcel(SalesContactSearchCondition condition, Sort sort) {
        List<SalesContact> contacts = contactRepository.searchAll(condition, sort);
        if (contacts.isEmpty()) {
            return excelExporter.export(List.of());
        }
        List<Long> contactIds = contacts.stream().map(SalesContact::getId).toList();
        EmploymentRefs refs = loadActiveEmployments(contactIds);
        Map<Long, List<AcquisitionSourceInfo>> sourcesByContact = loadSourcesByContact(contactIds);

        List<SalesContactExcelRow> rows = contacts.stream()
                .map(c -> {
                    SalesContactEmployment active = refs.activeByContact.get(c.getId());
                    String sourcesJoined = sourcesByContact.getOrDefault(c.getId(), List.of()).stream()
                            .map(AcquisitionSourceInfo::name)
                            .collect(java.util.stream.Collectors.joining(", "));
                    return SalesContactExcelRow.builder()
                            .name(c.getName())
                            .nameEn(c.getNameEn())
                            .mobilePhone(c.getMobilePhone())
                            .officePhone(c.getOfficePhone())
                            .email(c.getEmail())
                            .personalEmail(c.getPersonalEmail())
                            .metAt(c.getMetAt())
                            .sources(sourcesJoined.isEmpty() ? null : sourcesJoined)
                            .currentCompanyName(refs.companyName(active))
                            .currentPosition(active == null ? null : active.getPosition())
                            .currentDepartment(active == null ? null : active.getDepartment())
                            .note(c.getNote())
                            .build();
                })
                .toList();
        return excelExporter.export(rows);
    }

    public byte[] exportTemplate() {
        return excelImporter.exportTemplate();
    }

    /**
     * 엑셀 일괄 업로드 — all-or-nothing.
     * <p>
     * 만난 경로는 acquisition_sources.name 으로 lookup (없는 이름은 에러). 현재 회사 / 직책 / 부서가 채워지면
     * 활성 재직 이력 (외부 회사 자유 입력) 으로 함께 생성. customerId 매핑은 지원 안 함 — 회사명은 free-form 입력.
     */
    @Transactional
    public ExcelUploadResult importExcel(MultipartFile file) {
        ParsedRows<Holder> parsed = excelImporter.parse(file);
        List<ExcelRowError> errors = new ArrayList<>(parsed.errors());

        Set<String> seenMobilePhones = new HashSet<>();

        // 모든 행에서 참조된 source 이름 lookup 1번에 처리.
        Set<String> allSourceNames = new HashSet<>();
        for (ParsedRow<Holder> pr : parsed.builders()) {
            allSourceNames.addAll(pr.builder().sourceNames);
        }
        Map<String, Long> sourceIdByName = acquisitionSourceService.findByNames(new ArrayList<>(allSourceNames)).stream()
                .collect(java.util.stream.Collectors.toMap(s -> s.name(), s -> s.id()));

        for (ParsedRow<Holder> pr : parsed.builders()) {
            int rowNum = pr.rowNum();
            Holder holder = pr.builder();
            holder.mobilePhone = trimToNull(holder.mobilePhone);
            holder.email = trimToNull(holder.email);
            holder.personalEmail = trimToNull(holder.personalEmail);

            SalesContactCreateRequest req = new SalesContactCreateRequest(
                    holder.name, holder.nameEn, holder.mobilePhone, holder.officePhone,
                    holder.email, holder.personalEmail, holder.metAt, List.of(), holder.note
            );
            for (ConstraintViolation<SalesContactCreateRequest> v : validator.validate(req)) {
                errors.add(ExcelRowError.of(rowNum, v.getPropertyPath().toString(), v.getMessage()));
            }

            if (holder.mobilePhone != null) {
                if (!seenMobilePhones.add(holder.mobilePhone)) {
                    errors.add(ExcelRowError.of(rowNum, "휴대폰", "엑셀 내에서 중복된 휴대폰 번호입니다."));
                } else if (contactRepository.existsByMobilePhone(holder.mobilePhone)) {
                    errors.add(ExcelRowError.of(rowNum, "휴대폰", SalesContactErrorCode.DUPLICATE_MOBILE_PHONE.getMessage()));
                }
            }

            for (String sourceName : holder.sourceNames) {
                if (!sourceIdByName.containsKey(sourceName)) {
                    errors.add(ExcelRowError.of(rowNum, "만난 경로",
                            "등록되지 않은 경로입니다: '" + sourceName + "' (시스템에 미리 등록된 경로명만 사용 가능합니다)"));
                }
            }

            // 재직 이력은 회사명이 채워진 경우에만 생성. 시작일이 없으면 metAt 또는 오늘.
            if (trimToNull(holder.currentCompanyName) == null
                    && (trimToNull(holder.currentPosition) != null || trimToNull(holder.currentDepartment) != null)) {
                errors.add(ExcelRowError.of(rowNum, "현재 회사",
                        "직책 / 부서를 입력하려면 현재 회사도 함께 입력해야 합니다."));
            }
        }

        if (!errors.isEmpty()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ExcelUploadResult.failure(parsed.totalRows(), errors);
        }

        for (ParsedRow<Holder> pr : parsed.builders()) {
            Holder h = pr.builder();
            SalesContact contact = SalesContact.builder()
                    .name(h.name)
                    .nameEn(h.nameEn)
                    .mobilePhone(h.mobilePhone)
                    .officePhone(h.officePhone)
                    .email(h.email)
                    .personalEmail(h.personalEmail)
                    .metAt(h.metAt)
                    .note(h.note)
                    .build();
            Long contactId = contactRepository.save(contact).getId();

            if (!h.sourceNames.isEmpty()) {
                List<SalesContactSource> rows = h.sourceNames.stream()
                        .map(name -> SalesContactSource.builder()
                                .contactId(contactId)
                                .sourceId(sourceIdByName.get(name))
                                .build())
                        .toList();
                contactSourceRepository.saveAll(rows);
            }

            String company = trimToNull(h.currentCompanyName);
            if (company != null) {
                LocalDate startDate = h.metAt != null ? h.metAt : LocalDate.now();
                SalesContactEmployment employment = SalesContactEmployment.builder()
                        .contactId(contactId)
                        .externalCompanyName(company)
                        .position(trimToNull(h.currentPosition))
                        .department(trimToNull(h.currentDepartment))
                        .startDate(startDate)
                        .build();
                employmentRepository.save(employment);
            }
        }
        return ExcelUploadResult.success(parsed.totalRows());
    }

    public SalesContactDetailResponse getDetail(Long id) {
        SalesContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));
        List<SalesContactEmployment> employments = employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(id);
        Map<Long, String> customerNames = loadCustomerNames(employments);

        List<SalesContactEmploymentResponse> employmentResponses = employments.stream()
                .map(e -> salesContactMapper.toEmploymentResponse(e, contact, customerNames.get(e.getCustomerId())))
                .toList();

        List<AcquisitionSourceInfo> sources = loadSourcesByContact(List.of(id))
                .getOrDefault(id, List.of());

        return SalesContactDetailResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .nameEn(contact.getNameEn())
                .mobilePhone(contact.getMobilePhone())
                .officePhone(contact.getOfficePhone())
                .email(contact.getEmail())
                .personalEmail(contact.getPersonalEmail())
                .metAt(contact.getMetAt())
                .note(contact.getNote())
                .sources(sources)
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

    /**
     * 휴대폰 번호 사용 가능 여부 — 등록 / 수정 화면의 디바운스 중복 검사 용.
     * excludeId 가 있으면 자기 자신은 제외 (수정 모드에서 본인 번호 그대로 두는 케이스 허용).
     */
    public boolean isMobilePhoneAvailable(String mobilePhone, Long excludeId) {
        if (mobilePhone == null || mobilePhone.isBlank()) {
            return false;
        }
        String trimmed = mobilePhone.trim();
        if (excludeId == null) {
            return !contactRepository.existsByMobilePhone(trimmed);
        }
        return !contactRepository.existsByMobilePhoneAndIdNot(trimmed, excludeId);
    }

    @Transactional
    public Long create(SalesContactCreateRequest request) {
        List<Long> sourceIds = distinctOrEmpty(request.sourceIds());
        acquisitionSourceService.validateIds(sourceIds);

        String mobilePhone = trimToNull(request.mobilePhone());
        if (mobilePhone != null && contactRepository.existsByMobilePhone(mobilePhone)) {
            throw new BusinessException(SalesContactErrorCode.DUPLICATE_MOBILE_PHONE);
        }

        SalesContact contact = SalesContact.builder()
                .name(request.name())
                .nameEn(request.nameEn())
                .mobilePhone(mobilePhone)
                .officePhone(request.officePhone())
                .email(request.email())
                .personalEmail(request.personalEmail())
                .metAt(request.metAt())
                .note(request.note())
                .build();
        Long id = contactRepository.save(contact).getId();
        replaceSources(id, sourceIds);
        return id;
    }

    @Transactional
    public void update(Long id, SalesContactUpdateRequest request) {
        SalesContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));

        List<Long> sourceIds = distinctOrEmpty(request.sourceIds());
        acquisitionSourceService.validateIds(sourceIds);

        String mobilePhone = trimToNull(request.mobilePhone());
        if (mobilePhone != null
                && !mobilePhone.equals(contact.getMobilePhone())
                && contactRepository.existsByMobilePhone(mobilePhone)) {
            throw new BusinessException(SalesContactErrorCode.DUPLICATE_MOBILE_PHONE);
        }

        contact.update(
                request.name(),
                request.nameEn(),
                mobilePhone,
                request.officePhone(),
                request.email(),
                request.personalEmail(),
                request.metAt(),
                request.note()
        );
        replaceSources(id, sourceIds);
    }

    @Transactional
    public void delete(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND);
        }
        // 재직 이력 / 만난 경로 정션도 함께 제거 (물리 삭제 — 영업 명부 자체를 지우는 케이스)
        employmentRepository.deleteAll(employmentRepository.findByContactIdOrderByEndDateAscStartDateDesc(id));
        contactSourceRepository.deleteByContactId(id);
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
     * 명부의 컨택 경로 정션을 새 sourceIds 로 교체 (delete-all + insert-all). 빈 목록이면 정션을 모두 비움.
     */
    private void replaceSources(Long contactId, List<Long> sourceIds) {
        contactSourceRepository.deleteByContactId(contactId);
        if (sourceIds.isEmpty()) {
            return;
        }
        List<SalesContactSource> rows = sourceIds.stream()
                .map(sid -> SalesContactSource.builder()
                        .contactId(contactId)
                        .sourceId(sid)
                        .build())
                .toList();
        contactSourceRepository.saveAll(rows);
    }

    /**
     * 명부 ids 의 정션을 일괄 조회 → contactId → 정렬된 AcquisitionSourceInfo 목록으로 변환.
     * 정렬: type ASC → name ASC.
     */
    private Map<Long, List<AcquisitionSourceInfo>> loadSourcesByContact(List<Long> contactIds) {
        if (contactIds == null || contactIds.isEmpty()) {
            return Map.of();
        }
        List<SalesContactSource> rows = contactSourceRepository.findByContactIdIn(contactIds);
        if (rows.isEmpty()) {
            return Map.of();
        }
        List<Long> sourceIds = rows.stream().map(SalesContactSource::getSourceId).distinct().toList();
        Map<Long, AcquisitionSourceInfo> sourceById = acquisitionSourceService.findByIds(sourceIds).stream()
                .collect(toMap(AcquisitionSourceInfo::id, s -> s));
        Map<Long, List<AcquisitionSourceInfo>> grouped = rows.stream()
                .filter(r -> sourceById.containsKey(r.getSourceId()))
                .collect(groupingBy(
                        SalesContactSource::getContactId,
                        mapping(r -> sourceById.get(r.getSourceId()), toList())
                ));
        grouped.replaceAll((cid, list) -> list.stream()
                .sorted(java.util.Comparator
                        .comparing((AcquisitionSourceInfo s) -> s.type().name())
                        .thenComparing(AcquisitionSourceInfo::name))
                .toList());
        return grouped;
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

    private static List<Long> distinctOrEmpty(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> distinct = new HashSet<>(ids);
        return distinct.stream().toList();
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

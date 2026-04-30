package io.github.ladium1.erp.salescustomer.internal.service;

import io.github.ladium1.erp.customer.api.CustomerApi;
import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.salescontact.api.SalesContactApi;
import io.github.ladium1.erp.salescontact.api.dto.SalesContactInfo;
import io.github.ladium1.erp.salescustomer.api.SalesCustomerApi;
import io.github.ladium1.erp.salescustomer.api.dto.RecentSalesActivityInfo;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityResponse;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentResponse;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentTerminateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerAggregate;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerDetailResponse;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivity;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
import io.github.ladium1.erp.salescustomer.internal.exception.SalesCustomerErrorCode;
import io.github.ladium1.erp.salescustomer.internal.mapper.SalesCustomerMapper;
import io.github.ladium1.erp.salescustomer.internal.repository.AggregatedActivityCount;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesActivityRepository;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * 고객사 영업 관리 — 활동(Activity) + 담당자 배정(Assignment) 모두 다루는 단일 서비스.
 * customer / employee 무결성은 외부 모듈 Api 호출로 검증.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesCustomerService implements SalesCustomerApi {

    private final SalesActivityRepository activityRepository;
    private final SalesAssignmentRepository assignmentRepository;
    private final SalesCustomerMapper salesCustomerMapper;
    private final CustomerApi customerApi;
    private final EmployeeApi employeeApi;
    private final SalesContactApi salesContactApi;

    @Override
    public long countActivitiesSince(LocalDateTime since) {
        return activityRepository.countByActivityDateGreaterThanEqual(since);
    }

    @Override
    public List<RecentSalesActivityInfo> findRecentActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<SalesActivity> activities = activityRepository.findAllByOrderByActivityDateDesc(pageable);
        if (activities.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = activities.stream().map(SalesActivity::getCustomerId).distinct().toList();
        Map<Long, CustomerInfo> customerMap = customerApi.findByIds(customerIds).stream()
                .collect(toMap(CustomerInfo::id, c -> c));

        List<Long> employeeIds = activities.stream().map(SalesActivity::getOurEmployeeId).distinct().toList();
        Map<Long, EmployeeInfo> employeeMap = employeeApi.findByIds(employeeIds).stream()
                .collect(toMap(EmployeeInfo::id, e -> e));

        return activities.stream()
                .map(a -> {
                    CustomerInfo customer = customerMap.get(a.getCustomerId());
                    EmployeeInfo employee = employeeMap.get(a.getOurEmployeeId());
                    return RecentSalesActivityInfo.builder()
                            .id(a.getId())
                            .customerId(a.getCustomerId())
                            .customerCode(customer == null ? null : customer.code())
                            .customerName(customer == null ? null : customer.name())
                            .type(a.getType().name())
                            .subject(a.getSubject())
                            .activityDate(a.getActivityDate())
                            .ourEmployeeId(a.getOurEmployeeId())
                            .ourEmployeeName(employee == null ? null : employee.name())
                            .build();
                })
                .toList();
    }

    public SalesCustomerDetailResponse getDetail(Long customerId) {
        CustomerInfo customer = customerApi.getById(customerId);

        List<SalesActivity> activities = activityRepository.findByCustomerIdOrderByActivityDateDesc(customerId);
        List<SalesAssignment> assignments = assignmentRepository.findByCustomerIdOrderByEndDateAscStartDateDesc(customerId);

        List<Long> employeeIds = collectEmployeeIds(activities, assignments);
        Map<Long, EmployeeInfo> employeeMap = employeeApi.findByIds(employeeIds).stream()
                .collect(toMap(EmployeeInfo::id, e -> e));

        List<Long> contactIds = activities.stream()
                .map(SalesActivity::getCustomerContactId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SalesContactInfo> contactMap = salesContactApi.findByIds(contactIds).stream()
                .collect(toMap(SalesContactInfo::id, c -> c));

        List<SalesActivityResponse> activityResponses = activities.stream()
                .map(a -> salesCustomerMapper.toActivityResponse(
                        a,
                        customer,
                        employeeMap.get(a.getOurEmployeeId()),
                        a.getCustomerContactId() == null ? null : contactMap.get(a.getCustomerContactId())
                ))
                .toList();
        List<SalesAssignmentResponse> assignmentResponses = assignments.stream()
                .map(a -> salesCustomerMapper.toAssignmentResponse(a, employeeMap.get(a.getEmployeeId())))
                .toList();

        return SalesCustomerDetailResponse.builder()
                .customerId(customer.id())
                .customerCode(customer.code())
                .customerName(customer.name())
                .activities(activityResponses)
                .assignments(assignmentResponses)
                .build();
    }

    /**
     * 명부 상세 페이지에서 호출 — 특정 영업 명부 인물이 등장한 활동 이력 (모든 고객사 통합).
     */
    public List<SalesActivityResponse> findActivitiesByContactId(Long contactId) {
        SalesContactInfo contact = salesContactApi.getById(contactId);
        List<SalesActivity> activities = activityRepository.findByCustomerContactIdOrderByActivityDateDesc(contactId);
        if (activities.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = activities.stream().map(SalesActivity::getCustomerId).distinct().toList();
        Map<Long, CustomerInfo> customerMap = customerApi.findByIds(customerIds).stream()
                .collect(toMap(CustomerInfo::id, c -> c));

        List<Long> employeeIds = activities.stream().map(SalesActivity::getOurEmployeeId).distinct().toList();
        Map<Long, EmployeeInfo> employeeMap = employeeApi.findByIds(employeeIds).stream()
                .collect(toMap(EmployeeInfo::id, e -> e));

        return activities.stream()
                .map(a -> salesCustomerMapper.toActivityResponse(
                        a,
                        customerMap.get(a.getCustomerId()),
                        employeeMap.get(a.getOurEmployeeId()),
                        contact
                ))
                .toList();
    }

    /**
     * 목록 화면 보강 — 주어진 customerIds 에 대한 활성 담당자 / 활동 카운트 / 마지막 활동일 집계.
     */
    public List<SalesCustomerAggregate> aggregateByCustomerIds(Collection<Long> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }

        // 활동 카운트 + 마지막 활동일
        Map<Long, AggregatedActivityCount> activityMap = activityRepository.aggregateByCustomerIds(customerIds).stream()
                .collect(toMap(AggregatedActivityCount::customerId, a -> a));

        // 활성 배정
        List<SalesAssignment> activeAssignments = assignmentRepository.findByCustomerIdInAndEndDateIsNull(customerIds);
        Map<Long, List<SalesAssignment>> assignmentsByCustomer = activeAssignments.stream()
                .collect(Collectors.groupingBy(SalesAssignment::getCustomerId));

        // 활성 담당자들의 직원 정보 (이름)
        List<Long> employeeIds = activeAssignments.stream()
                .map(SalesAssignment::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, EmployeeInfo> employeeMap = employeeApi.findByIds(employeeIds).stream()
                .collect(toMap(EmployeeInfo::id, e -> e));

        return customerIds.stream()
                .map(cid -> {
                    AggregatedActivityCount act = activityMap.get(cid);
                    List<SalesAssignment> assignments = assignmentsByCustomer.getOrDefault(cid, List.of());
                    SalesAssignment primary = assignments.stream()
                            .filter(SalesAssignment::isPrimary)
                            .findFirst()
                            .orElse(null);
                    EmployeeInfo primaryEmployee = primary == null ? null : employeeMap.get(primary.getEmployeeId());

                    return SalesCustomerAggregate.builder()
                            .customerId(cid)
                            .primaryAssigneeId(primary == null ? null : primary.getEmployeeId())
                            .primaryAssigneeName(primaryEmployee == null ? null : primaryEmployee.name())
                            .activeAssigneeCount(assignments.size())
                            .activityCount(act == null ? 0L : act.count())
                            .lastActivityDate(act == null ? null : act.lastActivityDate())
                            .build();
                })
                .toList();
    }

    @Transactional
    public Long createActivity(SalesActivityCreateRequest request) {
        customerApi.getById(request.customerId());
        employeeApi.getById(request.ourEmployeeId());
        if (request.customerContactId() != null) {
            salesContactApi.getById(request.customerContactId());
        }

        SalesActivity activity = SalesActivity.builder()
                .customerId(request.customerId())
                .type(request.type())
                .activityDate(request.activityDate())
                .subject(request.subject())
                .content(request.content())
                .ourEmployeeId(request.ourEmployeeId())
                .customerContactId(request.customerContactId())
                .build();
        return activityRepository.save(activity).getId();
    }

    @Transactional
    public void updateActivity(Long id, SalesActivityUpdateRequest request) {
        SalesActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesCustomerErrorCode.ACTIVITY_NOT_FOUND));
        employeeApi.getById(request.ourEmployeeId());
        if (request.customerContactId() != null) {
            salesContactApi.getById(request.customerContactId());
        }

        activity.update(
                request.type(),
                request.activityDate(),
                request.subject(),
                request.content(),
                request.ourEmployeeId(),
                request.customerContactId()
        );
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new BusinessException(SalesCustomerErrorCode.ACTIVITY_NOT_FOUND);
        }
        activityRepository.deleteById(id);
    }

    @Transactional
    public Long createAssignment(SalesAssignmentCreateRequest request) {
        customerApi.getById(request.customerId());
        employeeApi.getById(request.employeeId());

        // 신규 배정이 primary 면 기존 primary 해제 (한 고객사당 활성 primary 1명 보장)
        if (request.primary()) {
            assignmentRepository.findByCustomerIdAndPrimaryTrueAndEndDateIsNull(request.customerId())
                    .forEach(SalesAssignment::clearPrimary);
        }

        SalesAssignment assignment = SalesAssignment.builder()
                .customerId(request.customerId())
                .employeeId(request.employeeId())
                .startDate(request.startDate())
                .endDate(null)
                .primary(request.primary())
                .reason(request.reason())
                .build();
        return assignmentRepository.save(assignment).getId();
    }

    @Transactional
    public void updateAssignment(Long id, SalesAssignmentUpdateRequest request) {
        SalesAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_NOT_FOUND));
        if (!assignment.isActive()) {
            throw new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_ALREADY_TERMINATED);
        }

        // primary 변경 시 같은 고객사의 다른 primary 해제
        if (request.primary() && !assignment.isPrimary()) {
            assignmentRepository.findByCustomerIdAndPrimaryTrueAndEndDateIsNull(assignment.getCustomerId())
                    .stream()
                    .filter(other -> !other.getId().equals(id))
                    .forEach(SalesAssignment::clearPrimary);
        }

        assignment.update(request.startDate(), request.primary(), request.reason());
    }

    @Transactional
    public void terminateAssignment(Long id, SalesAssignmentTerminateRequest request) {
        SalesAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_NOT_FOUND));
        if (!assignment.isActive()) {
            throw new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_ALREADY_TERMINATED);
        }
        if (request.endDate().isBefore(assignment.getStartDate())) {
            throw new BusinessException(SalesCustomerErrorCode.INVALID_END_DATE);
        }
        assignment.terminate(request.endDate(), request.reason());
    }

    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        assignmentRepository.deleteById(id);
    }

    private List<Long> collectEmployeeIds(List<SalesActivity> activities, List<SalesAssignment> assignments) {
        Map<Long, Boolean> seen = new HashMap<>();
        activities.forEach(a -> seen.put(a.getOurEmployeeId(), true));
        assignments.forEach(a -> seen.put(a.getEmployeeId(), true));
        return List.copyOf(seen.keySet());
    }
}

package io.github.ladium1.erp.salescustomer.internal.security;

import io.github.ladium1.erp.customer.api.CustomerVisibilityContributor;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.salescustomer.internal.repository.SalesAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 활성 영업 배정 (sales_assignments) 을 근거로 customer 가시성을 기여.
 * <p>
 * SELF: 본인이 활성 담당으로 들어간 customer.
 * DEPARTMENT(_TREE): 본인 부서 (또는 + 하위) 직원 누군가가 활성 담당으로 들어간 customer.
 */
@Component
@RequiredArgsConstructor
class SalesCustomerVisibilityProvider implements CustomerVisibilityContributor {

    private final SalesAssignmentRepository assignmentRepository;
    private final EmployeeApi employeeApi;

    @Override
    public Set<Long> visibleCustomerIds(DataScope scope, DataScopeContext context) {
        return switch (scope) {
            case ALL -> Set.of(); // 호출되지 않음 — defensive
            case SELF -> visibleByEmployees(
                    context.employeeId() == null ? List.of() : List.of(context.employeeId())
            );
            case DEPARTMENT -> visibleByDepartments(
                    context.departmentId() == null ? List.of() : List.of(context.departmentId())
            );
            case DEPARTMENT_TREE -> visibleByDepartments(
                    context.departmentSubtreeIds().stream().toList()
            );
        };
    }

    private Set<Long> visibleByDepartments(List<Long> departmentIds) {
        if (departmentIds.isEmpty()) return Set.of();
        List<Long> employeeIds = employeeApi.findIdsByDepartmentIds(departmentIds);
        return visibleByEmployees(employeeIds);
    }

    private Set<Long> visibleByEmployees(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) return Set.of();
        return Set.copyOf(assignmentRepository.findActiveCustomerIdsByEmployeeIds(employeeIds));
    }
}

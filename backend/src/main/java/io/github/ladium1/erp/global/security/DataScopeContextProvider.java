package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 현재 인증 사용자로부터 {@link DataScopeContext} 를 조립.
 * <p>
 * 가시성 contributor 가 호출해 SELF / DEPARTMENT(_TREE) 술어 생성에 사용한다.
 */
@Component
@RequiredArgsConstructor
public class DataScopeContextProvider {

    private final EmployeeApi employeeApi;
    private final DepartmentApi departmentApi;

    public DataScopeContext current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return DataScopeContext.anonymous();
        }
        return forLoginId(auth.getName());
    }

    public DataScopeContext forLoginId(String loginId) {
        EmployeeInfo me = employeeApi.findByLoginId(loginId).orElse(null);
        if (me == null) {
            return DataScopeContext.anonymous();
        }
        Long deptId = me.departmentId();
        Set<Long> subtree = deptId == null ? Set.of() : departmentApi.findSubtreeIds(deptId);
        return new DataScopeContext(me.id(), deptId, subtree);
    }
}

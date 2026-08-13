package io.github.ladium1.erp.employee.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.demo.DemoExcelExportGuard;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.DataScopeResolver;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.LoginAccountApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.employee.api.dto.EmploymentStatus;
import io.github.ladium1.erp.employee.internal.dto.EmployeeCreateRequest;
import io.github.ladium1.erp.employee.internal.dto.EmployeeDetailResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeReferenceResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeUpdateRequest;
import io.github.ladium1.erp.employee.internal.entity.Address;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import io.github.ladium1.erp.employee.internal.excel.EmployeeExcelExporter;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.mapper.EmployeeMapper;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService implements EmployeeApi, LoginAccountApi {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final RoleApi roleApi;
    private final DepartmentApi departmentApi;
    private final PositionApi positionApi;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeExcelExporter employeeExcelExporter;
    private final DataScopeResolver dataScopeResolver;
    private final DataScopeContextProvider dataScopeContextProvider;
    private final DemoProtectionPolicy demoProtectionPolicy;
    private final DemoExcelExportGuard demoExcelExportGuard;

    @Override
    public Long getRoleIdByLoginId(String loginId) {
        return employeeRepository.findByLoginId(loginId)
                .map(Employee::getRoleId)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Override
    public EmployeeInfo getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        ReferenceCache refs = loadReferences(List.of(employee));
        return toInfo(employee, refs);
    }

    @Override
    public Optional<EmployeeInfo> findByLoginId(String loginId) {
        return employeeRepository.findByLoginId(loginId)
                .map(employee -> toInfo(employee, loadReferences(List.of(employee))));
    }

    @Override
    public boolean isLoginAllowed(String loginId) {
        return loginId != null
                && employeeRepository.existsByLoginIdAndStatusNot(loginId, EmployeeStatus.RESIGNED);
    }

    @Override
    public List<EmployeeInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Employee> employees = employeeRepository.findAllById(ids);
        ReferenceCache refs = loadReferences(employees);
        return employees.stream().map(e -> toInfo(e, refs)).toList();
    }

    @Override
    public long countCurrentlyEmployed() {
        String excludedLoginId = demoProtectionPolicy.recoveryOperationsEmployeeLoginId();
        return excludedLoginId == null
                ? employeeRepository.countByStatusNot(EmployeeStatus.RESIGNED)
                : employeeRepository.countByStatusNotAndLoginIdNot(
                        EmployeeStatus.RESIGNED, excludedLoginId);
    }

    @Override
    public List<EmployeeInfo> findAllCurrentlyEmployed() {
        String excludedLoginId = demoProtectionPolicy.recoveryOperationsEmployeeLoginId();
        List<Employee> employees = excludedLoginId == null
                ? employeeRepository.findByStatusNot(EmployeeStatus.RESIGNED)
                : employeeRepository.findByStatusNotAndLoginIdNot(
                        EmployeeStatus.RESIGNED, excludedLoginId);
        ReferenceCache refs = loadReferences(employees);
        return employees.stream().map(e -> toInfo(e, refs)).toList();
    }

    @Override
    public boolean isCurrentlyEmployed(Long employeeId) {
        return employeeId != null
                && employeeRepository.findById(employeeId)
                        .filter(employee -> employee.getStatus() != EmployeeStatus.RESIGNED
                                && !demoProtectionPolicy.isOperationsEmployee(employee.getLoginId()))
                        .isPresent();
    }

    @Override
    public boolean isEligibleForNewWorkReference(Long employeeId) {
        return employeeId != null && allEligibleForNewWorkReference(List.of(employeeId));
    }

    @Override
    public boolean allEligibleForNewWorkReference(Collection<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty() || employeeIds.stream().anyMatch(Objects::isNull)) {
            return false;
        }
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>(employeeIds);
        List<Employee> employees = employeeRepository.findAllById(distinctIds);
        return employees.size() == distinctIds.size()
                && employees.stream().allMatch(employee ->
                        employee.getStatus() == EmployeeStatus.ACTIVE
                                && !demoProtectionPolicy.isOperationsEmployee(employee.getLoginId()));
    }

    @Override
    public List<Long> findIdsByDepartmentIds(Collection<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return employeeRepository.findIdsByDepartmentIdIn(departmentIds);
    }

    private EmployeeInfo toInfo(Employee employee, ReferenceCache refs) {
        return EmployeeInfo.builder()
                .id(employee.getId())
                .loginId(employee.getLoginId())
                .name(employee.getName())
                .departmentId(employee.getDepartmentId())
                .departmentName(refs.departmentName(employee.getDepartmentId()))
                .positionName(refs.positionName(employee.getPositionId()))
                .status(EmploymentStatus.valueOf(employee.getStatus().name()))
                .build();
    }

    public boolean isLoginIdAvailable(String loginId) {
        return !employeeRepository.existsByLoginId(loginId);
    }

    public EmployeeProfileResponse getMyInfo(String loginId) {
        Employee employee = employeeRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        RoleInfo roleInfo = roleApi.getById(employee.getRoleId());
        List<MenuPermission> menuPermissions = roleApi.getMenuPermissionsByRoleId(employee.getRoleId());

        DepartmentInfo departmentInfo = Optional.ofNullable(employee.getDepartmentId())
                .map(departmentApi::getById)
                .orElse(null);

        PositionInfo positionInfo = Optional.ofNullable(employee.getPositionId())
                .map(positionApi::getById)
                .orElse(null);

        return employeeMapper.toProfileResponse(employee, departmentInfo, positionInfo, roleInfo, menuPermissions);
    }

    public PageResponse<EmployeeSummaryResponse> search(String viewerLoginId, EmployeeSearchCondition condition, Pageable pageable) {
        EmployeeSearchCondition visibleCondition = condition.withExcludedLoginId(
                demoProtectionPolicy.hiddenOperationsEmployeeLoginId(viewerLoginId));
        Page<Employee> page = employeeRepository.search(visibleCondition, pageable);
        ReferenceCache refs = loadReferences(page.getContent());
        return PageResponse.of(page.map(employee -> toSummary(employee, refs)));
    }

    public PageResponse<EmployeeReferenceResponse> searchReference(
            EmployeeSearchCondition condition,
            Pageable pageable
    ) {
        Page<Employee> page = employeeRepository.search(
                condition.withExcludedLoginId(demoProtectionPolicy.recoveryOperationsEmployeeLoginId()),
                pageable
        );
        return toReferencePage(page);
    }

    public PageResponse<EmployeeReferenceResponse> searchContractReference(
            EmployeeSearchCondition condition,
            Pageable pageable
    ) {
        Page<Employee> page = employeeRepository.searchVisible(
                condition.withExcludedLoginId(demoProtectionPolicy.recoveryOperationsEmployeeLoginId()),
                resolveContractVisibleEmployeeIds().orElse(null),
                pageable
        );
        return toReferencePage(page);
    }

    private PageResponse<EmployeeReferenceResponse> toReferencePage(Page<Employee> page) {
        OrganizationReferenceCache refs = loadOrganizationReferences(page.getContent());
        return PageResponse.of(page.map(employee -> EmployeeReferenceResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .departmentName(refs.departmentName(employee.getDepartmentId()))
                .positionName(refs.positionName(employee.getPositionId()))
                .status(employee.getStatus())
                .build()));
    }

    private Optional<Set<Long>> resolveContractVisibleEmployeeIds() {
        DataScope scope = dataScopeResolver.resolve(Menu.CONTRACTS);
        if (scope == DataScope.ALL) {
            return Optional.empty();
        }
        DataScopeContext context = dataScopeContextProvider.current();
        return Optional.of(switch (scope) {
            case ALL -> Set.of();
            case SELF -> context.employeeId() == null
                    ? Set.of()
                    : Set.of(context.employeeId());
            case DEPARTMENT -> context.departmentId() == null
                    ? Set.of()
                    : Set.copyOf(employeeRepository.findIdsByDepartmentIdIn(
                            List.of(context.departmentId())
                    ));
            case DEPARTMENT_TREE -> context.departmentSubtreeIds().isEmpty()
                    ? Set.of()
                    : Set.copyOf(employeeRepository.findIdsByDepartmentIdIn(
                            context.departmentSubtreeIds()
                    ));
        });
    }

    public byte[] exportExcel(String viewerLoginId, EmployeeSearchCondition condition, Sort sort) {
        demoExcelExportGuard.assertExportAllowed(DemoExcelExportGuard.Table.EMPLOYEES);
        EmployeeSearchCondition visibleCondition = condition.withExcludedLoginId(
                demoProtectionPolicy.hiddenOperationsEmployeeLoginId(viewerLoginId));
        List<Employee> employees = employeeRepository.searchAll(visibleCondition, sort);
        ReferenceCache refs = loadReferences(employees);
        List<EmployeeSummaryResponse> rows = employees.stream()
                .map(employee -> toSummary(employee, refs))
                .toList();
        return employeeExcelExporter.export(rows);
    }

    private EmployeeSummaryResponse toSummary(Employee employee, ReferenceCache refs) {
        return employeeMapper.toSummaryResponse(
                employee,
                refs.departmentName(employee.getDepartmentId()),
                refs.positionName(employee.getPositionId()),
                refs.roleName(employee.getRoleId())
        );
    }

    public EmployeeDetailResponse getDetail(String viewerLoginId, Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        if (demoProtectionPolicy.isEmployeeHiddenFrom(viewerLoginId, employee.getLoginId())) {
            throw new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }

        DepartmentInfo departmentInfo = Optional.ofNullable(employee.getDepartmentId())
                .map(departmentApi::getById)
                .orElse(null);
        PositionInfo positionInfo = Optional.ofNullable(employee.getPositionId())
                .map(positionApi::getById)
                .orElse(null);
        RoleInfo roleInfo = roleApi.getById(employee.getRoleId());

        return employeeMapper.toDetailResponse(employee, departmentInfo, positionInfo, roleInfo);
    }

    @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.CREATE, targetType = "Employee", targetIdFromReturn = true)
    @Transactional
    public Long create(EmployeeCreateRequest request) {
        if (employeeRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(EmployeeErrorCode.DUPLICATE_LOGIN_ID);
        }
        validateReferences(request.roleId(), request.departmentId(), request.positionId());

        Employee employee = Employee.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()))
                .joinDate(request.joinDate())
                .birthDate(request.birthDate())
                .status(request.status())
                .roleId(request.roleId())
                .departmentId(request.departmentId())
                .positionId(request.positionId())
                .build();

        return employeeRepository.save(employee).getId();
    }

    @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.UPDATE, targetType = "Employee", targetIdParam = "id")
    @Transactional
    public void update(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        boolean passwordChanged = request.newPassword() != null && !request.newPassword().isEmpty();
        demoProtectionPolicy.assertProtectedEmployeeUpdateAllowed(
                employee.getLoginId(),
                !Objects.equals(employee.getStatus(), request.status()),
                !Objects.equals(employee.getRoleId(), request.roleId()),
                passwordChanged
        );
        validateReferences(request.roleId(), request.departmentId(), request.positionId());

        employee.update(
                request.name(),
                request.email(),
                request.phone(),
                toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()),
                request.joinDate(),
                request.birthDate(),
                request.status(),
                request.roleId(),
                request.departmentId(),
                request.positionId()
        );

        // newPassword 가 채워진 경우에만 비밀번호 변경 (null/빈 값이면 기존 유지)
        if (passwordChanged) {
            employee.changePassword(passwordEncoder.encode(request.newPassword()));
        }
    }

    @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.DELETE, targetType = "Employee", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        demoProtectionPolicy.assertEmployeeDeletionAllowed(employee.getLoginId());
        employeeRepository.delete(employee);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션 내에서 ID 별로 단건 delete 를 호출.
     * 한 건이라도 실패하면 전체 롤백 (BusinessException 전파).
     */
    @Auditable(menu = Menu.EMPLOYEES, action = AuditAction.DELETE, targetType = "Employee")
    @Transactional
    public void deleteAll(List<Long> ids) {
        RequestCollectionPolicy.requireBoundedMutationBatch(ids);
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    private Address toAddress(String zipCode, String roadAddress, String detailAddress) {
        if (zipCode == null && roadAddress == null && detailAddress == null) {
            return null;
        }
        return Address.builder()
                .zipCode(zipCode)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .build();
    }

    private void validateReferences(Long roleId, Long departmentId, Long positionId) {
        roleApi.getById(roleId);
        if (departmentId != null) {
            departmentApi.getById(departmentId);
        }
        if (positionId != null) {
            positionApi.getById(positionId);
        }
    }

    private ReferenceCache loadReferences(List<Employee> employees) {
        List<Long> deptIds = distinctIds(employees.stream().map(Employee::getDepartmentId));
        List<Long> posIds = distinctIds(employees.stream().map(Employee::getPositionId));
        List<Long> roleIds = distinctIds(employees.stream().map(Employee::getRoleId));

        Map<Long, String> deptNames = departmentApi.findByIds(deptIds).stream()
                .collect(toMap(DepartmentInfo::id, DepartmentInfo::name));
        Map<Long, String> posNames = positionApi.findByIds(posIds).stream()
                .collect(toMap(PositionInfo::id, PositionInfo::name));
        Map<Long, String> roleNames = roleApi.findByIds(roleIds).stream()
                .collect(toMap(RoleInfo::id, RoleInfo::name));

        return new ReferenceCache(deptNames, posNames, roleNames);
    }

    private OrganizationReferenceCache loadOrganizationReferences(List<Employee> employees) {
        List<Long> departmentIds = distinctIds(employees.stream().map(Employee::getDepartmentId));
        List<Long> positionIds = distinctIds(employees.stream().map(Employee::getPositionId));
        Map<Long, String> departmentNames = departmentApi.findByIds(departmentIds).stream()
                .collect(toMap(DepartmentInfo::id, DepartmentInfo::name));
        Map<Long, String> positionNames = positionApi.findByIds(positionIds).stream()
                .collect(toMap(PositionInfo::id, PositionInfo::name));
        return new OrganizationReferenceCache(departmentNames, positionNames);
    }

    private static List<Long> distinctIds(Stream<Long> ids) {
        return ids.filter(java.util.Objects::nonNull).distinct().toList();
    }

    private record ReferenceCache(Map<Long, String> departmentNames,
                                  Map<Long, String> positionNames,
                                  Map<Long, String> roleNames) {

        String departmentName(Long id) {
            return id == null ? null : departmentNames.get(id);
        }

        String positionName(Long id) {
            return id == null ? null : positionNames.get(id);
        }

        String roleName(Long id) {
            return id == null ? null : roleNames.get(id);
        }
    }

    private record OrganizationReferenceCache(Map<Long, String> departmentNames,
                                              Map<Long, String> positionNames) {

        String departmentName(Long id) {
            return id == null ? null : departmentNames.get(id);
        }

        String positionName(Long id) {
            return id == null ? null : positionNames.get(id);
        }
    }
}

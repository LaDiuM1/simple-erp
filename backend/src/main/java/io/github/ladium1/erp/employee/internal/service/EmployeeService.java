package io.github.ladium1.erp.employee.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.internal.dto.EmployeeCreateRequest;
import io.github.ladium1.erp.employee.internal.dto.EmployeeDetailResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeUpdateRequest;
import io.github.ladium1.erp.employee.internal.entity.Address;
import io.github.ladium1.erp.employee.internal.entity.Employee;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService implements EmployeeApi {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final RoleApi roleApi;
    private final DepartmentApi departmentApi;
    private final PositionApi positionApi;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeExcelExporter employeeExcelExporter;

    @Override
    public Long getRoleIdByLoginId(String loginId) {
        return employeeRepository.findByLoginId(loginId)
                .map(Employee::getRoleId)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
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

    public PageResponse<EmployeeSummaryResponse> search(EmployeeSearchCondition condition, Pageable pageable) {
        Page<Employee> page = employeeRepository.search(condition, pageable);
        ReferenceCache refs = loadReferences(page.getContent());
        return PageResponse.of(page.map(employee -> toSummary(employee, refs)));
    }

    public byte[] exportExcel(EmployeeSearchCondition condition, Sort sort) {
        List<Employee> employees = employeeRepository.searchAll(condition, sort);
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

    public EmployeeDetailResponse getDetail(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        DepartmentInfo departmentInfo = Optional.ofNullable(employee.getDepartmentId())
                .map(departmentApi::getById)
                .orElse(null);
        PositionInfo positionInfo = Optional.ofNullable(employee.getPositionId())
                .map(positionApi::getById)
                .orElse(null);
        RoleInfo roleInfo = roleApi.getById(employee.getRoleId());

        return employeeMapper.toDetailResponse(employee, departmentInfo, positionInfo, roleInfo);
    }

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
                .status(request.status())
                .roleId(request.roleId())
                .departmentId(request.departmentId())
                .positionId(request.positionId())
                .build();

        return employeeRepository.save(employee).getId();
    }

    @Transactional
    public void update(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));
        validateReferences(request.roleId(), request.departmentId(), request.positionId());

        employee.update(
                request.name(),
                request.email(),
                request.phone(),
                toAddress(request.zipCode(), request.roadAddress(), request.detailAddress()),
                request.joinDate(),
                request.status(),
                request.roleId(),
                request.departmentId(),
                request.positionId()
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
        }
        employeeRepository.deleteById(id);
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
}

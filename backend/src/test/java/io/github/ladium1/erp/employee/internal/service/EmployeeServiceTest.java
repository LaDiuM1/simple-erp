package io.github.ladium1.erp.employee.internal.service;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;
import io.github.ladium1.erp.global.security.DataScopeContextProvider;
import io.github.ladium1.erp.global.security.DataScopeResolver;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeReferenceResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSearchCondition;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.mapper.EmployeeMapper;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;
    @Mock private RoleApi roleApi;
    @Mock private DepartmentApi departmentApi;
    @Mock private PositionApi positionApi;
    @Mock private DataScopeResolver dataScopeResolver;
    @Mock private DataScopeContextProvider dataScopeContextProvider;

    private final String TEST_ID = "testUser";

    @Test
    @DisplayName("계약 직원 참조는 계약의 부서 데이터 범위를 저장소 조건으로 강제한다")
    void search_contract_reference_applies_contract_department_scope() {
        PageRequest pageable = PageRequest.of(0, 20);
        EmployeeSearchCondition condition = new EmployeeSearchCondition(
                null, null, null, null, null, EmployeeStatus.ACTIVE);
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.DEPARTMENT);
        given(dataScopeContextProvider.current())
                .willReturn(new DataScopeContext(5L, 10L, Set.of(10L)));
        given(employeeRepository.findIdsByDepartmentIdIn(List.of(10L)))
                .willReturn(List.of(5L, 7L));
        given(employeeRepository.searchVisible(condition, Set.of(5L, 7L), pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        employeeService.searchContractReference(condition, pageable);

        verify(employeeRepository).searchVisible(condition, Set.of(5L, 7L), pageable);
    }

    @Test
    @DisplayName("계약 SELF 범위에서 인증 직원을 식별하지 못하면 직원 참조는 빈 결과로 제한한다")
    void search_contract_reference_fails_closed_without_employee_context() {
        PageRequest pageable = PageRequest.of(0, 20);
        EmployeeSearchCondition condition = new EmployeeSearchCondition(
                null, null, null, null, null, EmployeeStatus.ACTIVE);
        given(dataScopeResolver.resolve(Menu.CONTRACTS)).willReturn(DataScope.SELF);
        given(dataScopeContextProvider.current()).willReturn(DataScopeContext.anonymous());
        given(employeeRepository.searchVisible(condition, Set.of(), pageable))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        employeeService.searchContractReference(condition, pageable);

        verify(employeeRepository).searchVisible(condition, Set.of(), pageable);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void get_my_info_success() {
        // given
        String testName = "테스트이름";

        Long testRoleId = 10L;
        String testRoleCode = "TEST_ROLE";
        String testRoleName = "테스트권한명";

        Long testDeptId = 20L;
        String testDepartment = "테스트부서";

        Long testPosId = 30L;
        String testPosition = "테스트직책";

        Employee mockEmployee = Employee.builder()
                .loginId(TEST_ID)
                .name(testName)
                .roleId(testRoleId)
                .departmentId(testDeptId)
                .positionId(testPosId)
                .build();

        RoleInfo mockRoleInfo = RoleInfo.builder()
                .id(testRoleId)
                .code(testRoleCode)
                .name(testRoleName)
                .build();

        MenuPermission mockPermission = new MenuPermission(Menu.EMPLOYEES, true, true);
        List<MenuPermission> mockPermissions = List.of(mockPermission);

        DepartmentInfo mockDeptInfo = DepartmentInfo.builder()
                .id(testDeptId)
                .name(testDepartment)
                .build();

        PositionInfo mockPosInfo = PositionInfo.builder()
                .id(testPosId)
                .name(testPosition)
                .build();

        EmployeeProfileResponse expectedResponse = EmployeeProfileResponse.builder()
                .loginId(TEST_ID)
                .name(testName)
                .departmentName(testDepartment)
                .positionName(testPosition)
                .roleName(testRoleName)
                .roleCode(testRoleCode)
                .menuPermissions(mockPermissions)
                .build();

        given(employeeRepository.findByLoginId(TEST_ID)).willReturn(Optional.of(mockEmployee));
        given(roleApi.getById(testRoleId)).willReturn(mockRoleInfo);
        given(roleApi.getMenuPermissionsByRoleId(testRoleId)).willReturn(mockPermissions);
        given(departmentApi.getById(testDeptId)).willReturn(mockDeptInfo);
        given(positionApi.getById(testPosId)).willReturn(mockPosInfo);
        given(employeeMapper.toProfileResponse(mockEmployee, mockDeptInfo, mockPosInfo, mockRoleInfo, mockPermissions))
                .willReturn(expectedResponse);

        // when
        EmployeeProfileResponse actualResponse = employeeService.getMyInfo(TEST_ID);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.loginId()).isEqualTo(TEST_ID);
        assertThat(actualResponse.name()).isEqualTo(testName);
        assertThat(actualResponse.departmentName()).isEqualTo(testDepartment);
        assertThat(actualResponse.roleCode()).isEqualTo(testRoleCode);

        verify(employeeMapper).toProfileResponse(mockEmployee, mockDeptInfo, mockPosInfo, mockRoleInfo, mockPermissions);
    }

    @Test
    @DisplayName("직원 정보 없음")
    void get_my_info_fail_employee_not_found() {
        // given
        given(employeeRepository.findByLoginId(TEST_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> employeeService.getMyInfo(TEST_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", EmployeeErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    @DisplayName("직원 참조 조회는 조직과 상태 정보만 구성한다")
    void search_reference_returns_minimum_organization_fields() {
        Employee employee = Employee.builder()
                .loginId("private-login")
                .name("홍길동")
                .email("private@example.com")
                .phone("010-0000-0000")
                .birthDate(java.time.LocalDate.of(1990, 1, 1))
                .joinDate(java.time.LocalDate.of(2020, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .roleId(10L)
                .departmentId(20L)
                .positionId(30L)
                .build();
        ReflectionTestUtils.setField(employee, "id", 1L);
        PageRequest pageable = PageRequest.of(0, 20);
        given(employeeRepository.search(any(EmployeeSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(employee), pageable, 1));
        given(departmentApi.findByIds(List.of(20L))).willReturn(List.of(
                DepartmentInfo.builder().id(20L).name("영업팀").build()));
        given(positionApi.findByIds(List.of(30L))).willReturn(List.of(
                PositionInfo.builder().id(30L).name("대리").build()));

        EmployeeReferenceResponse result = employeeService.searchReference(
                new EmployeeSearchCondition(null, "홍", null, null, null, null),
                pageable
        ).content().getFirst();

        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.departmentName()).isEqualTo("영업팀");
        assertThat(result.positionName()).isEqualTo("대리");
        assertThat(result.status()).isEqualTo(EmployeeStatus.ACTIVE);
        verify(roleApi, never()).findByIds(any());
    }

    @Test
    @DisplayName("현재 재직 판정 — 휴직 직원은 재직 관계에 포함")
    void currently_employed_includes_leave() {
        Employee employee = Employee.builder()
                .loginId("leave-user")
                .name("휴직자")
                .status(EmployeeStatus.LEAVE)
                .build();
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));

        assertThat(employeeService.isCurrentlyEmployed(1L)).isTrue();
    }

    @Test
    @DisplayName("현재 재직 판정 — 퇴사 직원은 제외")
    void currently_employed_excludes_resigned() {
        Employee employee = Employee.builder()
                .loginId("resigned-user")
                .name("퇴사자")
                .status(EmployeeStatus.RESIGNED)
                .build();
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));

        assertThat(employeeService.isCurrentlyEmployed(1L)).isFalse();
    }

    @Test
    @DisplayName("로그인 가능 상태 확인은 퇴사 여부만 조회")
    void login_allowed_checks_non_resigned_account() {
        given(employeeRepository.existsByLoginIdAndStatusNot(TEST_ID, EmployeeStatus.RESIGNED))
                .willReturn(true);

        assertThat(employeeService.isLoginAllowed(TEST_ID)).isTrue();
        verify(employeeRepository).existsByLoginIdAndStatusNot(TEST_ID, EmployeeStatus.RESIGNED);
    }

    @Test
    @DisplayName("존재하지 않거나 퇴사한 계정은 로그인 불가")
    void login_not_allowed_for_missing_or_resigned_account() {
        given(employeeRepository.existsByLoginIdAndStatusNot(TEST_ID, EmployeeStatus.RESIGNED))
                .willReturn(false);

        assertThat(employeeService.isLoginAllowed(TEST_ID)).isFalse();
    }
}

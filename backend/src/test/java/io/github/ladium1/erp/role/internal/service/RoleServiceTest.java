package io.github.ladium1.erp.role.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.role.api.RoleDeletingEvent;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.MenuPermissionRequest;
import io.github.ladium1.erp.role.internal.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.internal.dto.RoleDetailResponse;
import io.github.ladium1.erp.role.internal.dto.RoleSummaryResponse;
import io.github.ladium1.erp.role.internal.dto.RoleUpdateRequest;
import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import io.github.ladium1.erp.role.internal.exception.RoleErrorCode;
import io.github.ladium1.erp.role.internal.mapper.RoleMapper;
import io.github.ladium1.erp.role.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.role.internal.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock private RoleRepository roleRepository;
    @Mock private RoleMenuRepository roleMenuRepository;
    @Mock private RoleMapper roleMapper;
    @Mock private ApplicationEventPublisher eventPublisher;


    @Test
    @DisplayName("getById 성공")
    void get_by_id_success() {
        Role role = mockRole("STAFF", "사원", false);
        RoleInfo info = RoleInfo.builder().id(1L).code("STAFF").name("사원").system(false).build();
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));
        given(roleMapper.toRoleInfo(role)).willReturn(info);

        assertThat(roleService.getById(1L)).isEqualTo(info);
    }

    @Test
    @DisplayName("getById 실패 — ROLE_NOT_FOUND")
    void get_by_id_fail_not_found() {
        given(roleRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.ROLE_NOT_FOUND);
    }


    @Test
    @DisplayName("bootstrapSystemRole — 이미 존재하면 그대로 반환 (저장 안함)")
    void bootstrap_existing() {
        Role existing = mockRole("MASTER", "관리자", true);
        RoleInfo info = RoleInfo.builder().id(1L).code("MASTER").system(true).build();
        given(roleRepository.findByCode("MASTER")).willReturn(Optional.of(existing));
        given(roleMapper.toRoleInfo(existing)).willReturn(info);

        RoleInfo actual = roleService.bootstrapSystemRole("MASTER", "관리자", "전체 권한");

        assertThat(actual).isEqualTo(info);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("bootstrapSystemRole — 신규 생성 시 system=true + 모든 메뉴 read/write 부여")
    void bootstrap_new_creates_with_all_menus() {
        given(roleRepository.findByCode("MASTER")).willReturn(Optional.empty());
        Role saved = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);

        roleService.bootstrapSystemRole("MASTER", "관리자", "전체 권한");

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().isSystem()).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        assertThat(rowsCaptor.getValue()).hasSize(Menu.values().length);
        assertThat(rowsCaptor.getValue()).allMatch(rm -> rm.isCanRead() && rm.isCanWrite());
    }


    @Test
    @DisplayName("isCodeAvailable — 미사용 코드면 true")
    void is_code_available_true() {
        given(roleRepository.existsByCode("NEW")).willReturn(false);
        assertThat(roleService.isCodeAvailable("NEW")).isTrue();
    }

    @Test
    @DisplayName("isCodeAvailable — 빈 문자열은 false (DB 미조회)")
    void is_code_available_blank() {
        assertThat(roleService.isCodeAvailable("")).isFalse();
        assertThat(roleService.isCodeAvailable(null)).isFalse();
        verify(roleRepository, never()).existsByCode(any());
    }


    @Test
    @DisplayName("create 성공 — Role 저장 + 매트릭스 replace")
    void create_success() {
        RoleCreateRequest request = new RoleCreateRequest(
                "MANAGER", "매니저", "팀장",
                List.of(
                        new MenuPermissionRequest(Menu.EMPLOYEES, true, true),
                        new MenuPermissionRequest(Menu.DEPARTMENTS, true, false),
                        new MenuPermissionRequest(Menu.POSITIONS, false, false)
                )
        );
        given(roleRepository.existsByCode("MANAGER")).willReturn(false);
        Role saved = mockRole("MANAGER", "매니저", false);
        ReflectionTestUtils.setField(saved, "id", 5L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);

        Long id = roleService.create(request);

        assertThat(id).isEqualTo(5L);
        // canRead=false/canWrite=false 행은 저장에서 제외 (DB lean)
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        assertThat(rowsCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("create 실패 — 중복 코드 시 DUPLICATE_ROLE_CODE")
    void create_fail_duplicate_code() {
        RoleCreateRequest request = new RoleCreateRequest("MANAGER", "매니저", null, List.of());
        given(roleRepository.existsByCode("MANAGER")).willReturn(true);

        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.DUPLICATE_ROLE_CODE);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create — 매트릭스에 중복 메뉴 포함 시 DUPLICATE_MENU_IN_PERMISSIONS")
    void create_fail_duplicate_menu() {
        RoleCreateRequest request = new RoleCreateRequest(
                "DUP", "중복",  null,
                List.of(
                        new MenuPermissionRequest(Menu.EMPLOYEES, true, true),
                        new MenuPermissionRequest(Menu.EMPLOYEES, true, false)
                )
        );
        given(roleRepository.existsByCode("DUP")).willReturn(false);
        Role saved = mockRole("DUP", "중복", false);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);

        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.DUPLICATE_MENU_IN_PERMISSIONS);
    }

    @Test
    @DisplayName("create — 쓰기=true 인데 읽기=false 면 읽기 자동 보정")
    void create_coerces_read_when_write_true() {
        RoleCreateRequest request = new RoleCreateRequest(
                "X", "임의", null,
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, false, true))
        );
        given(roleRepository.existsByCode("X")).willReturn(false);
        Role saved = mockRole("X", "임의", false);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);

        roleService.create(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        RoleMenu row = rowsCaptor.getValue().getFirst();
        assertThat(row.isCanRead()).isTrue();
        assertThat(row.isCanWrite()).isTrue();
    }


    @Test
    @DisplayName("update — 일반 권한은 매트릭스도 변경됨")
    void update_normal_role_replaces_matrix() {
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));

        RoleUpdateRequest request = new RoleUpdateRequest(
                "사원V2", "수정됨",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, true, false))
        );
        roleService.update(7L, request);

        assertThat(role.getName()).isEqualTo("사원V2");
        verify(roleMenuRepository).deleteAllByRole(role);
        verify(roleMenuRepository).saveAll(any(List.class));
    }

    @Test
    @DisplayName("update — 시스템 권한은 이름/설명만 갱신, 매트릭스 변경 무시")
    void update_system_role_keeps_matrix() {
        Role role = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(role, "id", 1L);
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));

        RoleUpdateRequest request = new RoleUpdateRequest(
                "최고관리자", "변경된 설명",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, false, false))
        );
        roleService.update(1L, request);

        assertThat(role.getName()).isEqualTo("최고관리자");
        verify(roleMenuRepository, never()).deleteAllByRole(any());
        verify(roleMenuRepository, never()).saveAll(any(List.class));
    }


    @Test
    @DisplayName("delete 성공 — 이벤트 발행 + role_menus 삭제 + role 삭제")
    void delete_success() {
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));

        roleService.delete(7L);

        verify(eventPublisher).publishEvent(new RoleDeletingEvent(7L));
        verify(roleMenuRepository).deleteAllByRole(role);
        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("delete 실패 — 시스템 권한은 삭제 불가 (이벤트 미발행)")
    void delete_fail_system_role() {
        Role role = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(role, "id", 1L);
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.SYSTEM_ROLE_PROTECTED);
        verify(eventPublisher, never()).publishEvent(any());
        verify(roleRepository, never()).delete(any());
    }


    @Test
    @DisplayName("getDetail — Menu 전체 행을 채워서 반환 (없는 메뉴는 false/false)")
    void get_detail_fills_full_matrix() {
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));
        // EMPLOYEES 만 부여된 상태
        RoleMenu rm = RoleMenu.builder().role(role).menuCode(Menu.EMPLOYEES).canRead(true).canWrite(false).build();
        given(roleMenuRepository.findAllByRole(role)).willReturn(List.of(rm));

        RoleDetailResponse detail = roleService.getDetail(7L);

        assertThat(detail.menuPermissions()).hasSize(Menu.values().length);
        MenuPermission emp = detail.menuPermissions().stream()
                .filter(p -> p.menuCode() == Menu.EMPLOYEES)
                .findFirst().orElseThrow();
        assertThat(emp.canRead()).isTrue();
        assertThat(emp.canWrite()).isFalse();
        // 다른 메뉴들은 모두 false/false
        assertThat(detail.menuPermissions().stream()
                .filter(p -> p.menuCode() != Menu.EMPLOYEES))
                .allMatch(p -> !p.canRead() && !p.canWrite());
    }


    private Role mockRole(String code, String name, boolean system) {
        return Role.builder()
                .code(code)
                .name(name)
                .description(null)
                .system(system)
                .build();
    }
}

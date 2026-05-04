package io.github.ladium1.erp.role.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.role.api.RoleDeletingEvent;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.MenuPermissionRequest;
import io.github.ladium1.erp.role.internal.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.internal.dto.RoleDetailResponse;
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

import java.util.Arrays;
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
        // given
        Role role = mockRole("STAFF", "사원", false);
        RoleInfo info = RoleInfo.builder().id(1L).code("STAFF").name("사원").system(false).build();
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));
        given(roleMapper.toRoleInfo(role)).willReturn(info);

        // when
        RoleInfo actual = roleService.getById(1L);

        // then
        assertThat(actual).isEqualTo(info);
    }

    @Test
    @DisplayName("getById 실패 — ROLE_NOT_FOUND")
    void get_by_id_fail_not_found() {
        // given
        given(roleRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roleService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.ROLE_NOT_FOUND);
    }

    @Test
    @DisplayName("bootstrapSystemRole — 모든 메뉴 행 정상 시 reconcile 무동작")
    void bootstrap_existing_fully_reconciled() {
        // given
        Role existing = mockRole("MASTER", "관리자", true);
        RoleInfo info = RoleInfo.builder().id(1L).code("MASTER").system(true).build();
        given(roleRepository.findByCode("MASTER")).willReturn(Optional.of(existing));
        given(roleMenuRepository.findAllByRole(existing)).willReturn(allMenuRows(existing));
        given(roleMapper.toRoleInfo(existing)).willReturn(info);

        // when
        RoleInfo actual = roleService.bootstrapSystemRole("MASTER", "관리자", "전체 권한");

        // then
        assertThat(actual).isEqualTo(info);
        verify(roleRepository, never()).save(any());
        verify(roleMenuRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("bootstrapSystemRole — 누락 메뉴만 reconcile")
    void bootstrap_existing_reconciles_missing_menus() {
        // given
        Role existing = mockRole("MASTER", "관리자", true);
        Menu droppedMenu = Menu.values()[Menu.values().length - 1];
        List<RoleMenu> partial = allMenuRows(existing).stream()
                .filter(rm -> rm.getMenuCode() != droppedMenu)
                .toList();
        given(roleRepository.findByCode("MASTER")).willReturn(Optional.of(existing));
        given(roleMenuRepository.findAllByRole(existing)).willReturn(partial);

        // when
        roleService.bootstrapSystemRole("MASTER", "관리자", "전체 권한");

        // then
        verify(roleRepository, never()).save(any());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        assertThat(rowsCaptor.getValue()).hasSize(1);
        RoleMenu added = rowsCaptor.getValue().getFirst();
        assertThat(added.getMenuCode()).isEqualTo(droppedMenu);
        assertThat(added.isCanRead()).isTrue();
        assertThat(added.isCanWrite()).isTrue();
    }

    @Test
    @DisplayName("bootstrapSystemRole — 신규 시 system=true + 모든 메뉴 부여")
    void bootstrap_new_creates_with_all_menus() {
        // given
        given(roleRepository.findByCode("MASTER")).willReturn(Optional.empty());
        Role saved = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);
        given(roleMenuRepository.findAllByRole(saved)).willReturn(List.of());

        // when
        roleService.bootstrapSystemRole("MASTER", "관리자", "전체 권한");

        // then
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
        // given
        given(roleRepository.existsByCode("NEW")).willReturn(false);

        // when & then
        assertThat(roleService.isCodeAvailable("NEW")).isTrue();
    }

    @Test
    @DisplayName("isCodeAvailable — 빈 문자열은 false (DB 미조회)")
    void is_code_available_blank() {
        // when & then
        assertThat(roleService.isCodeAvailable("")).isFalse();
        assertThat(roleService.isCodeAvailable(null)).isFalse();
        verify(roleRepository, never()).existsByCode(any());
    }

    @Test
    @DisplayName("create 성공 — Role 저장 + 매트릭스 replace")
    void create_success() {
        // given
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

        // when
        Long id = roleService.create(request);

        // then
        assertThat(id).isEqualTo(5L);
        // canRead=false / canWrite=false 행은 저장에서 제외 (DB lean)
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        assertThat(rowsCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("create 실패 — 중복 코드 시 DUPLICATE_ROLE_CODE")
    void create_fail_duplicate_code() {
        // given
        RoleCreateRequest request = new RoleCreateRequest("MANAGER", "매니저", null, List.of());
        given(roleRepository.existsByCode("MANAGER")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.DUPLICATE_ROLE_CODE);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 매트릭스 중복 메뉴 시 DUPLICATE_MENU_IN_PERMISSIONS")
    void create_fail_duplicate_menu() {
        // given
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

        // when & then
        assertThatThrownBy(() -> roleService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.DUPLICATE_MENU_IN_PERMISSIONS);
    }

    @Test
    @DisplayName("create — write=true / read=false 면 read 자동 보정")
    void create_coerces_read_when_write_true() {
        // given
        RoleCreateRequest request = new RoleCreateRequest(
                "X", "임의", null,
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, false, true))
        );
        given(roleRepository.existsByCode("X")).willReturn(false);
        Role saved = mockRole("X", "임의", false);
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(roleRepository.save(any(Role.class))).willReturn(saved);

        // when
        roleService.create(request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleMenu>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleMenuRepository).saveAll(rowsCaptor.capture());
        RoleMenu row = rowsCaptor.getValue().getFirst();
        assertThat(row.isCanRead()).isTrue();
        assertThat(row.isCanWrite()).isTrue();
    }

    @Test
    @DisplayName("update — 일반 권한은 매트릭스도 변경")
    void update_normal_role_replaces_matrix() {
        // given
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));

        RoleUpdateRequest request = new RoleUpdateRequest(
                "사원V2", "수정됨",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, true, false))
        );

        // when
        roleService.update(7L, request);

        // then
        assertThat(role.getName()).isEqualTo("사원V2");
        verify(roleMenuRepository).deleteAllByRole(role);
        verify(roleMenuRepository).saveAll(any(List.class));
    }

    @Test
    @DisplayName("update — 시스템 권한은 매트릭스 변경 무시")
    void update_system_role_keeps_matrix() {
        // given
        Role role = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(role, "id", 1L);
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));

        RoleUpdateRequest request = new RoleUpdateRequest(
                "최고관리자", "변경된 설명",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, false, false))
        );

        // when
        roleService.update(1L, request);

        // then
        assertThat(role.getName()).isEqualTo("최고관리자");
        verify(roleMenuRepository, never()).deleteAllByRole(any());
        verify(roleMenuRepository, never()).saveAll(any(List.class));
    }

    @Test
    @DisplayName("delete 성공 — 이벤트 발행 + role_menus 삭제 + role 삭제")
    void delete_success() {
        // given
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));

        // when
        roleService.delete(7L);

        // then
        verify(eventPublisher).publishEvent(new RoleDeletingEvent(7L));
        verify(roleMenuRepository).deleteAllByRole(role);
        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("delete 실패 — 시스템 권한은 삭제 불가")
    void delete_fail_system_role() {
        // given
        Role role = mockRole("MASTER", "관리자", true);
        ReflectionTestUtils.setField(role, "id", 1L);
        given(roleRepository.findById(1L)).willReturn(Optional.of(role));

        // when & then
        assertThatThrownBy(() -> roleService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RoleErrorCode.SYSTEM_ROLE_PROTECTED);
        verify(eventPublisher, never()).publishEvent(any());
        verify(roleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getDetail — 전체 메뉴 행 채워서 반환")
    void get_detail_fills_full_matrix() {
        // given
        Role role = mockRole("STAFF", "사원", false);
        ReflectionTestUtils.setField(role, "id", 7L);
        given(roleRepository.findById(7L)).willReturn(Optional.of(role));
        // EMPLOYEES 만 부여된 상태
        RoleMenu rm = RoleMenu.builder().role(role).menuCode(Menu.EMPLOYEES).canRead(true).canWrite(false).build();
        given(roleMenuRepository.findAllByRole(role)).willReturn(List.of(rm));

        // when
        RoleDetailResponse detail = roleService.getDetail(7L);

        // then
        assertThat(detail.menuPermissions()).hasSize(Menu.values().length);
        MenuPermission emp = detail.menuPermissions().stream()
                .filter(p -> p.menuCode() == Menu.EMPLOYEES)
                .findFirst().orElseThrow();
        assertThat(emp.canRead()).isTrue();
        assertThat(emp.canWrite()).isFalse();
        assertThat(detail.menuPermissions().stream()
                .filter(p -> p.menuCode() != Menu.EMPLOYEES))
                .allMatch(p -> !p.canRead() && !p.canWrite());
    }


    private List<RoleMenu> allMenuRows(Role role) {
        return Arrays.stream(Menu.values())
                .map(m -> RoleMenu.builder()
                        .role(role)
                        .menuCode(m)
                        .canRead(true)
                        .canWrite(true)
                        .dataScope(DataScope.ALL)
                        .build())
                .toList();
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

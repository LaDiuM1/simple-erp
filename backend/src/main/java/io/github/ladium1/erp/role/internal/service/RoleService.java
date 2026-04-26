package io.github.ladium1.erp.role.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.RoleDeletingEvent;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.MenuPermissionRequest;
import io.github.ladium1.erp.role.internal.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.internal.dto.RoleDetailResponse;
import io.github.ladium1.erp.role.internal.dto.RoleSearchCondition;
import io.github.ladium1.erp.role.internal.dto.RoleSummaryResponse;
import io.github.ladium1.erp.role.internal.dto.RoleUpdateRequest;
import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import io.github.ladium1.erp.role.internal.exception.RoleErrorCode;
import io.github.ladium1.erp.role.internal.mapper.RoleMapper;
import io.github.ladium1.erp.role.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.role.internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService implements RoleApi {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleMapper roleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RoleInfo getById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toRoleInfo)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
    }

    @Override
    public Optional<RoleInfo> findByCode(String code) {
        return roleRepository.findByCode(code)
                .map(roleMapper::toRoleInfo);
    }

    @Override
    @Transactional
    public RoleInfo bootstrapSystemRole(String code, String name, String description) {
        Optional<Role> existing = roleRepository.findByCode(code);
        if (existing.isPresent()) {
            return roleMapper.toRoleInfo(existing.get());
        }
        Role saved = roleRepository.save(Role.builder()
                .code(code)
                .name(name)
                .description(description)
                .system(true)
                .build());
        // 모든 메뉴에 read/write 부여
        List<RoleMenu> rows = Arrays.stream(Menu.values())
                .map(menu -> RoleMenu.builder()
                        .role(saved)
                        .menuCode(menu)
                        .canRead(true)
                        .canWrite(true)
                        .build())
                .toList();
        roleMenuRepository.saveAll(rows);
        return roleMapper.toRoleInfo(saved);
    }

    @Override
    public List<MenuPermission> getMenuPermissionsByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
        return roleMenuRepository.findAllByRole(role).stream()
                .map(rm -> new MenuPermission(rm.getMenuCode(), rm.isCanRead(), rm.isCanWrite()))
                .toList();
    }

    @Override
    public List<RoleInfo> findAll() {
        return roleMapper.toRoleInfos(
                roleRepository.findAll(Sort.by("name").ascending())
        );
    }

    @Override
    public List<RoleInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return roleMapper.toRoleInfos(roleRepository.findAllById(ids));
    }

    public PageResponse<RoleSummaryResponse> search(RoleSearchCondition condition, Pageable pageable) {
        Page<Role> page = roleRepository.search(condition, pageable);
        return PageResponse.of(page.map(roleMapper::toSummaryResponse));
    }

    public RoleDetailResponse getDetail(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
        List<MenuPermission> permissions = buildFullMatrix(role);
        return RoleDetailResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .system(role.isSystem())
                .menuPermissions(permissions)
                .build();
    }

    /**
     * 사용자 입력 코드의 사용 가능 여부 — 등록 화면의 디바운스 중복 검사 용.
     */
    public boolean isCodeAvailable(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return !roleRepository.existsByCode(code.trim());
    }

    @Transactional
    public Long create(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new BusinessException(RoleErrorCode.DUPLICATE_ROLE_CODE);
        }
        Role saved = roleRepository.save(Role.builder()
                .code(request.code().trim())
                .name(request.name().trim())
                .description(trimToNull(request.description()))
                .system(false)
                .build());
        replaceMatrix(saved, request.menuPermissions());
        return saved.getId();
    }

    @Transactional
    public void update(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
        role.update(request.name().trim(), trimToNull(request.description()));
        // 시스템 권한은 매트릭스 변경 차단 (FE 가 readonly 로 막지만 이중 방어)
        if (!role.isSystem()) {
            replaceMatrix(role, request.menuPermissions());
        }
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));
        if (role.isSystem()) {
            throw new BusinessException(RoleErrorCode.SYSTEM_ROLE_PROTECTED);
        }
        // 다른 모듈 (직원 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new RoleDeletingEvent(id));
        roleMenuRepository.deleteAllByRole(role);
        roleRepository.delete(role);
    }

    /**
     * Role 의 RoleMenu 행을 요청 매트릭스로 통째로 교체.
     * - canWrite=true 면서 canRead=false 인 행은 canRead=true 로 자동 보정.
     * - canRead/canWrite 둘 다 false 인 행은 저장하지 않음 (DB lean).
     * - 중복 메뉴 거부.
     */
    private void replaceMatrix(Role role, List<MenuPermissionRequest> requested) {
        roleMenuRepository.deleteAllByRole(role);
        if (requested == null || requested.isEmpty()) {
            return;
        }
        Set<Menu> seen = new HashSet<>();
        List<RoleMenu> rows = requested.stream()
                .filter(p -> {
                    if (!seen.add(p.menuCode())) {
                        throw new BusinessException(RoleErrorCode.DUPLICATE_MENU_IN_PERMISSIONS);
                    }
                    return p.canRead() || p.canWrite();
                })
                .map(p -> RoleMenu.builder()
                        .role(role)
                        .menuCode(p.menuCode())
                        .canRead(p.canRead() || p.canWrite())
                        .canWrite(p.canWrite())
                        .build())
                .toList();
        roleMenuRepository.saveAll(rows);
    }

    /**
     * Menu 전체 행을 채워서 반환 — DB 에 행이 없는 메뉴는 canRead=false/canWrite=false 로.
     */
    private List<MenuPermission> buildFullMatrix(Role role) {
        Map<Menu, RoleMenu> stored = roleMenuRepository.findAllByRole(role).stream()
                .collect(toMap(RoleMenu::getMenuCode, rm -> rm));
        return Arrays.stream(Menu.values())
                .map(m -> {
                    RoleMenu rm = stored.get(m);
                    if (rm == null) {
                        return new MenuPermission(m, false, false);
                    }
                    return new MenuPermission(m, rm.isCanRead(), rm.isCanWrite());
                })
                .toList();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

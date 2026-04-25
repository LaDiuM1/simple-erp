package io.github.ladium1.erp.role.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import io.github.ladium1.erp.role.internal.exception.RoleErrorCode;
import io.github.ladium1.erp.role.internal.mapper.RoleMapper;
import io.github.ladium1.erp.role.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.role.internal.repository.RoleRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService implements RoleApi {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleInfo getById(@Validated @NotNull Long id) {
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
    public RoleInfo createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new BusinessException(RoleErrorCode.DUPLICATE_ROLE_CODE);
        }

        Role newRole = roleMapper.toEntity(request);
        return roleMapper.toRoleInfo(roleRepository.save(newRole));
    }

    @Override
    @Transactional
    public void assignMenuPermissions(Long roleId, List<Menu> menus) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));

        // 호출된 권한에 주어진 메뉴에 대한 모든 권한 할당
        List<RoleMenu> roleMenus = menus.stream()
                .map(menu -> RoleMenu.builder()
                        .role(role)
                        .menuCode(menu)
                        .canRead(true)
                        .canWrite(true)
                        .build())
                .toList();

        roleMenuRepository.saveAll(roleMenus);
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
}

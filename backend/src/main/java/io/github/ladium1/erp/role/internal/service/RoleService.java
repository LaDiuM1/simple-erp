package io.github.ladium1.erp.role.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.role.RoleApi;
import io.github.ladium1.erp.role.RoleCreateRequest;
import io.github.ladium1.erp.role.RoleInfo;
import io.github.ladium1.erp.role.internal.entity.Role;
import io.github.ladium1.erp.role.internal.entity.RoleMenu;
import io.github.ladium1.erp.role.internal.exception.RoleErrorCode;
import io.github.ladium1.erp.role.internal.mapper.RoleMapper;
import io.github.ladium1.erp.role.internal.repository.RoleMenuRepository;
import io.github.ladium1.erp.role.internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public RoleInfo createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new BusinessException(RoleErrorCode.DUPLICATE_ROLE_CODE);
        }

        Role newRole = roleMapper.toEntity(request);
        return roleMapper.toRoleInfo(roleRepository.save(newRole));
    }

    @Override
    @Transactional
    public void assignMenuPermissions(Long roleId, List<Long> menuIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));

        // 호출된 권한에 주어진 메뉴에 대한 모든 권한 할당
        List<RoleMenu> roleMenus = menuIds.stream()
                .map(menuId -> RoleMenu.builder()
                        .role(role)
                        .menuId(menuId)
                        .canRead(true)
                        .canWrite(true)
                        .build())
                .toList();

        roleMenuRepository.saveAll(roleMenus);
    }
}

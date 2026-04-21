package io.github.ladium1.erp.role.internal.mapper;

import io.github.ladium1.erp.role.RoleCreateRequest;
import io.github.ladium1.erp.role.RoleInfo;
import io.github.ladium1.erp.role.internal.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleInfo toRoleInfo(Role role);

    Role toEntity(RoleCreateRequest request);

}
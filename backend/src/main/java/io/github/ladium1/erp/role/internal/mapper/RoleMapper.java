package io.github.ladium1.erp.role.internal.mapper;

import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.RoleSummaryResponse;
import io.github.ladium1.erp.role.internal.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleInfo toRoleInfo(Role role);

    List<RoleInfo> toRoleInfos(List<Role> roles);

    RoleSummaryResponse toSummaryResponse(Role role);
}

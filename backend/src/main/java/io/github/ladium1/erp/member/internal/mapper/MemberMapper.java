package io.github.ladium1.erp.member.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(source = "member.id", target = "id")
    @Mapping(source = "member.loginId", target = "loginId")
    @Mapping(source = "member.name", target = "name")
    @Mapping(source = "departmentInfo.name", target = "departmentName")
    @Mapping(source = "positionInfo.name", target = "positionName")
    @Mapping(source = "roleInfo.name", target = "roleName")
    @Mapping(source = "roleInfo.code", target = "roleCode")
    MemberProfileResponse toProfileResponse(Member member,
                                            DepartmentInfo departmentInfo,
                                            PositionInfo positionInfo,
                                            RoleInfo roleInfo,
                                            List<MenuPermission> menuPermissions);


}
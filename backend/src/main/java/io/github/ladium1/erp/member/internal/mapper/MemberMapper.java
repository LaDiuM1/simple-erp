package io.github.ladium1.erp.member.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.member.internal.dto.MemberDetailResponse;
import io.github.ladium1.erp.member.internal.dto.MemberProfileResponse;
import io.github.ladium1.erp.member.internal.dto.MemberSummaryResponse;
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

    @Mapping(source = "member.id", target = "id")
    @Mapping(source = "member.loginId", target = "loginId")
    @Mapping(source = "member.name", target = "name")
    @Mapping(source = "member.email", target = "email")
    @Mapping(source = "member.phone", target = "phone")
    @Mapping(source = "member.joinDate", target = "joinDate")
    @Mapping(source = "member.status", target = "status")
    @Mapping(source = "departmentName", target = "departmentName")
    @Mapping(source = "positionName", target = "positionName")
    @Mapping(source = "roleName", target = "roleName")
    MemberSummaryResponse toSummaryResponse(Member member,
                                            String departmentName,
                                            String positionName,
                                            String roleName);

    @Mapping(source = "member.id", target = "id")
    @Mapping(source = "member.loginId", target = "loginId")
    @Mapping(source = "member.name", target = "name")
    @Mapping(source = "member.email", target = "email")
    @Mapping(source = "member.phone", target = "phone")
    @Mapping(source = "member.address.zipCode", target = "zipCode")
    @Mapping(source = "member.address.roadAddress", target = "roadAddress")
    @Mapping(source = "member.address.detailAddress", target = "detailAddress")
    @Mapping(source = "member.joinDate", target = "joinDate")
    @Mapping(source = "member.status", target = "status")
    @Mapping(source = "member.departmentId", target = "departmentId")
    @Mapping(source = "departmentInfo.name", target = "departmentName")
    @Mapping(source = "member.positionId", target = "positionId")
    @Mapping(source = "positionInfo.name", target = "positionName")
    @Mapping(source = "member.roleId", target = "roleId")
    @Mapping(source = "roleInfo.name", target = "roleName")
    MemberDetailResponse toDetailResponse(Member member,
                                          DepartmentInfo departmentInfo,
                                          PositionInfo positionInfo,
                                          RoleInfo roleInfo);

}

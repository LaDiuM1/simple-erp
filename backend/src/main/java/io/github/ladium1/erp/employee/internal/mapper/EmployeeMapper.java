package io.github.ladium1.erp.employee.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.employee.internal.dto.EmployeeDetailResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import io.github.ladium1.erp.employee.internal.entity.Employee;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "employee.id", target = "id")
    @Mapping(source = "employee.loginId", target = "loginId")
    @Mapping(source = "employee.name", target = "name")
    @Mapping(source = "departmentInfo.name", target = "departmentName")
    @Mapping(source = "positionInfo.name", target = "positionName")
    @Mapping(source = "roleInfo.name", target = "roleName")
    @Mapping(source = "roleInfo.code", target = "roleCode")
    EmployeeProfileResponse toProfileResponse(Employee employee,
                                              DepartmentInfo departmentInfo,
                                              PositionInfo positionInfo,
                                              RoleInfo roleInfo,
                                              List<MenuPermission> menuPermissions);

    @Mapping(source = "employee.id", target = "id")
    @Mapping(source = "employee.loginId", target = "loginId")
    @Mapping(source = "employee.name", target = "name")
    @Mapping(source = "employee.email", target = "email")
    @Mapping(source = "employee.phone", target = "phone")
    @Mapping(source = "employee.joinDate", target = "joinDate")
    @Mapping(source = "employee.status", target = "status")
    @Mapping(source = "departmentName", target = "departmentName")
    @Mapping(source = "positionName", target = "positionName")
    @Mapping(source = "roleName", target = "roleName")
    EmployeeSummaryResponse toSummaryResponse(Employee employee,
                                              String departmentName,
                                              String positionName,
                                              String roleName);

    @Mapping(source = "employee.id", target = "id")
    @Mapping(source = "employee.loginId", target = "loginId")
    @Mapping(source = "employee.name", target = "name")
    @Mapping(source = "employee.email", target = "email")
    @Mapping(source = "employee.phone", target = "phone")
    @Mapping(source = "employee.address.zipCode", target = "zipCode")
    @Mapping(source = "employee.address.roadAddress", target = "roadAddress")
    @Mapping(source = "employee.address.detailAddress", target = "detailAddress")
    @Mapping(source = "employee.joinDate", target = "joinDate")
    @Mapping(source = "employee.status", target = "status")
    @Mapping(source = "employee.departmentId", target = "departmentId")
    @Mapping(source = "departmentInfo.name", target = "departmentName")
    @Mapping(source = "employee.positionId", target = "positionId")
    @Mapping(source = "positionInfo.name", target = "positionName")
    @Mapping(source = "employee.roleId", target = "roleId")
    @Mapping(source = "roleInfo.name", target = "roleName")
    EmployeeDetailResponse toDetailResponse(Employee employee,
                                            DepartmentInfo departmentInfo,
                                            PositionInfo positionInfo,
                                            RoleInfo roleInfo);

}

package io.github.ladium1.erp.department.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.entity.Department;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentInfo toDepartmentInfo(Department department);

    List<DepartmentInfo> toDepartmentInfos(List<Department> departments);

}
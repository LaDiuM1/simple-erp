package io.github.ladium1.erp.department.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.dto.DepartmentDetailResponse;
import io.github.ladium1.erp.department.internal.dto.DepartmentSummaryResponse;
import io.github.ladium1.erp.department.internal.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "parent.id", target = "parentId")
    DepartmentInfo toDepartmentInfo(Department department);

    List<DepartmentInfo> toDepartmentInfos(List<Department> departments);

    @Mapping(source = "parent.name", target = "parentName")
    DepartmentSummaryResponse toSummaryResponse(Department department);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    DepartmentDetailResponse toDetailResponse(Department department);
}

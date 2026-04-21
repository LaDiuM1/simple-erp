package io.github.ladium1.erp.position.internal.mapper;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;
import io.github.ladium1.erp.department.internal.entity.Department;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.entity.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    PositionInfo toPositionInfo(Position position);

}
package io.github.ladium1.erp.position.internal.mapper;

import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.dto.PositionDetailResponse;
import io.github.ladium1.erp.position.internal.dto.PositionSummaryResponse;
import io.github.ladium1.erp.position.internal.entity.Position;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    PositionInfo toPositionInfo(Position position);

    List<PositionInfo> toPositionInfos(List<Position> positions);

    PositionSummaryResponse toSummaryResponse(Position position);

    PositionDetailResponse toDetailResponse(Position position);
}

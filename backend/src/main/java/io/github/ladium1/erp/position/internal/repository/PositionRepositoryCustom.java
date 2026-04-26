package io.github.ladium1.erp.position.internal.repository;

import io.github.ladium1.erp.position.internal.dto.PositionSearchCondition;
import io.github.ladium1.erp.position.internal.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositionRepositoryCustom {

    Page<Position> search(PositionSearchCondition condition, Pageable pageable);
}

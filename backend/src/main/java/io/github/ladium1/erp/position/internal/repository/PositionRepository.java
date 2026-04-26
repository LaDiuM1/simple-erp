package io.github.ladium1.erp.position.internal.repository;

import io.github.ladium1.erp.position.internal.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long>, PositionRepositoryCustom {

    boolean existsByCode(String code);

    @Override
    List<Position> findAll();

    Optional<Position> findTopByOrderByRankLevelDesc();
}

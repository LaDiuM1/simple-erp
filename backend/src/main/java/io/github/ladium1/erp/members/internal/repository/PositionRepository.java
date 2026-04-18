package io.github.ladium1.erp.members.internal.repository;

import io.github.ladium1.erp.members.internal.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}
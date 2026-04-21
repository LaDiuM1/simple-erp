package io.github.ladium1.erp.menu.internal.repository;

import io.github.ladium1.erp.menu.internal.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
        List<Menu> findByIdIn(List<Long> ids);
}
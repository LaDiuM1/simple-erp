package io.github.ladium1.erp.menus.internal.repository;

import io.github.ladium1.erp.menus.internal.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("SELECT m FROM Menu m WHERE m.parent IS NULL ORDER BY m.sortOrder ASC")
    List<Menu> findAllRootMenus();
}
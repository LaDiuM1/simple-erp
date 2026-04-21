package io.github.ladium1.erp.menu.internal.service;

import io.github.ladium1.erp.menu.api.MenuApi;
import io.github.ladium1.erp.menu.api.dto.MenuInfo;
import io.github.ladium1.erp.menu.internal.mapper.MenuMapper;
import io.github.ladium1.erp.menu.internal.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService implements MenuApi {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    @Override
    public List<MenuInfo> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(menuMapper::toMenuInfo)
                .toList();
    }
}

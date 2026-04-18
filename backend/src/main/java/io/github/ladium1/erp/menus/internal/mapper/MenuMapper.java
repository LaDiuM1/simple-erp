package io.github.ladium1.erp.menus.internal.mapper;

import io.github.ladium1.erp.menus.MenuInfo;
import io.github.ladium1.erp.menus.internal.entity.Menu;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    MenuInfo toMenuInfo(Menu menu);

}
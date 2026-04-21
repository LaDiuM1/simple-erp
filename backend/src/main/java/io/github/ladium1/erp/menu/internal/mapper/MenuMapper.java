package io.github.ladium1.erp.menu.internal.mapper;

import io.github.ladium1.erp.menu.api.dto.MenuInfo;
import io.github.ladium1.erp.menu.internal.entity.Menu;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    MenuInfo toMenuInfo(Menu menu);

}
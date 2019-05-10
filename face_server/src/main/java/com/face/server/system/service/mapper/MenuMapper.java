package com.face.server.system.service.mapper;

import com.face.server.common.mapper.EntityMapper;
import com.face.server.common.mapper.EntityMapper;
import com.face.server.system.domain.Menu;
import com.face.server.system.service.dto.MenuDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jie
 * @date 2018-12-17
 */
@Mapper(componentModel = "spring",uses = {RoleMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuMapper extends EntityMapper<MenuDTO, Menu> {

}

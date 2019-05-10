package com.face.server.system.service.mapper;

import com.face.server.common.mapper.EntityMapper;
import com.face.server.common.mapper.EntityMapper;
import com.face.server.system.domain.Role;
import com.face.server.system.service.dto.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jie
 * @date 2018-11-23
 */
@Mapper(componentModel = "spring", uses = {PermissionMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper extends EntityMapper<RoleDTO, Role> {

}

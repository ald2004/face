package com.face.server.system.service.mapper;

import com.face.server.common.mapper.EntityMapper;
import com.face.server.system.domain.FaceLog;
import com.face.server.system.domain.FaceUser;
import com.face.server.system.service.dto.FaceLogDTO;
import com.face.server.system.service.dto.FaceUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jie
 * @date 2018-11-23
 */
@Mapper(componentModel = "spring",uses = {RoleMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FaceLogMapper extends EntityMapper<FaceLogDTO, FaceLog> {

}

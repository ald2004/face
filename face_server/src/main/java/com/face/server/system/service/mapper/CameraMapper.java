package com.face.server.system.service.mapper;

import com.face.server.common.mapper.EntityMapper;
import com.face.server.common.mapper.EntityMapper;
import com.face.server.system.domain.Camera;
import com.face.server.system.domain.UserData;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.dto.UserDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jie
 * @date 2018-11-23
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CameraMapper extends EntityMapper<CameraDTO, Camera> {

}

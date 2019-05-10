package com.face.server.system.service.mapper;

import com.face.server.common.mapper.EntityMapper;
import com.face.server.common.mapper.EntityMapper;
import com.face.server.system.domain.User;
import com.face.server.system.domain.UserData;
import com.face.server.system.service.dto.UserDTO;
import com.face.server.system.service.dto.UserDataDTO;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jie
 * @date 2018-11-23
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserDataMapper extends EntityMapper<UserDataDTO, UserData> {

}

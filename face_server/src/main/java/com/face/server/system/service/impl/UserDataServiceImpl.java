package com.face.server.system.service.impl;

import com.face.server.common.exception.EntityExistException;
import com.face.server.common.exception.EntityNotFoundException;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.UserData;
import com.face.server.system.service.UserDataService;
import com.face.server.system.service.dto.UserDataDTO;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.exception.EntityExistException;
import com.face.server.common.exception.EntityNotFoundException;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.core.security.JwtUser;
import com.face.server.core.utils.EncryptUtils;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.Role;
import com.face.server.system.domain.User;
import com.face.server.system.domain.UserData;
import com.face.server.system.repository.UserDataRepository;
import com.face.server.system.repository.UserRepository;
import com.face.server.system.service.UserDataService;
import com.face.server.system.service.UserService;
import com.face.server.system.service.dto.UserDTO;
import com.face.server.system.service.dto.UserDataDTO;
import com.face.server.system.service.mapper.UserDataMapper;
import com.face.server.system.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * @author jie
 * @date 2018-11-23
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserDataServiceImpl implements UserDataService {

    @Autowired
    private UserDataRepository userRepository;

    @Autowired
    private UserDataMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public UserDataDTO findById(long id) {
        Optional<UserData> user = userRepository.findById(id);
        ValidationUtil.isNull(user,"User","id",id);
        return userMapper.toDto(user.get());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDataDTO create(UserData resources) {

        if(userRepository.findByUsername(resources.getUsername())!=null){
            throw new EntityExistException(UserData.class,"username",resources.getUsername());
        }

        return userMapper.toDto(userRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserData resources) {

        Optional<UserData> userOptional = userRepository.findById(resources.getId());
        ValidationUtil.isNull(userOptional,"User","id",resources.getId());

        UserData user = userOptional.get();

         // 根据实际需求修改
        /*if(user.getId().equals(1L)){
            throw new BadRequestException("该账号不能被修改");
        }*/

        UserData user1 = userRepository.findByUsername(resources.getUsername());

        if(user1 != null && !user1.getId().equals(user.getId())){
            throw new EntityExistException(UserData.class,"username",resources.getUsername());
        }

        user.setUsername(resources.getUsername());
        user.setAddress(resources.getAddress());
        user.setIdCard(resources.getIdCard());
        user.setType(resources.getType());
        user.setOldTime(resources.getOldTime());

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {

        /**
         * 根据实际需求修改
         */
       /* if(id.equals(1L)){
            throw new BadRequestException("该账号不能被删除");
        }*/
        userRepository.deleteById(id);
    }

    @Override
    public UserData findByName(String userName) {
        UserData user = null;

        user = userRepository.findByUsername(userName);

        if (user == null) {
            throw new EntityNotFoundException(UserData.class, "name", userName);
        } else {
            return user;
        }
    }


}

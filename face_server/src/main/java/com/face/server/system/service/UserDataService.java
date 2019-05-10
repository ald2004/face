package com.face.server.system.service;

import com.face.server.system.domain.UserData;
import com.face.server.core.security.JwtUser;
import com.face.server.system.domain.User;
import com.face.server.system.domain.UserData;
import com.face.server.system.service.dto.UserDTO;
import com.face.server.system.service.dto.UserDataDTO;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author jie
 * @date 2018-11-23
 */
@CacheConfig(cacheNames = "userMes")
public interface UserDataService {

    /**
     * get
     * @param id
     * @return
     */
    @Cacheable(key = "#p0")
    UserDataDTO findById(long id);

    /**
     * create
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    UserDataDTO create(UserData resources);

    /**
     * update
     * @param resources
     */
    @CacheEvict(allEntries = true)
    void update(UserData resources);

    /**
     * delete
     * @param id
     */
    @CacheEvict(allEntries = true)
    void delete(Long id);

    /**
     * findByNumber
     * @param userName
     * @return
     */
    @Cacheable(key = "'findByName'+#p0")
    UserData findByName(String userName);

}

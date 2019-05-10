package com.face.server.system.service;

import com.face.server.system.domain.Camera;
import com.face.server.system.domain.Camera;
import com.face.server.system.domain.UserData;
import com.face.server.system.service.dto.CameraCountDTO;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.dto.UserDataDTO;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author 摄像头
 * @date 2018-11-23
 */
@CacheConfig(cacheNames = "camera")
public interface CameraService {

    /**
     * get
     *
     * @param id
     * @return
     */
    @Cacheable(key = "#p0")
    CameraDTO findById(long id);

    /**
     * create
     *
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    CameraDTO create(Camera resources);

    /**
     * update
     *
     * @param resources
     */
    @CacheEvict(allEntries = true)
    void update(Camera resources);

    /**
     * delete
     *
     * @param id
     */
    @CacheEvict(allEntries = true)
    void delete(Long id);

    /**
     * findByNumber
     *
     * @param number
     * @return
     */
    @Cacheable(key = "'findByNumber'+#p0")
    Camera findByNumber(String number);

    @Cacheable(key = "'count'")
    CameraCountDTO count();
}

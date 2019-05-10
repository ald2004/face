package com.face.server.system.service;

import com.face.server.system.domain.FaceUser;
import com.face.server.system.service.dto.FaceUserCountDTO;
import com.face.server.system.service.dto.FaceUserDTO;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

@CacheConfig(cacheNames = "face-user")
public interface FaceUserService {

    /**
     * get
     *
     * @param id
     * @return
     */
    @Cacheable(key = "#p0")
    FaceUserDTO findById(long id);

    /**
     * create
     *
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    FaceUserDTO create(FaceUser resources);

    /**
     * update
     *
     * @param resources
     */
    @CacheEvict(allEntries = true)
    void update(FaceUser resources);

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
     * @param name
     * @return
     */
    @Cacheable(key = "'findByName'+#p0")
    FaceUserDTO findByName(String name);

    @CacheEvict(allEntries = true)
    List<FaceUser> createAll(ArrayList<FaceUser> faceUsers);

    @Cacheable(key = "'count'")
    FaceUserCountDTO count();

    @CacheEvict(allEntries = true)
    void delete(List<Long> idArrays);
}

package com.face.server.system.service;

import com.face.server.system.domain.FaceLog;
import com.face.server.system.service.dto.FaceLogCountDTO;
import com.face.server.system.service.dto.FaceLogCountTopNDTO;
import com.face.server.system.service.dto.FaceLogDTO;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@CacheConfig(cacheNames = "face-log")
public interface FaceLogService {

    /**
     * get
     *
     * @param id
     * @return
     */
    @Cacheable(key = "#p0")
    FaceLogDTO findById(long id);

    /**
     * create
     *
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    FaceLogDTO create(FaceLog resources);

    /**
     * create
     *
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    List<FaceLogDTO> createAll(List<FaceLog> resources);

    /**
     * delete
     *
     * @param id
     */
    @CacheEvict(allEntries = true)
    void delete(Long id);

    @Cacheable(key = "'count'")
    FaceLogCountDTO count();

    @Cacheable(key = "'countTop'+#p0")
    List<FaceLogCountTopNDTO> countTopN(int n);

    /**
     * delete
     *
     * @param idArrays
     */
    @CacheEvict(allEntries = true)
    void delete(List<Long> idArrays);

    Long countNew(Long cameraId);
}

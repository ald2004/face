package com.face.server.system.service.impl;

import com.face.server.common.exception.EntityExistException;
import com.face.server.common.exception.EntityNotFoundException;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.Camera;
import com.face.server.system.repository.CameraRepository;
import com.face.server.system.service.CameraService;
import com.face.server.system.service.dto.CameraCountDTO;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.mapper.CameraMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Optional;

/**
 * @author jie
 * @date 2018-11-23
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CameraServiceImpl implements CameraService {

    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
    private CameraMapper cMapper;

    @Override
    public CameraDTO findById(long id) {
        Optional<Camera> camera = cameraRepository.findById(id);
        ValidationUtil.isNull(camera, "Camera", "id", id);
        return cMapper.toDto(camera.get());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CameraDTO create(Camera resources) {

        if (cameraRepository.findByNumber(resources.getNumber()) != null) {
            //注意此处对应的字段
            throw new EntityExistException(Camera.class, "number", resources.getNumber());
        }

        return cMapper.toDto(cameraRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Camera resources) {

        Optional<Camera> cameraOptional = cameraRepository.findById(resources.getId());
        ValidationUtil.isNull(cameraOptional, "Camera", "id", resources.getId());

        Camera camera = cameraOptional.get();

        camera.setNumber(resources.getNumber());
        camera.setIp(resources.getIp());
        camera.setRegion(resources.getRegion());
        camera.setUsername(resources.getUsername());
        camera.setPassword(resources.getPassword());
        camera.setPort(resources.getPort());
        camera.setStatus(resources.getStatus());

        cameraRepository.save(camera);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        cameraRepository.deleteById(id);
    }

    @Override
    public Camera findByNumber(String number) {
        Camera camera = null;

        camera = cameraRepository.findByNumber(number);

        if (camera == null) {
            throw new EntityNotFoundException(Camera.class, "number", number);
        } else {
            return camera;
        }
    }

    @Override
    public CameraCountDTO count() {
        CameraCountDTO cameraCountDTO = new CameraCountDTO();
        cameraCountDTO.setCount(cameraRepository.count());
        long prohibitCount = cameraRepository.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 0)));
        cameraCountDTO.setProhibitCount(prohibitCount);
        return cameraCountDTO;
    }


}

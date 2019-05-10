package com.face.server.system.service.impl;

import com.face.server.common.exception.BadRequestException;
import com.face.server.common.exception.EntityExistException;
import com.face.server.common.exception.EntityNotFoundException;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.system.domain.FaceUser;
import com.face.server.system.repository.FaceUserRepository;
import com.face.server.system.service.FaceUserService;
import com.face.server.system.service.dto.FaceUserCountDTO;
import com.face.server.system.service.dto.FaceUserDTO;
import com.face.server.system.service.mapper.FaceUserMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FaceUserServiceImpl implements FaceUserService {


    @Autowired
    private FaceUserMapper faceUserMapper;
    @Autowired
    private FaceUserRepository faceUserRepo;
    @Value("${face.base-path}")
    private String basePath;

    @Override
    public FaceUserDTO findById(long id) {
        Optional<FaceUser> faceUser = faceUserRepo.findById(id);
        ValidationUtil.isNull(faceUser, "FaceUser", "id", id);
        return faceUserMapper.toDto(faceUser.get());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FaceUserDTO create(FaceUser resources) {

        if (resources.getFacePhoto() == null || resources.getFacePhoto().length() == 0) {
            throw new BadRequestException("图像信息错误！");
        }

        if (faceUserRepo.findByName(resources.getName()) != null) {
            throw new EntityExistException(FaceUser.class, "name", resources.getName());
        }

        if (faceUserRepo.findByIdCard(resources.getIdCard()) != null) {
            throw new EntityExistException(FaceUser.class, "idCard", resources.getIdCard());
        }
        return faceUserMapper.toDto(faceUserRepo.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FaceUser> createAll(ArrayList<FaceUser> faceUsers) {
        return faceUserRepo.saveAll(faceUsers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FaceUser resources) {

        Optional<FaceUser> userOptional = faceUserRepo.findById(resources.getId());
        ValidationUtil.isNull(userOptional, "FaceUser", "id", resources.getId());

        FaceUser faceUser = userOptional.get();

        if (faceUser.getFacePhoto() == null || faceUser.getFacePhoto().length() == 0) {
            throw new BadRequestException("图像信息错误！");
        }

        FaceUser user1 = faceUserRepo.findByName(faceUser.getName());
        FaceUser user2 = faceUserRepo.findByIdCard(faceUser.getIdCard());

        if (user1 != null && !faceUser.getId().equals(user1.getId())) {
            throw new EntityExistException(FaceUser.class, "name", resources.getName());
        }

        if (user2 != null && !faceUser.getId().equals(user2.getId())) {
            throw new EntityExistException(FaceUser.class, "idCard", resources.getIdCard());
        }

        faceUser.setName(resources.getName());
        faceUser.setIdCard(resources.getIdCard());
        faceUser.setStatus(resources.getStatus());
        faceUser.setPhoto(resources.getPhoto());
        faceUser.setFacePhoto(resources.getFacePhoto());
        faceUser.setPhone(resources.getPhone());
        faceUser.setDes(resources.getDes());
        faceUser.setEmbedding(resources.getEmbedding());

        faceUserRepo.save(faceUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        faceUserRepo.deleteById(id);
    }

    @Override
    public FaceUserDTO findByName(String name) {
        FaceUser byIdCard = faceUserRepo.findByIdCard(name);
        if (byIdCard != null) return faceUserMapper.toDto(byIdCard);
        FaceUser byName = faceUserRepo.findByName(name);
        if (byName != null) return faceUserMapper.toDto(byName);
        throw new EntityNotFoundException(FaceUser.class, "name", name);
    }

    /**
     * 状态，0 禁用 1 黑名单 2 白名单
     *
     * @return
     */
    @Override
    public FaceUserCountDTO count() {
        FaceUserCountDTO faceUserCountDTO = new FaceUserCountDTO();

        faceUserCountDTO.setProhibitCount(faceUserRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 0))));
        faceUserCountDTO.setBlackCount(faceUserRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 1))));
        faceUserCountDTO.setWhiteCount(faceUserRepo.count((Specification) (root, query, cb) -> cb.and(cb.equal(root.get("status").as(Long.class), 2))));
        faceUserCountDTO.setCount(faceUserRepo.count());

        return faceUserCountDTO;
    }

    @Override
    public void delete(List<Long> idArrays) {
        List<FaceUser> faceUsers = faceUserRepo.findAllById(idArrays);

        for (FaceUser faceUser : faceUsers) {
            try {
                FileUtils.forceDelete(new File(basePath + File.separator + faceUser.getPhoto().replace("api/images/", "imgs/")));
                FileUtils.forceDelete(new File(basePath + File.separator + faceUser.getFacePhoto().replace("api/images/", "imgs/")));
            } catch (Exception e) {
            }
        }

        faceUserRepo.deleteAll(faceUsers);
    }
}

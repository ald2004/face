package com.face.server.system.service.query;

import com.face.server.common.utils.PageUtil;
import com.face.server.system.domain.FaceUser;
import com.face.server.system.repository.FaceUserRepository;
import com.face.server.system.service.FaceUserService;
import com.face.server.system.service.dto.FaceUserDTO;
import com.face.server.system.service.mapper.FaceUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = "face-user")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FaceUserQueryService {

    @Autowired
    private FaceUserMapper faceUserMapper;
    @Autowired
    private FaceUserRepository faceUserRepo;
    @Autowired
    private FaceUserService faceUserService;


    /**
     * 分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(FaceUserDTO user, Pageable pageable) {
        Page<FaceUser> page = faceUserRepo.findAll(new FaceUserQueryService.Spec(user), pageable);
        return PageUtil.toPage(page.map(faceUserMapper::toDto));
    }

    /**
     * 不分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(FaceUserDTO user) {
        return faceUserMapper.toDto(faceUserRepo.findAll(new FaceUserQueryService.Spec(user)));
    }

    public void delete(Long id) {
        faceUserService.delete(id);
    }

    public void delete(List<Long> idArrays) {
        faceUserService.delete(idArrays);
    }

    class Spec implements Specification<FaceUser> {

        private FaceUserDTO faceUserDTO;

        public Spec(FaceUserDTO user) {
            this.faceUserDTO = user;
        }

        @Override
        public Predicate toPredicate(Root<FaceUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

            List<Predicate> list = new ArrayList<Predicate>();

            if (!ObjectUtils.isEmpty(faceUserDTO.getId())) {
                /*
                  相等
                 */
                list.add(cb.equal(root.get("id").as(Long.class), faceUserDTO.getId()));
            }

            if (!ObjectUtils.isEmpty(faceUserDTO.getStatus())) {
                /*
                 * 相等
                 */
                list.add(cb.equal(root.get("status").as(Integer.class), faceUserDTO.getStatus()));
            }


            if (!ObjectUtils.isEmpty(faceUserDTO.getIdCard())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("idCard").as(String.class), "%" + faceUserDTO.getIdCard() + "%"));
            }


            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }
    }
}

package com.face.server.system.service.query;

import com.face.server.common.utils.PageUtil;
import com.face.server.system.domain.Camera;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.repository.CameraRepository;
import com.face.server.system.service.mapper.CameraMapper;
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

/**
 * @author cxj   查询用户数据
 * @date 2018-11-22
 */
@Service
@CacheConfig(cacheNames = "camera")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CameraQueryService {

    @Autowired(required = true)
    private CameraRepository cameraRepo;

    @Autowired(required = true)
    private CameraMapper mapper;

    /**
     * 分页
     *
     * @Cacheable在方法上配置是否启用缓存
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(CameraDTO cameraDTO, Pageable pageable) {
        Page<Camera> page = cameraRepo.findAll(new Spec(cameraDTO), pageable);
        return PageUtil.toPage(page.map(mapper::toDto));
    }

    /**
     * 不分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(CameraDTO camera) {
        return mapper.toDto(cameraRepo.findAll(new Spec(camera)));
    }

    class Spec implements Specification<Camera> {

        private CameraDTO cameraDTO;

        public Spec(CameraDTO camera) {
            this.cameraDTO = camera;
        }

        @Override
        public Predicate toPredicate(Root<Camera> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

            List<Predicate> list = new ArrayList<Predicate>();

            if (!ObjectUtils.isEmpty(cameraDTO.getId())) {
                /*
                 * 相等
                 */
                list.add(cb.equal(root.get("id").as(Long.class), cameraDTO.getId()));
            }

            if (!ObjectUtils.isEmpty(cameraDTO.getStatus())) {
                /*
                 * 相等
                 */
                list.add(cb.equal(root.get("status").as(Integer.class), cameraDTO.getStatus()));
            }


            if (!ObjectUtils.isEmpty(cameraDTO.getIp())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("ip").as(String.class), "%" + cameraDTO.getIp() + "%"));
            }

            if (!ObjectUtils.isEmpty(cameraDTO.getRegion())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("region").as(String.class), "%" + cameraDTO.getRegion() + "%"));
            }


            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }
    }
}

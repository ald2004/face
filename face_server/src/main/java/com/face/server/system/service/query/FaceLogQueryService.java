package com.face.server.system.service.query;

import com.face.server.common.utils.PageUtil;
import com.face.server.system.domain.FaceLog;
import com.face.server.system.repository.FaceLogRepository;
import com.face.server.system.service.FaceLogService;
import com.face.server.system.service.dto.FaceLogDTO;
import com.face.server.system.service.mapper.FaceLogMapper;
import org.hibernate.criterion.Restrictions;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = "face-log")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FaceLogQueryService {


    @Autowired
    private FaceLogMapper faceLogMapper;
    @Autowired
    private FaceLogRepository faceLogRepo;
    @Autowired
    private FaceLogService faceLogService;

    /**
     * 分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(FaceLogDTO user, Pageable pageable) {
        Page<FaceLog> page = faceLogRepo.findAll(new FaceLogQueryService.Spec(user), pageable);
        return PageUtil.toPage(page.map(faceLogMapper::toDto));
    }

    /**
     * 不分页
     */
    @Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(FaceLogDTO user) {
        return faceLogMapper.toDto(faceLogRepo.findAll(new FaceLogQueryService.Spec(user)));
    }

    public void delete(Long id) {
        faceLogService.delete(id);
    }

    public void delete(List<Long> idArrays) {
        faceLogService.delete(idArrays);
    }

    class Spec implements Specification<FaceLog> {

        private FaceLogDTO faceLogDTO;

        public Spec(FaceLogDTO user) {
            this.faceLogDTO = user;
        }

        @Override
        public Predicate toPredicate(Root<FaceLog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

            List<Predicate> list = new ArrayList<Predicate>();

            if (!ObjectUtils.isEmpty(faceLogDTO.getId())) {
                /*
                  相等
                 */
                list.add(cb.equal(root.get("id").as(Long.class), faceLogDTO.getId()));
            }

            if (!ObjectUtils.isEmpty(faceLogDTO.getCameraId())) {
                /*
                  相等
                 */
                list.add(cb.equal(root.get("cameraId").as(Long.class), faceLogDTO.getCameraId()));
            }

            if (!ObjectUtils.isEmpty(faceLogDTO.getFaceUserId())) {
                /*
                  相等
                 */
                list.add(cb.equal(root.get("faceUserId").as(Long.class), faceLogDTO.getFaceUserId()));
            }

            if (!ObjectUtils.isEmpty(faceLogDTO.getStatus())) {
                /*
                 * 相等
                 */
                list.add(cb.equal(root.get("status").as(Integer.class), faceLogDTO.getStatus()));
            }
            SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
            if (!ObjectUtils.isEmpty(faceLogDTO.getStartTime())) {
                /*
                 * 大于等于
                 */
                list.add(cb.ge(root.get("createTime").as(Long.class), Long.parseLong(yyyyMMddHHmmss.format(faceLogDTO.getStartTime()))));
            }
            if (!ObjectUtils.isEmpty(faceLogDTO.getEndTime())) {
                /*
                 * 小于等于
                 */
                list.add(cb.le(root.get("createTime").as(Long.class), Long.parseLong(yyyyMMddHHmmss.format(faceLogDTO.getEndTime()))));
            }


            if (!ObjectUtils.isEmpty(faceLogDTO.getPhone())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("phone").as(String.class), "%" + faceLogDTO.getPhone() + "%"));
            }
            if (!ObjectUtils.isEmpty(faceLogDTO.getIdCard())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("idCard").as(String.class), "%" + faceLogDTO.getIdCard() + "%"));
            }
            if (!ObjectUtils.isEmpty(faceLogDTO.getName())) {
                /*
                 * 模糊
                 */
                list.add(cb.like(root.get("name").as(String.class), "%" + faceLogDTO.getName() + "%"));
            }


            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }
    }

}

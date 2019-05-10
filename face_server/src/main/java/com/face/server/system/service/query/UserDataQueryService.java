package com.face.server.system.service.query;

import com.face.server.common.utils.PageUtil;
import com.face.server.system.domain.UserData;
import com.face.server.common.utils.PageUtil;
import com.face.server.system.domain.UserData;
import com.face.server.system.repository.UserDataRepository;
import com.face.server.system.service.dto.UserDataDTO;
import com.face.server.system.service.mapper.UserDataMapper;
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
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cxj   查询用户数据
 * @date 2018-11-22
 */
@Service
@CacheConfig(cacheNames = "userMes")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserDataQueryService {

    @Autowired(required=true)
    private UserDataRepository userRepo;

    @Autowired(required=true)
    private UserDataMapper mapper;

    /**
     * 分页
     * @Cacheable在方法上配置是否启用缓存
     */
    //@Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(UserDataDTO user, Pageable pageable){
        Page<UserData> page = userRepo.findAll(new Spec(user),pageable);
        return PageUtil.toPage(page.map(mapper::toDto));
    }

    /**
     * 不分页
     */
    //@Cacheable(keyGenerator = "keyGenerator")
    public Object queryAll(UserDataDTO user){
        return mapper.toDto(userRepo.findAll(new Spec(user)));
    }

    class Spec implements Specification<UserData> {

        private UserDataDTO user;

        public Spec(UserDataDTO user){
            this.user = user;
        }

        @Override
        public Predicate toPredicate(Root<UserData> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

            List<Predicate> list = new ArrayList<Predicate>();

            if(!ObjectUtils.isEmpty(user.getId())){
                /**
                 * 相等
                 */
                list.add(cb.equal(root.get("id").as(Long.class),user.getId()));
            }

            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }
    }
}

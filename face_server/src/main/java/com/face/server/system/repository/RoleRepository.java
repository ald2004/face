package com.face.server.system.repository;

import com.face.server.system.domain.Role;
import com.face.server.system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author jie
 * @date 2018-12-03
 */
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor {

    /**
     * findByNumber
     * @param name
     * @return
     */
    Role findByName(String name);
}

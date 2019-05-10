package com.face.server.system.repository;

import com.face.server.system.domain.Menu;
import com.face.server.system.domain.Role;
import com.face.server.system.domain.Menu;
import com.face.server.system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Set;

/**
 * @author jie
 * @date 2018-12-17
 */
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor {

    /**
     * findByNumber
     * @param name
     * @return
     */
    Menu findByName(String name);

    /**
     * findByRoles
     * @param roleSet
     * @return
     */
    Set<Menu> findByRolesOrderBySort(Set<Role> roleSet);

    /**
     * findByPid
     * @param pid
     * @return
     */
    List<Menu> findByPid(long pid);
}

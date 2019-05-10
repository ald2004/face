package com.face.server.system.repository;

import com.face.server.system.domain.UserData;
import com.face.server.system.domain.Role;
import com.face.server.system.domain.User;
import com.face.server.system.domain.UserData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author cxj
 * @date 2018-11-22
 */
public interface UserDataRepository extends JpaRepository<UserData, Long>, JpaSpecificationExecutor {

    /**
     * findByUsername
     * @param username
     * @return
     */
    /*@Query("from userMes   where username = :username")
    UserData findByUsername(@Param("username") String username);*/

    UserData findByUsername(String username);


}

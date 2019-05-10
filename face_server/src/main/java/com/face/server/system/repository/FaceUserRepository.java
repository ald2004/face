package com.face.server.system.repository;

import com.face.server.system.domain.FaceUser;
import com.face.server.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FaceUserRepository extends JpaRepository<FaceUser, Long>, JpaSpecificationExecutor {
    /**
     * findByNumber
     *
     * @param name
     * @return
     */
    @Query("from FaceUser u where u.name = :name")
    FaceUser findByName(@Param("name") String name);

    /**
     * findByIdCard
     * @param idCard
     * @return
     */
    @Query("from FaceUser u where u.idCard = :idCard")
    FaceUser findByIdCard(@Param("idCard") String idCard);
}

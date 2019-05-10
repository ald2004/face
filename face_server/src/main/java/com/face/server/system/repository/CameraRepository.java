package com.face.server.system.repository;

import com.face.server.system.domain.Camera;
import com.face.server.system.domain.Camera;
import com.face.server.system.domain.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author cxj
 * @date 2018-11-22
 */
public interface CameraRepository extends JpaRepository<Camera, Long>, JpaSpecificationExecutor {

    /**
     * findByNumber
     *
     * @param number
     * @return
     */
    @Query("from Camera c where c.number = :number")
    Camera findByNumber(String number);


}

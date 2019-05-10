package com.face.server.system.repository;

import com.face.server.system.domain.FaceLog;
import com.face.server.system.service.dto.FaceLogCountDTO;
import com.face.server.system.service.dto.FaceLogCountTopNDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface FaceLogRepository extends JpaRepository<FaceLog, Long>, JpaSpecificationExecutor {
    /**
     * @return
     */
//    cameraId
//count
//faceUserStatus
//ip
//region
    @Query("SELECT COUNT(cameraId) AS count,cameraId AS cameraId,ip AS ip,region AS region,faceUserStatus AS faceUserStatus FROM FaceLog  GROUP BY cameraId,ip,region,faceUserStatus ORDER BY COUNT(cameraId) DESC")
    List<Map<String, Object>> countTopN(int n);

    @Query("SELECT COUNT(id) FROM FaceLog WHERE createTime > :date ")
    Long countNew(@Param("date") Date date);

    @Query("SELECT COUNT(id) FROM FaceLog WHERE cameraId = :cameraId AND createTime > :date ")
    Long countNew(@Param("cameraId") Long cameraId, @Param("date") Date date);
}

package com.face.server.system.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class FaceLogDTO implements Serializable {
    private Long id;
    private Timestamp createTime;

    //    查询条件开始时间
    private Timestamp startTime;
    //    查询条件结束时间
    private Timestamp endTime;


    private Integer status;
    private String logImg;
    // ================人脸库字段==================
    private Long faceUserId;
    private String name;
    private Integer faceUserStatus;
    private String idCard;
    private String phone;
    private String facePhoto;
    private String photo;
    // ================人脸库字段==================

    // ================摄像头字段==================
    private Long cameraId;
    private String number;
    private String username;
    private String password;
    private String region;
    private String ip;
    private Integer port;
    private Integer cameraStatus;
    // ================摄像头字段==================

    // ================人脸框字段==================
    public int x;
    public int y;
    public int width;
    public int height;
    public String points;
    public float angle;
    // ================人脸框字段==================

}

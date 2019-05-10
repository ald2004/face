package com.face.server.system.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 */
@Data
public class FaceUserDTO implements Serializable {
    private Long id;
    private String photo;
    private String facePhoto;
    private Timestamp createTime;
    private String idCard;
    private Integer status;
    private String name;
    private String phone;
    private String des;
    private String embedding;
}

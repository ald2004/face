package com.face.server.system.service.dto;

import lombok.Data;

import javax.persistence.Entity;

/**
 * project : face-web-server
 * Code Create : 2019/5/7
 * Class : com.face.server.system.service.dto.CameraCountDTO
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
@Data
public class FaceLogCountTopNDTO {
    private long count;
    private long cameraId;
    private String ip;
    private String region;
    /**
     * 0 禁用 1 黑名单 2 白名单 3 陌生人
     */
    private int faceUserStatus;
}

package com.face.server.system.domain;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "face_log")
public class FaceLog implements Serializable {

    @Column(columnDefinition = "bigint COMMENT 'id'")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "timestamp COMMENT '创建时间'")
    @CreationTimestamp
    private Timestamp createTime;

    @Column(columnDefinition = "int(11) COMMENT '识别记录状态，0 禁用 1 黑名单 2 白名单 3 陌生人'")
    @NotNull
    private int status;

    @Column(columnDefinition = "varchar(255) COMMENT '识别记录图片路径'")
    @NotNull
    private String logImg;


    // ================人脸库字段==================
    @Column(columnDefinition = "bigint COMMENT '人脸库ID，陌生人为 -1'")
    @NotNull
    private Long faceUserId;

    @Column(columnDefinition = "varchar(30) COMMENT '姓名'")
    @NotBlank
    private String name;

    @Column(columnDefinition = "int(11) COMMENT '人脸库状态，0 禁用 1 黑名单 2 白名单'")
    @NotNull
    private int faceUserStatus;

    @Column(columnDefinition = "varchar(30) COMMENT '身份证'")
    @NotBlank
    private String idCard;

    @Column(columnDefinition = "varchar(20) COMMENT '手机号'")
    @NotBlank
    private String phone;
    @Column(columnDefinition = "varchar(255) COMMENT '人脸图像地址'")
    @NotBlank
    private String facePhoto;
    @Column(columnDefinition = "varchar(255) COMMENT '原始图像地址'")
    @NotBlank
    private String photo;

    // ================人脸库字段==================

    // ================摄像头字段==================

    @Column(columnDefinition = "bigint COMMENT '摄像头ID'")
    @NotNull
    private Long cameraId;

    @Column(columnDefinition = "varchar(255) COMMENT '编号'")
    @NotBlank
    private String number;

    @Column(columnDefinition = "varchar(255) COMMENT '摄像头用户名'")
    @NotNull
    @NotBlank
    private String username;

    @Column(columnDefinition = "varchar(255) COMMENT '摄像头密码'")
    @NotNull
    @NotBlank
    private String password;

    @Column(columnDefinition = "varchar(255) COMMENT '安装位置'")
    private String region;

    @Column(columnDefinition = "varchar(64) COMMENT 'IP'")
    @NotNull
    @NotBlank
    private String ip;

    @Column(columnDefinition = "int(11) COMMENT 'RTSP端口'")
    @NotNull
    private Integer port;

    @Column(columnDefinition = "int(11) COMMENT '摄像头状态，0 禁用 1 启用'")
    @NotNull
    private Integer cameraStatus;
    // ================摄像头字段==================
    // ================人脸框字段==================
    @Column(columnDefinition = "int(11) COMMENT '人脸框X'")
    public int x;
    @Column(columnDefinition = "int(11) COMMENT '人脸框Y'")
    public int y;
    @Column(columnDefinition = "int(11) COMMENT '人脸框宽度'")
    public int width;
    @Column(columnDefinition = "int(11) COMMENT '人脸框高度'")
    public int height;
    @Column(columnDefinition = "varchar(255) COMMENT 'Landmark'")
    public String points;
    @Column(columnDefinition = "float(11) COMMENT '人脸框高度'")
    // 水平角度
    public float angle;
    // ================人脸框字段==================
}

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
@Table(name = "face_user")
public class FaceUser implements Serializable {

    @Column(columnDefinition = "bigint COMMENT 'id'")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(columnDefinition = "varchar(20) COMMENT '手机号'")
    @NotBlank
    private String phone;
    @Column(columnDefinition = "varchar(255) COMMENT '人脸图像地址'")
    @NotBlank
    private String facePhoto;
    @Column(columnDefinition = "varchar(255) COMMENT '原始图像地址'")
    @NotBlank
    private String photo;


    @Column(columnDefinition = "timestamp COMMENT '创建时间'")
    @CreationTimestamp
    private Timestamp createTime;

    // 全局唯一
    @Column(columnDefinition = "varchar(30) COMMENT '身份证'", unique = true)
    @NotNull
    @NotBlank
    private String idCard;

    @Column(columnDefinition = "int(11) COMMENT '状态，0 禁用 1 黑名单 2 白名单'")
    @NotNull
    private int status;

    @Column(columnDefinition = "varchar(30) COMMENT '姓名'")
    @NotBlank
    private String name;

    @Column(columnDefinition = "varchar(255) COMMENT '描述'")
    private String des;

    @NotBlank
    @NotNull
    @Column(columnDefinition = "varchar(2000) COMMENT '人脸向量 float[128] 用于人脸识别'")
    private String embedding;

    @Override
    public String toString() {
        return "FaceUser{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", facePhoto='" + facePhoto + '\'' +
                ", photo='" + photo + '\'' +
                ", createTime=" + createTime +
                ", idCard='" + idCard + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", des='" + des + '\'' +
                ", embedding='" + embedding + '\'' +
                '}';
    }
}

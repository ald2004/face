package com.face.server.system.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author jie
 * @date 2018-11-22
 */
@Entity
@Getter
@Setter
@Table(name = "camera")
public class Camera implements Serializable {

    @Column(columnDefinition = "bigint COMMENT 'id'")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(columnDefinition = "timestamp COMMENT '创建时间'")
    @CreationTimestamp
    private Timestamp createTime;

    @Column(columnDefinition = "int(11) COMMENT '状态，0 禁用 1 启用'")
    @NotNull
    private Integer status;


}
package com.face.server.system.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * @author jie
 * @date 2018-11-23
 */
@Data
public class CameraDTO implements Serializable {
    private Long id;
    private String number;
    private String username;
    private String password;
    private String region;
    private String ip;
    private Integer port;
    private Timestamp createTime;
    private Integer status;
}

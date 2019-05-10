package com.face.server.system.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * @author jie
 * @date 2018-11-23
 */
@Data
public class UserDataDTO implements Serializable {

    private Long id;

    private String username;


    private String sex;
    
    private String idCard;
    private String image;
    private String address;
    private String type;
    private String oldTime;
    private Timestamp createTime;
}


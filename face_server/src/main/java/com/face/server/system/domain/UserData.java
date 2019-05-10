package com.face.server.system.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

/**
 * @author jie
 * @date 2018-11-22
 */
@Entity
@Getter
@Setter
@Table(name="userMes")
public class UserData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    private String sex;
    
    private String idCard;
    private String image;
    private String address;
    private String type;
    private String oldTime;

    @CreationTimestamp
    private Timestamp createTime;



   /* private Date lastPasswordResetTime;

    @ManyToMany
    @JoinTable(name = "users_roles", joinColumns = {@JoinColumn(name = "user_id",referencedColumnName = "id")}, inverseJoinColumns = {@JoinColumn(name = "role_id",referencedColumnName = "id")})
    private Set<Role> roles;*/

   
}
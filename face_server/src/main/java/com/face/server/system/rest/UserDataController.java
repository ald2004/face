package com.face.server.system.rest;

import com.face.server.system.domain.UserData;
import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.ElAdminConstant;
import com.face.server.common.utils.RequestHolder;
import com.face.server.core.security.JwtUser;
import com.face.server.core.utils.EncryptUtils;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.User;
import com.face.server.system.domain.UserData;
import com.face.server.system.domain.VerificationCode;
import com.face.server.system.service.UserDataService;
import com.face.server.system.service.UserService;
import com.face.server.system.service.VerificationCodeService;
import com.face.server.system.service.dto.UserDTO;
import com.face.server.system.service.dto.UserDataDTO;
import com.face.server.system.service.query.UserDataQueryService;
import com.face.server.system.service.query.UserQueryService;
import com.face.server.tools.domain.Picture;
import com.face.server.tools.service.PictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jie
 * @date 2018-11-23
 */
@RestController
@RequestMapping("api")
public class UserDataController {


    @Autowired
    private UserDataService userService;

    @Autowired
    private UserDataQueryService service;

    private static final String ENTITY_NAME = "userData";

    //Logger log=LoggerFactory.getLogger(UserDataController.class);

    @GetMapping(value = "/userData/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    public ResponseEntity getUser(@PathVariable Long id){
        return new ResponseEntity(userService.findById(id), HttpStatus.OK);
    }

    @Log(description = "查询用户数据")
    @GetMapping(value = "/userData")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    public ResponseEntity getUsers(UserDataDTO userDTO, Pageable pageable){
        return new ResponseEntity(service.queryAll(userDTO,pageable),HttpStatus.OK);
    }


    @Log(description = "新增用户数据")
    @PostMapping(value = "/userData")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_CREATE')")
    public ResponseEntity create(@Validated @RequestBody UserData resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        return new ResponseEntity(userService.create(resources),HttpStatus.CREATED);
    }

    @Log(description = "修改用户数据")
    @PutMapping(value = "/userData")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_EDIT')")
    public ResponseEntity update(@Validated @RequestBody UserData resources){
        if (resources.getId() == null) {
            throw new BadRequestException(ENTITY_NAME +" ID Can not be empty");
        }
        userService.update(resources);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Log(description = "删除用户数据")
    @DeleteMapping(value = "/userData/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_DELETE')")
    public ResponseEntity delete(@PathVariable Long id){
        userService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

}

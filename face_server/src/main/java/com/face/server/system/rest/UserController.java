package com.face.server.system.rest;

import com.face.server.system.domain.User;
import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.ElAdminConstant;
import com.face.server.common.utils.RequestHolder;
import com.face.server.core.security.JwtUser;
import com.face.server.core.utils.EncryptUtils;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.User;
import com.face.server.system.domain.VerificationCode;
import com.face.server.system.service.UserService;
import com.face.server.system.service.VerificationCodeService;
import com.face.server.system.service.dto.UserDTO;
import com.face.server.system.service.query.UserQueryService;
import com.face.server.tools.domain.Picture;
import com.face.server.tools.service.PictureService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jie
 * @date 2018-11-23
 */
@RestController
@RequestMapping("api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    @Qualifier("jwtUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private VerificationCodeService verificationCodeService;


    private static final String ENTITY_NAME = "user";

    @GetMapping(value = "/images/{fileName:[\\w.]+}")
//    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    @ResponseBody
    public byte[] getImages(@PathVariable("fileName") String fileName) throws IOException {
        return FileUtils.readFileToByteArray(new File("imgs/" + fileName));
    }

    @GetMapping(value = "/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    public ResponseEntity getUser(@PathVariable Long id) {
        return new ResponseEntity(userService.findById(id), HttpStatus.OK);
    }

    @Log(description = "查询用户")
    @GetMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_SELECT')")
    public ResponseEntity getUsers(UserDTO userDTO, Pageable pageable) {
        return new ResponseEntity(userQueryService.queryAll(userDTO, pageable), HttpStatus.OK);
    }

    @Log(description = "新增用户")
    @PostMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_CREATE')")
    public ResponseEntity create(@Validated @RequestBody User resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity(userService.create(resources), HttpStatus.CREATED);
    }

    @Log(description = "修改用户")
    @PutMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_EDIT')")
    public ResponseEntity update(@Validated @RequestBody User resources) {
        if (resources.getId() == null) {
            throw new BadRequestException(ENTITY_NAME + " ID Can not be empty");
        }
        userService.update(resources);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Log(description = "删除用户")
    @DeleteMapping(value = "/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_ALL','USER_DELETE')")
    public ResponseEntity delete(@PathVariable Long id) {
        userService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 验证密码
     *
     * @param pass
     * @return
     */
    @GetMapping(value = "/users/validPass/{pass}")
    public ResponseEntity validPass(@PathVariable String pass) {
        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest()));
        Map map = new HashMap();
        map.put("status", 200);
        if (!jwtUser.getPassword().equals(EncryptUtils.encryptPassword(pass))) {
            map.put("status", 400);
        }
        return new ResponseEntity(map, HttpStatus.OK);
    }

    /**
     * 修改密码
     *
     * @param pass
     * @return
     */
    @GetMapping(value = "/users/updatePass/{pass}")
    public ResponseEntity updatePass(@PathVariable String pass) {
        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest()));
        if (jwtUser.getPassword().equals(EncryptUtils.encryptPassword(pass))) {
            throw new BadRequestException("新密码不能与旧密码相同");
        }
        userService.updatePass(jwtUser, EncryptUtils.encryptPassword(pass));
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 修改头像
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/users/updateAvatar")
    public ResponseEntity updateAvatar(@RequestParam MultipartFile file) {
        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest()));
        Picture picture = pictureService.upload(file, jwtUser.getUsername());
        userService.updateAvatar(jwtUser, picture.getUrl());
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 修改邮箱
     *
     * @param user
     * @param user
     * @return
     */
    @PostMapping(value = "/users/updateEmail/{code}")
    public ResponseEntity updateEmail(@PathVariable String code, @RequestBody User user) {
        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest()));
        if (!jwtUser.getPassword().equals(EncryptUtils.encryptPassword(user.getPassword()))) {
            throw new BadRequestException("密码错误");
        }
        VerificationCode verificationCode = new VerificationCode(code, ElAdminConstant.RESET_MAIL, "email", user.getEmail());
        verificationCodeService.validated(verificationCode);
        userService.updateEmail(jwtUser, user.getEmail());
        return new ResponseEntity(HttpStatus.OK);
    }
}

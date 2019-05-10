package com.face.server.tools.rest;

import com.face.server.common.aop.log.Log;
import com.face.server.tools.domain.EmailConfig;
import com.face.server.tools.domain.vo.EmailVo;
import com.face.server.tools.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import com.face.server.common.aop.log.Log;
import com.face.server.tools.domain.EmailConfig;
import com.face.server.tools.domain.vo.EmailVo;
import com.face.server.tools.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 发送邮件
 * @author 郑杰
 * @date 2018/09/28 6:55:53
 */
@Slf4j
@RestController
@RequestMapping("api")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping(value = "/email")
    public ResponseEntity get(){
        return new ResponseEntity(emailService.find(),HttpStatus.OK);
    }

    @Log(description = "配置邮件")
    @PutMapping(value = "/email")
    public ResponseEntity emailConfig(@Validated @RequestBody EmailConfig emailConfig){
        emailService.update(emailConfig,emailService.find());
        return new ResponseEntity(HttpStatus.OK);
    }

    @Log(description = "发送邮件")
    @PostMapping(value = "/email")
    public ResponseEntity send(@Validated @RequestBody EmailVo emailVo) throws Exception {
        log.warn("REST request to send Email : {}" +emailVo);
        emailService.send(emailVo,emailService.find());
        return new ResponseEntity(HttpStatus.OK);
    }
}

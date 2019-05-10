package com.face.server.system.rest;


import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.FileUtil;
import com.face.server.system.domain.FaceLog;
import com.face.server.system.service.FaceLogService;
import com.face.server.system.service.dto.FaceLogDTO;
import com.face.server.system.service.query.FaceLogQueryService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/faceLog")
public class FaceLogController {

    @Autowired
    private FaceLogQueryService faceLogQueryService;

    @Autowired
    private FaceLogService faceLogService;

    private static final String ENTITY_NAME = "face-log";


    @Log(description = "统计识别记录")
    @GetMapping(value = "/count")
    public ResponseEntity count() {
        return new ResponseEntity(faceLogService.count(), HttpStatus.OK);
    }

    /**
     * 最近5分钟算新纪录
     *
     * @return
     */
    @Log(description = "统计最近识别记录")
    @GetMapping(value = "/countNew/{cameraId}")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_LOG_ALL','FACE_LOG_SELECT')")
    public ResponseEntity countNew(@PathVariable(value = "cameraId", required = false) Long cameraId) {
        return new ResponseEntity(faceLogService.countNew(cameraId), HttpStatus.OK);
    }

    @Log(description = "统计识别记录TopN")
    @GetMapping(value = "/count/camera/top/{n}")
    public ResponseEntity countTopN(@PathVariable("n") int n) {
        return new ResponseEntity(faceLogService.countTopN(n), HttpStatus.OK);
    }


    @Log(description = "查询人脸识别记录")
    @GetMapping(value = "/logs")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_LOG_ALL','FACE_LOG_SELECT')")
    public ResponseEntity getLogs(FaceLogDTO faceLogDTO, Pageable pageable) {
        return new ResponseEntity(faceLogQueryService.queryAll(faceLogDTO, pageable), HttpStatus.OK);
    }

    @Log(description = "创建人脸识别记录")
    @PutMapping(value = "/logs")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_LOG_ALL','FACE_LOG_CREATE')")
    public ResponseEntity create(@Validated @RequestBody FaceLog resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(faceLogService.create(resources), HttpStatus.CREATED);
    }


    @Log(description = "删除人脸识别记录")
    @DeleteMapping(value = "/logs/{ids}")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_LOG_ALL','FACE_LOG_DELETE')")
    public ResponseEntity delete(@PathVariable String ids) {

        if (!ids.matches("^(\\d+,)*\\d+$")) {
            throw new BadRequestException("ids:必须符合  ^(\\d+,)*\\d+$ 格式。如 1,2,3,4,5 ");
        }

        String[] split = ids.split(",");
        List<Long> idArrays = new ArrayList<>();
        for (String aSplit : split) {
            idArrays.add(Long.parseLong(aSplit));
        }
        faceLogQueryService.delete(idArrays);
        return new ResponseEntity(HttpStatus.OK);
    }
}

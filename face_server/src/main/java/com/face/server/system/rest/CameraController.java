package com.face.server.system.rest;

import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.system.domain.Camera;
import com.face.server.system.service.CameraService;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.query.CameraQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author jie
 * @date 2018-11-23
 */
@RestController
@RequestMapping("api")
public class CameraController {


    @Autowired
    private CameraService cameraService;

    @Autowired
    private CameraQueryService service;

    private static final String ENTITY_NAME = "camera";

    //Logger log=LoggerFactory.getLogger(CameraDataController.class);

    @Log(description = "统计摄像头数量")
    @GetMapping(value = "/camera/count")
    public ResponseEntity count() {
        return new ResponseEntity(cameraService.count(), HttpStatus.OK);
    }

    @Log(description = "查询摄像头数据{id}")
    @GetMapping(value = "/camera/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERA_ALL','CAMERA_SELECT')")
    public ResponseEntity getCamera(@PathVariable Long id) {
        return new ResponseEntity(cameraService.findById(id), HttpStatus.OK);
    }

    @Log(description = "查询摄像头数据")
    @GetMapping(value = "/camera")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERA_ALL','CAMERA_SELECT')")
    public ResponseEntity getCameras(CameraDTO cameraDTO, Pageable pageable) {
        return new ResponseEntity(service.queryAll(cameraDTO, pageable), HttpStatus.OK);
    }


    @Log(description = "新增摄像头数据")
    @PostMapping(value = "/camera")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERA_ALL','CAMERA_CREATE')")
    public ResponseEntity create(@Validated @RequestBody Camera resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity(cameraService.create(resources), HttpStatus.CREATED);
    }

    @Log(description = "修改摄像头数据")
    @PutMapping(value = "/camera")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERA_ALL','CAMERA_EDIT')")
    public ResponseEntity update(@Validated @RequestBody Camera resources) {
        if (resources.getId() == null) {
            throw new BadRequestException(ENTITY_NAME + " ID Can not be empty");
        }
        cameraService.update(resources);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Log(description = "删除摄像头数据")
    @DeleteMapping(value = "/camera/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERA_ALL','CAMERA_DELETE')")
    public ResponseEntity delete(@PathVariable Long id) {
        cameraService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }

}

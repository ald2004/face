package com.face.server.system.rest;

import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;
import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.system.domain.FaceLog;
import com.face.server.system.service.CameraService;
import com.face.server.system.service.FaceLogService;
import com.face.server.system.service.dto.CameraDTO;
import com.face.server.system.service.dto.FaceLogDTO;
import com.face.server.system.service.dto.FaceUserDTO;
import com.face.server.system.service.query.CameraQueryService;
import com.face.server.system.service.query.FaceLogQueryService;
import com.face.server.system.service.query.FaceUserQueryService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("api/no-user")
public class NoUserController {


    @Autowired
    private FaceUserQueryService faceUserQueryService;

    @Autowired
    private CameraService cameraService;

    @Autowired
    private FaceLogService faceLogService;

    @Autowired
    private CameraQueryService cameraQueryService;
    @Autowired
    private FaceLogQueryService faceLogQueryService;

    @Value("${face.job.base-path}")
    private String basePath;

//    日志太多 关闭


    @GetMapping(value = "/testSDK")
    public ResponseEntity testSDK(String imgPath) throws IOException {
        new Thread(() -> {
            for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
                System.out.println(threadEntry.getKey() + ":" + Arrays.toString(threadEntry.getValue()));
            }
            FaceSDK faceSDK = new FaceSDK();

            faceSDK.faceModelInit(4);
            BufferedImage read = null;
            try {
                read = ImageIO.read(new File(imgPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayList<FACE_BOX> faceBoxes = new ArrayList<>();

            long start = System.currentTimeMillis();
            faceSDK.faceDetect(read, faceBoxes);
            long end = System.currentTimeMillis();
            faceBoxes.forEach(System.out::println);
            System.out.println(end - start + "ms");

        }).start();
        return new ResponseEntity("ms", HttpStatus.OK);
    }

    @GetMapping(value = "/faceLog/images/{cameraId}/{fileName:[\\w.]+}")
    @ResponseBody
    public byte[] getImage(@PathVariable("fileName") String fileName, @PathVariable("cameraId") Integer cameraId, int x, int y, int w, int h) throws IOException {
        File file = new File(basePath + File.separator + cameraId + File.separator + fileName + "." + x + "." + y + "." + w + "." + h);
        if (file.exists()) return FileUtils.readFileToByteArray(file);

        File file2 = new File(basePath + File.separator + cameraId + File.separator + fileName);
        BufferedImage bi = ImageIO.read(file2);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setStroke(new BasicStroke(3.0f));
        g2.setColor(Color.RED);
        g2.drawRect(x, y, w, h);
        g2.dispose();
        ImageIO.write(bi, "jpg", file);
        return FileUtils.readFileToByteArray(file);
    }

    @GetMapping(value = "/faceLog/subimages/{cameraId}/{fileName:[\\w.]+}")
    @ResponseBody
    public byte[] getSubImage(@PathVariable("fileName") String fileName, @PathVariable("cameraId") Integer cameraId, int x, int y, int w, int h) throws IOException {

        File file = new File(basePath + File.separator + cameraId + File.separator + fileName + ".sub" + "." + x + "." + y + "." + w + "." + h);
        if (file.exists()) return FileUtils.readFileToByteArray(file);

        File file2 = new File(basePath + File.separator + cameraId + File.separator + fileName);
        BufferedImage bi = ImageIO.read(file2);
        ImageIO.write(bi.getSubimage(x, y, w, h), "jpg", file);
        return FileUtils.readFileToByteArray(file);
    }


    @GetMapping(value = "/faceLog/logs")
    public ResponseEntity getLogs(FaceLogDTO faceLogDTO, Pageable pageable) {
        return new ResponseEntity(faceLogQueryService.queryAll(faceLogDTO, pageable), HttpStatus.OK);
    }

    //    @Log(description = "[NO-USER]查询全部人脸库")
    @GetMapping(value = "/users")
    public ResponseEntity<Object> getAllUsers() {
        return new ResponseEntity<>(faceUserQueryService.queryAll(new FaceUserDTO()), HttpStatus.OK);
    }

    //    @Log(description = "[NO-USER]查询摄像头信息")
    @GetMapping(value = "/camera/{id}")
    public ResponseEntity<CameraDTO> getCameraInfo(@PathVariable Long id) {
        return new ResponseEntity<>(cameraService.findById(id), HttpStatus.OK);
    }

    //    @Log(description = "[NO-USER]批量查询摄像头信息")
    @GetMapping(value = "/camera/all")
    public ResponseEntity<Object> getAllCameraInfo() {
        return new ResponseEntity<>(cameraQueryService.queryAll(new CameraDTO()), HttpStatus.OK);
    }

    //    @Log(description = "[NO-USER]创建人脸识别记录")
    @PutMapping(value = "/face/logs")
    public ResponseEntity<FaceLogDTO> create(@Validated @RequestBody FaceLog resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + "face-log" + " cannot already have an ID");
        }
        return new ResponseEntity<>(faceLogService.create(resources), HttpStatus.CREATED);
    }

    //    @Log(description = "[NO-USER]批量创建人脸识别记录")
    @PutMapping(value = "/face/logs/all")
    public ResponseEntity<String> createAll(@Validated @RequestBody List<FaceLog> resources) {
        List<FaceLogDTO> all = faceLogService.createAll(resources);
        return new ResponseEntity<>("{}", HttpStatus.CREATED);
    }
}

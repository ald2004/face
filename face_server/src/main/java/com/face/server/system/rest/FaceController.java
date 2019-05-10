package com.face.server.system.rest;

import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;
import com.face.sdk.main.FaceUserEntity;
import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.FileUtil;
import com.face.server.common.utils.RequestHolder;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.domain.FaceUser;
import com.face.server.system.service.FaceUserService;
import com.face.server.system.service.dto.FaceUserDTO;
import com.face.server.system.service.query.FaceUserQueryService;
import com.face.server.tools.domain.Picture;
import com.face.server.tools.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.face.sdk.util.ExcelUtil.readExcel;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping("api/face")
public class FaceController {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private FaceUserQueryService faceUserQueryService;
    private static final String ENTITY_NAME = "face-user";
    @Autowired
    private FaceUserService faceUserService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static FaceSDK faceSDK = new FaceSDK();

    static {
        faceSDK.faceModelConf(new float[]{0.7f, 0.8f, 0.9f}, 120);
        faceSDK.faceModelInit(4);
    }

    @Log(description = "批量导入FaceUsers")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','USER_CREATE')")
    @PostMapping(value = "/upload/faceUsers")
    public ResponseEntity<Map<String, java.io.Serializable>> uploadFaceUsers(@RequestParam("file") MultipartFile multipartFile) {
        String userName = jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest());
        Map<String, java.io.Serializable> map = new HashMap<>();

        ArrayList<String> errMsg = new ArrayList<>();
        ArrayList<FaceUser> faceUsers = new ArrayList<>();
        ArrayList<Picture> pictures = new ArrayList<>();
        try {
            File file = FileUtil.toFile(multipartFile);
            String zipFilePath = file.getAbsolutePath();
            // 一般情况都是 windows 压缩的。默认GBK,linux 的用户UTF-8
            ZipFile zip = new ZipFile(zipFilePath, Charset.forName("GBK"));
//            Enumeration<? extends ZipEntry> entrys = zip.entries();

            ZipEntry excel = zip.getEntry("users.xlsx") == null ? zip.getEntry("users.xls") : zip.getEntry("users.xlsx");

            if (excel == null) throw new IllegalArgumentException("ZIP 文件格式错误，无法找到 users.xlsx 或 users.xls .");

            List<FaceUserEntity> faceUserEntities = readExcel(zip.getInputStream(excel), true);

            for (FaceUserEntity faceUserEntity : faceUserEntities) {
                ZipEntry photo = zip.getEntry(faceUserEntity.getPhoto());
                if (photo == null) {
                    errMsg.add(excel.getName() + " 文件描述错误，" + faceUserEntity.getName() + " 对应的图片未找到：" + faceUserEntity.getPhoto());
                } else {
                    try {
                        BufferedImage image = ImageIO.read(zip.getInputStream(photo));
                        if (image == null || (image.getWidth() < 160 || image.getHeight() < 160)) {
                            errMsg.add(faceUserEntity.getName() + " 的图片[" + faceUserEntity.getPhoto() + "]尺寸小于 160 x 160 像素，请更换更大更清晰的照片。");
                            continue;
                        }
                        long start = System.currentTimeMillis();
                        ArrayList<FACE_BOX> faceBoxes = new ArrayList<FACE_BOX>();
                        faceDetect(image, faceBoxes);
                        long end = System.currentTimeMillis();
                        log.info((end - start) + "ms");

                        if (faceBoxes.size() > 0) {
                            //throw new BadRequestException("检测到多张人脸！");
                        } else {
                            errMsg.add(faceUserEntity.getName() + " 的图片[" + faceUserEntity.getPhoto() + "]未检测到脸，请更换更大更清晰的照片。");
                            continue;
                        }

                        for (FACE_BOX faceBox : faceBoxes) {

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(
                                    image.getSubimage(faceBox.x, faceBox.y, faceBox.width, faceBox.height),
                                    "jpeg",
                                    baos);
                            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                            Picture picture = pictureService.upload(zip.getInputStream(photo), userName);
                            Picture picture1 = pictureService.upload(bais, userName);

                            pictures.add(picture);
                            pictures.add(picture1);
                            String embedding = Arrays.toString(faceBox.embedding);
                            FaceUser faceUser = new FaceUser();
                            BeanUtils.copyProperties(faceUserEntity, faceUser);
                            faceUser.setEmbedding(embedding);
                            faceUser.setPhoto(picture.getUrl());
                            faceUser.setFacePhoto(picture1.getUrl());
                            faceUsers.add(faceUser);
                            break;
                        }
                    } catch (IOException e) {
                        errMsg.add(faceUserEntity.getName() + " 的图片[" + faceUserEntity.getPhoto() + "]格式错误,支持PNG、JPEG、GIF、BMP 等常见图片格式。");
                    }
                }
            }

            zip.close();
            List<FaceUserDTO> faceUserDTOS = (List<FaceUserDTO>) faceUserQueryService.queryAll(new FaceUserDTO());

            for (FaceUser faceUser : faceUsers) {
                for (FaceUser faceUser2 : faceUsers) {
                    if (!faceUser.equals(faceUser2) && faceUser.getIdCard().equals(faceUser2.getIdCard())) {
                        errMsg.add("重复的身份证号[" + faceUser.getIdCard() + "]，请检查Excel中的信息！");
                        break;
                    }
                }
            }

            for (FaceUser faceUser : faceUsers) {
                for (FaceUserDTO faceUserDTO : faceUserDTOS) {
                    if (faceUserDTO.getIdCard().equals(faceUser.getIdCard())) {
                        errMsg.add("人脸库中已存在此身份证号:" + faceUserDTO.getIdCard());
                    }
                }
            }
        } catch (Exception e) {
            errMsg.add("解压文件失败，请上传 *.zip 格式压缩文件，注意不是 *.rar");
        }

        if (errMsg.isEmpty()) {
            faceUserService.createAll(faceUsers);
        } else {
            pictureService.deleteAll(pictures);
        }

        map.put("errno", errMsg.isEmpty());
        map.put("errMsg", errMsg);
        map.put("data", new String[]{});
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    private void faceDetect(BufferedImage image, ArrayList<FACE_BOX> faceBoxes) {
        faceSDK.faceDetect(image, faceBoxes);
    }

    /**
     * 上传人脸照片ThreadPoolExecutor
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Log(description = "上传人脸照片")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','USER_CREATE')")
    @PostMapping(value = "/upload/pictures")
    public ResponseEntity<Map<String, java.io.Serializable>> upload(@RequestParam MultipartFile file) {
        String userName = jwtTokenUtil.getUserName(RequestHolder.getHttpServletRequest());

        File file1 = FileUtil.toFile(file);
        ByteArrayInputStream bais = null;

        String embedding = "";
        try {
            BufferedImage image = ImageIO.read(file1);


            ArrayList<FACE_BOX> faceBoxes = new ArrayList<FACE_BOX>();

            long start = System.currentTimeMillis();
            faceDetect(image, faceBoxes);
            long end = System.currentTimeMillis();
            log.info((end - start) + "ms");
            if (faceBoxes.size() > 0) {
                //throw new BadRequestException("检测到多张人脸！");
            } else {
                throw new BadRequestException("未检测到脸,请重新上传清晰的照片。");
            }

            for (FACE_BOX faceBox : faceBoxes) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(
                        image.getSubimage(faceBox.x, faceBox.y, faceBox.width, faceBox.height),
                        "jpeg",
                        baos);
                bais = new ByteArrayInputStream(baos.toByteArray());
                embedding = Arrays.toString(faceBox.embedding);
                break;
            }

        } catch (IOException e) {
            // 要报错早报错了。
        }
        Picture picture = pictureService.upload(file1, userName);
        Picture picture1 = pictureService.upload(bais, userName);

        Map<String, java.io.Serializable> map = new HashMap<>();
        map.put("errno", 0);
        map.put("id", picture.getId());
        map.put("data", new String[]{picture.getUrl(), picture1.getUrl()});
        map.put("embedding", embedding);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Log(description = "统计人脸库数量")
    @GetMapping(value = "/count")
    public ResponseEntity count() {
        return new ResponseEntity(faceUserService.count(), HttpStatus.OK);
    }

    @Log(description = "查询人脸库")
    @GetMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','FACE_USER_SELECT')")
    public ResponseEntity getUsers(FaceUserDTO faceUserDTO, Pageable pageable) {
        return new ResponseEntity(faceUserQueryService.queryAll(faceUserDTO, pageable), HttpStatus.OK);
    }

    @Log(description = "新增人脸库")
    @PostMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','USER_CREATE')")
    public ResponseEntity<FaceUserDTO> create(@Validated @RequestBody FaceUser resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(faceUserService.create(resources), HttpStatus.CREATED);
    }

    @Log(description = "修改人脸库")
    @PutMapping(value = "/users")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','USER_EDIT')")
    public ResponseEntity update(@Validated @RequestBody FaceUser resources) {
        if (resources.getId() == null) {
            throw new BadRequestException(ENTITY_NAME + " ID Can not be empty");
        }
        faceUserService.update(resources);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Log(description = "删除人脸库")
    @DeleteMapping(value = "/users/{ids}")
    @PreAuthorize("hasAnyRole('ADMIN','FACE_USER_ALL','FACE_USER_DELETE')")
    public ResponseEntity delete(@PathVariable String ids) {

        if (!ids.matches("^(\\d+,)*\\d+$")) {
            throw new BadRequestException("ids:必须符合  ^(\\d+,)*\\d+$ 格式。如 1,2,3,4,5 ");
        }

        String[] split = ids.split(",");
        List<Long> idArrays = new ArrayList<>();
        for (String aSplit : split) {
            idArrays.add(Long.parseLong(aSplit));
        }

        faceUserQueryService.delete(idArrays);
        return new ResponseEntity(HttpStatus.OK);
    }
}

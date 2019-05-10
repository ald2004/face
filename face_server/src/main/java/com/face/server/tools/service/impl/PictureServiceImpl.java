package com.face.server.tools.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.FileUtil;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.tools.domain.Picture;
import com.face.server.tools.repository.PictureRepository;
import com.face.server.tools.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import com.face.server.common.exception.BadRequestException;
import com.face.server.common.utils.FileUtil;
import com.face.server.common.utils.ValidationUtil;
import com.face.server.tools.domain.Picture;
import com.face.server.tools.repository.PictureRepository;
import com.face.server.tools.service.PictureService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @author jie
 * @date 2018-12-27
 */
@Slf4j
@Service(value = "pictureService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class PictureServiceImpl implements PictureService {

    @Autowired
    private PictureRepository pictureRepository;

    public static final String SUCCESS = "success";

    public static final String CODE = "code";

    public static final String MSG = "msg";
    @Value("${face.base-path}")
    private String basePath;

/*    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Picture upload(MultipartFile multipartFile, String username) {
        File file = FileUtil.toFile(multipartFile);
        //将参数合成一个请求
        RestTemplate rest = new RestTemplate();

        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("smfile", resource);

        //设置头部，必须
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(param,headers);
        ResponseEntity<String> responseEntity = rest.exchange("https://sm.ms/api/upload", HttpMethod.POST, httpEntity, String.class);

        JSONObject jsonObject = JSONUtil.parseObj(responseEntity.getBody());
        Picture picture = null;
        if(!jsonObject.get(CODE).toString().equals(SUCCESS)){
           throw new BadRequestException(jsonObject.get(MSG).toString());
        }
        //转成实体类
        picture = JSON.parseObject(jsonObject.get("data").toString(), Picture.class);
        picture.setSize(FileUtil.getSize(Integer.valueOf(picture.getSize())));
        picture.setUsername(username);
        picture.setFilename(FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename())+FileUtil.getExtensionName(multipartFile.getOriginalFilename()));
        pictureRepository.save(picture);
        //删除临时文件
        FileUtil.deleteFile(file);
        return picture;
    }*/

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Picture upload(File file, String username) {

        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) throw new IOException();

            Picture picture = new Picture();
            String fileName = IdUtil.simpleUUID() + "." + FileUtil.getExtensionName(file.getName());
            File output = new File(basePath + fileName);
            FileUtils.forceMkdir(new File(basePath));
            FileUtils.moveFile(file, output);

            //转成实体类
            picture.setFilename(fileName);
            picture.setUrl("api/images/" + fileName);
            picture.setCreateTime(new Timestamp(System.currentTimeMillis()));
            picture.setWidth(String.valueOf(img.getWidth()));
            picture.setHeight(String.valueOf(img.getHeight()));
            picture.setSize(FileUtil.getSize(output.length()));
            picture.setDelete(output.getAbsolutePath());
            picture.setUsername(username);
            pictureRepository.save(picture);
          /*  //删除临时文件
            FileUtil.deleteFile(file);*/
            return picture;
        } catch (IOException e) {
            throw new BadRequestException("格式错误，请上传图片格式。");
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Picture upload(MultipartFile multipartFile, String username) {
        File file = FileUtil.toFile(multipartFile);
        return upload(file, username);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Picture upload(InputStream inputStream, String userName) {
        try {
            BufferedImage img = ImageIO.read(inputStream);
            if (img == null) throw new IOException();

            Picture picture = new Picture();
            String fileName = IdUtil.simpleUUID() + ".jpg";
            File output = new File(basePath + fileName);
            FileUtils.forceMkdir(new File(basePath));
            ImageIO.write(img, "jpeg", output);

            //转成实体类
            picture.setFilename(fileName);
            picture.setUrl("api/images/" + fileName);
            picture.setCreateTime(new Timestamp(System.currentTimeMillis()));
            picture.setWidth(String.valueOf(img.getWidth()));
            picture.setHeight(String.valueOf(img.getHeight()));
            picture.setSize(FileUtil.getSize(output.length()));
            picture.setDelete(output.getAbsolutePath());
            picture.setUsername(userName);
            pictureRepository.save(picture);
          /*  //删除临时文件
            FileUtil.deleteFile(file);*/
            return picture;
        } catch (IOException e) {
            throw new BadRequestException("格式错误，请上传图片格式。");
        }
    }

    @Override
    public Picture findById(Long id) {
        Optional<Picture> picture = pictureRepository.findById(id);
        ValidationUtil.isNull(picture, "Picture", "id", id);
        return picture.get();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Picture picture) {
        try {
            boolean delete = new File(picture.getDelete()).delete();
            //如果删除的地址出错，直接删除数据库数据
            pictureRepository.delete(picture);
        } catch (Exception e) {
            pictureRepository.delete(picture);
        }
    }

    @Override
    public void deleteAll(ArrayList<Picture> pictures) {
        for (Picture picture : pictures) {
            try {
                boolean delete = new File(picture.getDelete()).delete();
            } catch (Exception ignored) {
            }
        }
        pictureRepository.deleteAll();
    }

}

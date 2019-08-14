package com.face.sdk.main;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.face.sdk.util.ExcelUtil.readExcel;

/**
 * project : face-web-server
 * Code Create : 2019/4/28
 * Class : com.face.sdk.main.ZipTest
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class ZipTest {
    public ZipTest() {
    }


    public static void main(String[] args) throws IOException, InvalidFormatException {
        String zipFilePath = "D:\\上传示例\\上传示例.zip";

        // 一般情况都是 windows 压缩的。默认GBK,linux 的用户UTF-8
        ZipFile zip = new ZipFile(zipFilePath, Charset.forName("GBK"));

        Enumeration<? extends ZipEntry> entrys = zip.entries();

        ZipEntry excel = zip.getEntry("users.xlsx") == null ? zip.getEntry("users.xls") : zip.getEntry("users.xlsx");

        if (excel == null) throw new IllegalArgumentException("ZIP 文件格式错误，无法找到 users.xlsx 或 users.xls .");

        List<FaceUserEntity> faceUserEntities = readExcel(zip.getInputStream(excel), true);

        List<String> errMsg = new ArrayList<>();
        for (FaceUserEntity faceUserEntity : faceUserEntities) {
            ZipEntry photo = zip.getEntry(faceUserEntity.getPhoto());
            if (photo == null) {
                errMsg.add(excel.getName() + " 文件描述错误，" + faceUserEntity.getName() + " 对应的图片未找到：" + faceUserEntity.getPhoto());
            } else {
                try {
                    BufferedImage bufferedImage = ImageIO.read(zip.getInputStream(photo));
                    if (bufferedImage == null || (bufferedImage.getWidth() < 160 || bufferedImage.getHeight() < 160)) {
                        errMsg.add(faceUserEntity.getName() + " 的图片[" + faceUserEntity.getPhoto() + "]尺寸小于 160 x 160 像素，请更换更大更清晰的照片。");
                    }
                } catch (IOException e) {
                    errMsg.add(faceUserEntity.getName() + " 的图片[" + faceUserEntity.getPhoto() + "]格式错误,支持PNG、JPEG、GIF、BMP 等常见图片格式。");
                }
            }
        }
        for (String s : errMsg) {
            System.out.println(s);
        }

        zip.close();
    }


}

package com.face.sdk.main;

import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Thread.MAX_PRIORITY;

/**
 * project : face-web-server
 * Code Create : 2019/5/5
 * Class : com.face.sdk.main.TestJNI
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class TestJNI {
    public TestJNI() {
    }

    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread thread = new Thread(() -> {
            FaceSDK faceSDK = new FaceSDK();

            faceSDK.faceModelInit(4);
            BufferedImage read = null;
            try {
                read = ImageIO.read(new File(args[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ArrayList<FACE_BOX> faceBoxes = new ArrayList<>();

            long start = System.currentTimeMillis();
            faceSDK.faceDetect(read, faceBoxes);
            long end = System.currentTimeMillis();
            faceBoxes.forEach(System.out::println);
            System.out.println(end - start + "ms");

        });
        thread.setPriority(MAX_PRIORITY);
        thread.start();

    }


}

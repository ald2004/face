package com.face.sdk.main;

import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.face.sdk.main.Main.getFaceUsers;

/**
 * project : face-web-server
 * Code Create : 2019/5/7
 * Class : com.face.sdk.main.TestFace
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class TestFace {
    public TestFace() {
    }

    public static void main(String[] args) throws IOException {
        String usersURL = "http://localhost/api/no-user/users";
        String videoURL = "rtsp://admin:111111ab@192.168.1.64:554/";
        FaceSDK faceSDK = new FaceSDK();
        long frame = faceSDK.createMat();
        faceSDK.faceModelConf(new float[]{0.9f, 0.9f, 0.99f}, 120);
        faceSDK.faceModelInit(4);
        List<FaceUserEntity> faceUserEntityList = getFaceUsers(usersURL);
        if (faceUserEntityList.isEmpty()) throw new RuntimeException("no face user.");

        long videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                ArrayList<FACE_BOX> faceBoxes = new ArrayList<FACE_BOX>();

                long start = System.currentTimeMillis();
                faceSDK.faceDetect(frame, faceBoxes);
                for (FACE_BOX faceBox : faceBoxes) {

                    double maxSimilar = -1;
                    FaceUserEntity maxEntity = null;
                    for (FaceUserEntity entity : faceUserEntityList) {
                        double similar = faceSDK.calcSimilar(entity.getEmbedding(), faceBox.embedding);
                        if (similar > maxSimilar) {
                            maxSimilar = similar;
                            maxEntity = entity;
                        }
                    }

                    if (maxSimilar > 0.55) {
                        faceSDK.rectangleMat(frame, faceBox.x, faceBox.y, faceBox.width, faceBox.height, 255, 0, 0);
                        System.out.println(Math.abs(faceBox.angle) + ":" + maxEntity);
                    } else {

                    }

                }
                long end = System.currentTimeMillis();
//                System.out.println(end - start + "ms");
                faceSDK.showMat("test".toCharArray(), frame);
                faceSDK.waitKey(1);
            }
        }).start();

        while (true) {
            if (!faceSDK.isVideoOpened(videoCapture) || faceSDK.read(videoCapture, frame) == -1) {
                System.err.println("read img err.");
                faceSDK.releaseVideo(videoCapture);
                videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());
                continue;
            }

        }

    }
}

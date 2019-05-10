/*
package com.face.sdk.main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.face.sdk.jni.FACE_BOX;
import com.face.sdk.jni.FaceSDK;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

*/
/**
 * project : face-web-server
 * Code Create : 2019/4/29
 * Class : com.face.sdk.main.TrackTest
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 *//*

public class TrackTest {

    private static List<FaceUserEntity> faceUserEntityList;
    private static FaceSDK faceSDK;
    private static CameraEntity cameraEntity;
    private static String videoURL;
    private static long frame;
    private static long videoCapture;
    private static ArrayList<FaceLogEntity> faceLogEntities;
    private static String usersURL;
    private static String faceLogURL;
    private static String cameraURL;
    private static long time;

    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]);
    }

    private static List<FaceUserEntity> getFaceUsers(String url) throws IOException {
        List<FaceUserEntity> faceUserEntityList = new ArrayList<>();
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        client.executeMethod(getMethod);

        if (getMethod.getStatusCode() != 200) throw new RuntimeException("getMethod.getStatusCode() != 200");
        JSONArray array = JSON.parseArray(getMethod.getResponseBodyAsString());
        for (Object o : array) {
            if (o instanceof JSONObject) {
                JSONObject json = (JSONObject) o;
                FaceUserEntity entity = new FaceUserEntity();
                entity.setId(json.getLongValue("id"));
                entity.setName(json.getString("name"));
                entity.setStatus(json.getInteger("status"));
                entity.setDes(json.getString("des"));
                entity.setFacePhoto(json.getString("facePhoto"));
                entity.setPhoto(json.getString("photo"));
                entity.setIdCard(json.getString("idCard"));
                entity.setPhone(json.getString("phone"));
                JSONArray embeddingJSON = json.getJSONArray("embedding");
                float[] embedding = new float[embeddingJSON.size()];
                for (int i = 0; i < embeddingJSON.size(); i++) {
                    embedding[i] = embeddingJSON.getFloatValue(i);
                }
                entity.setEmbedding(embedding);
                faceUserEntityList.add(entity);
            }
        }
        return faceUserEntityList;
    }

    private static CameraEntity getCameraInfo(String url) throws IOException {

        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        client.executeMethod(getMethod);

        if (getMethod.getStatusCode() != 200) throw new RuntimeException("getMethod.getStatusCode() != 200");

        JSONObject jsonObject = JSON.parseObject(getMethod.getResponseBodyAsString());

        CameraEntity cameraEntity = new CameraEntity();

        cameraEntity.setId(jsonObject.getLongValue("id"));
        cameraEntity.setIp(jsonObject.getString("ip"));
        cameraEntity.setPort(jsonObject.getIntValue("port"));
        cameraEntity.setUsername(jsonObject.getString("username"));
        cameraEntity.setPassword(jsonObject.getString("password"));
        cameraEntity.setStatus(jsonObject.getIntValue("status"));
        cameraEntity.setNumber(jsonObject.getString("number"));
        cameraEntity.setRegion(jsonObject.getString("region"));

        return cameraEntity;
    }


    private static int saveLog(List<FaceLogEntity> faceLogEntity, String url) {
        if (faceLogEntity.isEmpty()) return 0;
        System.out.println(faceLogEntity);
        return 0;

    }

    public static void main(String[] args) throws Exception {
        int pid = getProcessID();
        boolean newFile = new File(pid + ".pid").createNewFile();
        usersURL = args[0];
        cameraURL = args[1];
        faceLogURL = args[2];
        init();
        System.out.println("inited.");

        if (videoCapture == -1) {
            System.out.println("open video err.");
            return;
        }


        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (faceSDK.isVideoOpened(videoCapture)) {
                        long start = System.currentTimeMillis();
                        execute();
                        long end = System.currentTimeMillis();
//                        System.out.println("execute:" + (end - start) + "ms");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();

        while (true) {
            if (!faceSDK.isVideoOpened(videoCapture) || faceSDK.read(videoCapture, frame) == -1) {
                System.err.println("read img err.");
                faceSDK.releaseVideo(videoCapture);
                videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());
                continue;
                // 不重新尝试连接，直接结束。
//                System.exit(1);
            }
//            faceSDK.showMat(frame);
//            faceSDK.waitKey(1);


        }
    }

    static int ID;
    static Map<Integer, TrackData> trackMap = new HashMap<>();

    private static void execute() throws Exception {


        // 删除超过10秒未再次出现的数据
        trackMap.entrySet().removeIf(entry -> {
            TrackData td = entry.getValue();
            boolean delete = ((System.currentTimeMillis() - td.updateTime.getTime()) > 1000);

            if (delete) {

                String id = UUID.randomUUID().toString().toUpperCase().replace("-", "");
                boolean save = false;
                String logImg = id + ".jpg";
                // 删除前未被识别
                if (!td.commited && td.faceBoxes.size() > 2) {
                    try {
                        faceSDK.writeMat(td.mat, (logImg).toCharArray());
                        commit(SimilarData.STRANGER, td, logImg);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                // 删除要释放
                if (td.mat > 0)
                    faceSDK.releaseMat(td.mat);
            }
            return delete;
        });

        String id = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        boolean save = false;
        String logImg = id + ".jpg";

        // 处理时间较久，避免在处理过程中图片发生变化
        long frameClone = faceSDK.createMat();
        faceSDK.cloneMat(frame, frameClone);

        ArrayList<FACE_BOX> faceBoxes = new ArrayList<FACE_BOX>();

        long start = System.currentTimeMillis();
        faceSDK.faceDetect(frameClone, faceBoxes);
        for (FACE_BOX faceBox : faceBoxes) {

            double maxSimilar = -1;
            double maxIOU = -1;
            TrackData maxData = null;
            for (TrackData data : trackMap.values()) {

                // 现实情况 一张图片中同一个人出现次数最多一次。
                float iou = IOUUtil.boxIOU(
                        data.faceBox.x, data.faceBox.y, data.faceBox.width, data.faceBox.height,
                        faceBox.x, faceBox.y, faceBox.width, faceBox.height
                );
                double similar = faceSDK.calcSimilar(data.faceBox.embedding, faceBox.embedding);

                if (iou > 0.6 || similar > 0.7) {
                    if (similar > maxSimilar) {
                        maxSimilar = similar;
                        maxIOU = iou;
                        maxData = data;
                    } else if (maxIOU > iou) {
                        maxSimilar = similar;
                        maxIOU = iou;
                        maxData = data;
                    }
                }
            }

            // 未匹配，新的追踪
            if (maxData == null) {
                int number = ++ID;

                long frameClone2 = faceSDK.createMat();
                faceSDK.cloneMat(frameClone, frameClone2);
                trackMap.put(number, new TrackData(faceBox, number, frameClone2));

            } else {
                //曾经出现过，老的追踪
                maxData.faceBoxes.add(faceBox);
                maxData.faceBox = faceBox;

                if (maxData.mat > 0)
                    faceSDK.releaseMat(maxData.mat);
                long frameClone2 = faceSDK.createMat();
                faceSDK.cloneMat(frameClone, frameClone2);
                maxData.mat = frameClone2;
                maxData.updateTime = new Date();
            }

        }
        long end = System.currentTimeMillis();
//        System.out.println("track:" + (end - start) + "ms");


        for (Map.Entry<Integer, TrackData> dataEntry : trackMap.entrySet()) {
            // 已提交过的避免重复提交
            TrackData td = dataEntry.getValue();
            if (td.commited) continue;

            SimilarData similarData = getSimilarData(td.faceBox.embedding);

            // 超过阈值可以发送
            if (similarData.maxSimilar > 0.55) {
                save = true;
                commit(similarData.maxEntity, td, logImg);
            } else {

                // 未识别的时间
                long unknownTime = td.updateTime.getTime() - td.createTime.getTime();
                // 2秒内未被识别，并且追踪到3张照片,断定为陌生人
                if ((unknownTime > 2000 && td.faceBoxes.size() > 3)) {
                    save = true;
                    commit(SimilarData.STRANGER, td, logImg);
                }
            }

        }


        faceSDK.showMat("clone".toCharArray(), frameClone);
        faceSDK.waitKey(1);

        if (save) {
            faceSDK.writeMat(frameClone, (logImg).toCharArray());
        }
        if (faceLogEntities.size() > 100 || System.currentTimeMillis() - time > 1000 * 2) {
            if (saveLog(faceLogEntities, faceLogURL) != 0) {
                System.out.println("提交Log失败");
                for (FaceLogEntity faceLogEntity : faceLogEntities) {
                    File file = new File(faceLogEntity.getLogImg());
                    if (file.exists()) FileUtils.forceDelete(file);
                }
            }

            // 不管成功失败都丢弃
            faceLogEntities.clear();
            time = System.currentTimeMillis();
        }
        faceSDK.releaseMat(frameClone);
    }

    private static void commit(FaceUserEntity faceUserEntity, TrackData td, String logImg) throws InvocationTargetException, IllegalAccessException {
        FaceLogEntity faceLogEntity = new FaceLogEntity();
        // faceUser数据
        BeanUtils.copyProperties(faceLogEntity, faceUserEntity);
        // 摄像头数据
        BeanUtils.copyProperties(faceLogEntity, cameraEntity);
        // 人脸框数据
        BeanUtils.copyProperties(faceLogEntity, td.faceBox);

        faceLogEntity.setPoints(Arrays.toString(td.faceBox.points));

        faceLogEntity.setStatus(faceUserEntity.getStatus());
        faceLogEntity.setFaceUserStatus(faceUserEntity.getStatus());
        faceLogEntity.setCameraStatus(cameraEntity.getStatus());
        faceLogEntity.setLogImg(logImg);

        faceLogEntity.setCameraId(cameraEntity.getId());
        faceLogEntity.setFaceUserId(faceUserEntity.getId());

        faceLogEntities.add(faceLogEntity);
        // 标记为已提交
        td.commited = true;
        System.out.println(faceLogEntity.getName());
    }

    private static SimilarData getSimilarData(float[] embedding) {

        double maxSimilar = -1;
        FaceUserEntity maxEntity = null;
        for (FaceUserEntity entity : faceUserEntityList) {
            double similar = faceSDK.calcSimilar(entity.getEmbedding(), embedding);
            if (maxSimilar < similar) {
                maxSimilar = similar;
                maxEntity = entity;
            }
        }
        return new SimilarData(maxSimilar, maxEntity);
    }

    private static void init() throws IOException {

//        System.setOut(new PrintStream(new FileOutputStream("out.log")));
//        System.setErr(new PrintStream(new FileOutputStream("err.log")));

        faceLogEntities = new ArrayList<>();

        faceSDK = new FaceSDK();
        frame = faceSDK.createMat();
        faceSDK.faceModelConf(new float[]{0.9f, 0.9f, 0.99f}, 100);
        faceUserEntityList = getFaceUsers(usersURL);
        if (faceUserEntityList.isEmpty()) throw new RuntimeException("no face user.");
        cameraEntity = getCameraInfo(cameraURL);
        faceSDK.faceModelInit(3);
        videoURL = String.format("rtsp://%s:%s@%s:%s",
                cameraEntity.getUsername(),
                cameraEntity.getPassword(),
                cameraEntity.getIp(),
                cameraEntity.getPort()
        );
        System.out.println(videoURL);
        videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());
//        videoCapture = faceSDK.VideoCapture(0);

    }
}


*/

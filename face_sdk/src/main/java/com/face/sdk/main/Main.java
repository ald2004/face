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

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {
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
    // 防止重复 提交，key user id，value createTime;
    private static Map<Long, Long> duplicationMap;
    private static int ID;
    private static Map<Integer, TrackData> trackMap = new HashMap<>();


    // 追踪丢失超时间
    private final static long OUT_TIME = 1000;
    // N 秒内不重复提交已知 人脸
    private final static long DUPLICATION_TIME = 10 * 1000L;

    // 最小追踪的IOU
    private final static double MIN_IOU = 0.2;

    // 最小追踪的 相似度
    private final static double MIN_SIMILAR = 0.55;

    // 人脸识别 阈值
    private final static double SUCCESS_SIMILAR = 0.55;

    // 最大可识别角度
    private final static float MAX_ANGLE = 60;

    // 多张角度时 最大平均角度
    private final static double MAX_AVG_ANGLE = 65;


    // 提交批次时间 ， 超时 提交。
    private final static long COMMIT_TIME = 1500;
    // 提交批次条数，超过 N 条提交
    private final static int COMMIT_SIZE = 10;


    // ================ 低配 =============
    // 确认是陌生人的时间
    private final static long UNKNOWN_TIME = 6000;
    // 确认是陌生人最少帧数
    private final static int UNKNOWN_FACE_SIZE = 0;
    // 超时删除时 确定为陌生人最少的帧数
    private final static int REMOVE_UNKNOWN_SIZE = 0;
    // ================ 低配 =============

   /* // ================ 中配 =============
    // 确认是陌生人的时间
    private final static long UNKNOWN_TIME = 6000;
    // 确认是陌生人最少帧数
    private final static int UNKNOWN_FACE_SIZE = 15;
    // 超时删除时 确定为陌生人最少的帧数
    private final static int REMOVE_UNKNOWN_SIZE = 4;
    // ================ 中配 =============*/

   /*
    // ================ 高配 =============
    // 确认是陌生人的时间
    private final static long UNKNOWN_TIME = 6000;
    // 确认是陌生人最少帧数
    private final static int UNKNOWN_FACE_SIZE = 5;
    // 超时删除时 确定为陌生人最少的帧数
    private final static int REMOVE_UNKNOWN_SIZE = 6;
    // ================ 高配 =============
*/

    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]);
    }

    public static List<FaceUserEntity> getFaceUsers(String url) throws IOException {
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


    private static int saveLog() {
        if (faceLogEntities.isEmpty()) return 0;

        // 超过10秒的删除
        duplicationMap.entrySet().removeIf(longLongEntry -> (System.currentTimeMillis() - longLongEntry.getValue()) > DUPLICATION_TIME);

        List<FaceLogEntity> faceLogEntityArrayList = new ArrayList<>();

        // 去重，一次提交不能存在 多个相同的人脸库中的人
        for (FaceLogEntity faceLogEntity : faceLogEntities) {
            // 10 秒内不重复提交
            if ("0000".equals(faceLogEntity.getIdCard())) {
                faceLogEntityArrayList.add(faceLogEntity);
            } else if (!"0000".equals(faceLogEntity.getIdCard()) && !duplicationMap.containsKey(faceLogEntity.getFaceUserId())) {
                duplicationMap.put(faceLogEntity.getFaceUserId(), System.currentTimeMillis());
                faceLogEntityArrayList.add(faceLogEntity);
            }
        }

        HttpClient client = new HttpClient();
        PutMethod putMethod = new PutMethod(faceLogURL);
        for (FaceLogEntity logEntity : faceLogEntityArrayList) {
            logEntity.setId(null);
        }
        try {
            String content = JSON.toJSONString(faceLogEntityArrayList);
            System.out.println(content);
            putMethod.setRequestEntity(new StringRequestEntity(content, "application/json", "UTF-8"));
            client.executeMethod(putMethod);
        } catch (IOException e) {
            return -1;
        }

        if (putMethod.getStatusCode() != 201) return -1;
        return 0;

    }


    private static void commitFace() {
        if (faceLogEntities.size() > COMMIT_SIZE || System.currentTimeMillis() - time > COMMIT_TIME) {
            if (saveLog() != 0) {
                System.out.println("提交Log失败");
                for (FaceLogEntity faceLogEntity : faceLogEntities) {
                    File file = new File(faceLogEntity.getLogImg());
                    if (file.exists()) {
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // 不管成功失败都丢弃
            faceLogEntities.clear();
            time = System.currentTimeMillis();
        }
    }

    private static void discern() throws InvocationTargetException, IllegalAccessException {
        for (Map.Entry<Integer, TrackData> dataEntry : trackMap.entrySet()) {
            // 已提交过的避免重复提交
            TrackData td = dataEntry.getValue();
            if (td.commited) continue;
//            if (Math.abs(td.faceBox.angle) < 50) continue;

            SimilarData similarData = getSimilarData(td.faceBox.embedding);

            double sumSimilar = 0;
            double avgSimilar = 0;
            for (FACE_BOX faceBox : td.faceBoxes) {
                SimilarData sd = getSimilarData(faceBox.embedding);
                sumSimilar += sd.maxSimilar;
            }

            avgSimilar = sumSimilar / td.faceBoxes.size();

            // 头偏的太厉害,左右超过50度 ，范围为  0~90 度
            if (Math.abs(td.faceBox.angle) > MAX_ANGLE) continue;

            // 超过阈值可以发送
            if (similarData.maxSimilar > SUCCESS_SIMILAR) {
                commit(similarData.maxEntity, td/*, logImg*/);
            } else if (avgSimilar < 0.4) {

//                 未识别的时间
                long unknownTime = td.updateTime.getTime() - td.createTime.getTime();

                double sum = 0;
                for (FACE_BOX faceBox : td.faceBoxes) {
                    sum += Math.min(90, Math.abs(faceBox.angle));
                }

//                 超过2秒未被识别，并且追踪到3张照片,断定为陌生人
                if ((unknownTime > UNKNOWN_TIME && td.faceBoxes.size() > UNKNOWN_FACE_SIZE && (sum / td.faceBoxes.size()) < MAX_AVG_ANGLE)) {
                    System.out.println("commit avg1:" + (sum / td.faceBoxes.size()));
                    commit(SimilarData.STRANGER, td/*, logImg*/);
                }
            }

        }
    }

    /**
     * 显示追踪
     *
     * @param frameClone
     */
    private static void showTrackFace(long frameClone) {
        for (Map.Entry<Integer, TrackData> td : trackMap.entrySet()) {

            TrackData value = td.getValue();
            Integer key = td.getKey();

            faceSDK.rectangleMat(frameClone, value.faceBox.x, value.faceBox.y, value.faceBox.width, value.faceBox.height, 255, 0, 0);
            faceSDK.putTextMat(frameClone, ("Number:" + key + ",angle:" + value.faceBox.angle).toCharArray(), value.faceBox.x, value.faceBox.y, 0, 1, 255, 0, 0);
        }
        faceSDK.showMat("frameClone".toCharArray(), frameClone);
        faceSDK.waitKey(1);
    }

    /**
     * 追踪， trackMap
     *
     * @param frameClone
     */
    private static void trackFace(long frameClone) {
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

                if (iou > MIN_IOU || (similar > MIN_SIMILAR)) {
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
                // 新的追踪 如果人脸角度过大没有办法找到更好的图片


            } else {
                //曾经出现过，老的追踪
                maxData.faceBoxes.add(faceBox);
                maxData.updateTime = new Date();
                maxData.faceBox = faceBox;

                // 已经提交就不管了，不更换图片了
                if (maxData.commited) continue;

                // 历史追踪就可以考虑上次角度要小于这次，才替换图片
                if (Math.abs(maxData.minAngleFaceBox.angle) > Math.abs(faceBox.angle)) {

                    maxData.minAngleFaceBox = faceBox;

                    if (maxData.mat > 0) {
                        faceSDK.releaseMat(maxData.mat);
                        maxData.mat = -1;
                    }
                    long frameClone2 = faceSDK.createMat();
                    faceSDK.cloneMat(frameClone, frameClone2);
                    maxData.mat = frameClone2;

                }
            }

        }
        long end = System.currentTimeMillis();
//        System.out.println("track:" + (end - start) + "ms");
    }

    /**
     * 删除超时，并提交超时的陌生人
     */
    private static void clearTimeOutFace() {
        // 删除超过1秒未再次出现的数据
        trackMap.entrySet().removeIf(entry -> {
            TrackData td = entry.getValue();
            boolean delete = ((System.currentTimeMillis() - td.updateTime.getTime()) > OUT_TIME);

            if (delete) {

                long unknownTime = td.updateTime.getTime() - td.createTime.getTime();

                double sum = 0;
                for (FACE_BOX faceBox : td.faceBoxes) {
                    sum += Math.min(90, Math.abs(faceBox.angle));
                }

                // 删除前未被识别
                if (!td.commited && td.faceBoxes.size() > REMOVE_UNKNOWN_SIZE && (sum / td.faceBoxes.size()) < MAX_AVG_ANGLE) {
                    System.out.println("commit avg:" + (sum / td.faceBoxes.size()));
                    try {
                        commit(SimilarData.STRANGER, td);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                // 删除要释放
                if (td.mat > 0) {
                    faceSDK.releaseMat(td.mat);
                    td.mat = -1;
                }
            }
            return delete;
        });
    }

    private static void commit(FaceUserEntity faceUserEntity, TrackData td, String logImg) throws InvocationTargetException, IllegalAccessException {
        FaceLogEntity faceLogEntity = new FaceLogEntity();
        // faceUser数据
        BeanUtils.copyProperties(faceLogEntity, faceUserEntity);
        // 摄像头数据
        BeanUtils.copyProperties(faceLogEntity, cameraEntity);
        // 人脸框数据
        BeanUtils.copyProperties(faceLogEntity, td.minAngleFaceBox);

        faceLogEntity.setPoints(Arrays.toString(td.minAngleFaceBox.points));

        faceLogEntity.setStatus(faceUserEntity.getStatus());
        faceLogEntity.setFaceUserStatus(faceUserEntity.getStatus());
        faceLogEntity.setCameraStatus(cameraEntity.getStatus());
        faceLogEntity.setLogImg(logImg);

        faceLogEntity.setCameraId(cameraEntity.getId());
        faceLogEntity.setFaceUserId(faceUserEntity.getId());

        for (FaceLogEntity logEntity : faceLogEntities) {

            // 人脸库中的人员短时间内不能重复提交
            if (!"0000".equals(faceLogEntity.getIdCard()) && logEntity.getIdCard().equals(faceLogEntity.getIdCard())) {
                td.commited = true;
                return;
            }

        }

        faceLogEntities.add(faceLogEntity);
        // 标记为已提交
        td.commited = true;
        System.out.println(faceLogEntity.getName());
    }

    private static void commit(FaceUserEntity faceUserEntity, TrackData td) throws InvocationTargetException, IllegalAccessException {
        String id = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        String logImg = id + ".jpg";
        faceSDK.writeMat(td.mat, (logImg).toCharArray());
        commit(faceUserEntity, td, logImg);
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


    public static void main(String[] args) throws Exception {
        int pid = getProcessID();
        File file = new File(pid + ".pid");
        boolean newFile = file.createNewFile();
        FileUtils.forceDeleteOnExit(file);

        init(args);
        System.out.println("inited.");

        if (videoCapture == -1) {
            System.out.println("open video err.");
            return;
        }


        Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
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
//                videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());
//                continue;
                // 不重新尝试连接，直接结束。
                System.exit(1);
            }
//            faceSDK.showMat(frame);
//            faceSDK.waitKey(1);


        }
    }

    private static void execute() throws Exception {


        // 清除过期的人脸，并提交陌生人
        clearTimeOutFace();

        // 处理时间较久，避免在处理过程中图片发生变化
        long frameClone = faceSDK.createMat();
        try {
            faceSDK.cloneMat(frame, frameClone);

            // 追踪
            trackFace(frameClone);

            // 显示追踪
            //showTrackFace(frameClone);
        } finally {
            faceSDK.releaseMat(frameClone);
        }

        // 对追踪结果识别，比较
        discern();

        // 判断是否提交
        commitFace();


    }

    private static void init(String[] args) throws IOException {

        System.setOut(new PrintStream(new FileOutputStream("out.log")));
        System.setErr(new PrintStream(new FileOutputStream("err.log")));

        usersURL = args[0];
        cameraURL = args[1];
        faceLogURL = args[2];

        faceLogEntities = new ArrayList<>();
        duplicationMap = new HashMap<>();

        faceSDK = new FaceSDK();
        frame = faceSDK.createMat();
//        faceSDK.faceModelConf(new float[]{0.8f, 0.8f, 0.9f}, 100);
        faceSDK.faceModelConf(new float[]{0.9f, 0.9f, 0.99f},60);
        faceSDK.faceModelInit(8);
        faceUserEntityList = getFaceUsers(usersURL);
        if (faceUserEntityList.isEmpty()) throw new RuntimeException("no face user.");
        cameraEntity = getCameraInfo(cameraURL);
        videoURL = String.format("rtsp://%s:%s@%s:%s",
                cameraEntity.getUsername(),
                cameraEntity.getPassword(),
                cameraEntity.getIp(),
                cameraEntity.getPort()
        );
        System.out.println(videoURL);
        videoCapture = faceSDK.VideoCapture(videoURL.toCharArray());

    }
}

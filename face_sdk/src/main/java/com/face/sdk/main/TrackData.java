package com.face.sdk.main;

import com.face.sdk.jni.FACE_BOX;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * project : face-web-server
 * Code Create : 2019/4/27
 * Class : com.face.sdk.main.TrackData
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class TrackData {
    public TrackData(FACE_BOX faceBox, int number, long mat) {
        this(faceBox, number, new Date(), mat);
    }

    public TrackData(FACE_BOX faceBox, int number, Date createTime, long mat) {
        this.number = number;
        this.faceBox = faceBox;
        this.minAngleFaceBox = faceBox;
        this.createTime = createTime;
        this.updateTime = createTime;
        this.mat = mat;
        faceBoxes.add(faceBox);
    }

    int number;
    FACE_BOX faceBox;

    FACE_BOX minAngleFaceBox;

    List<FACE_BOX> faceBoxes = new ArrayList<>();
    Date createTime;
    Date updateTime;
    long mat;
    boolean commited;

    @Override
    public String toString() {
        return "TrackData{" +
                "number=" + number +
                ", faceBox=" + faceBox +
                ", faceBoxes=" + faceBoxes +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", commited=" + commited +
                '}';
    }
}

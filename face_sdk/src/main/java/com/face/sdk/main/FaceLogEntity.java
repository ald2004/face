package com.face.sdk.main;

import java.io.Serializable;
import java.sql.Timestamp;

public class FaceLogEntity implements Serializable {
    private Long id;
    private Integer status;
    private String logImg;
    // ================人脸库字段==================
    private Long faceUserId;
    private String name;
    private Integer faceUserStatus;
    private String idCard;
    private String phone;
    private String facePhoto;
    private String photo;
    // ================人脸库字段==================

    // ================摄像头字段==================
    private Long cameraId;
    private String number;
    private String username;
    private String password;
    private String region;
    private String ip;
    private Integer port;
    private Integer cameraStatus;
    // ================摄像头字段==================

    // ================人脸框字段==================
    public int x;
    public int y;
    public int width;
    public int height;
    public String points;
    // 水平角度
    public float angle;
    // ================人脸框字段==================


    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "FaceLogEntity{" +
                "id=" + id +
                ", status=" + status +
                ", logImg='" + logImg + '\'' +
                ", faceUserId=" + faceUserId +
                ", name='" + name + '\'' +
                ", faceUserStatus=" + faceUserStatus +
                ", idCard='" + idCard + '\'' +
                ", phone='" + phone + '\'' +
                ", facePhoto='" + facePhoto + '\'' +
                ", photo='" + photo + '\'' +
                ", cameraId=" + cameraId +
                ", number='" + number + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", region='" + region + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", cameraStatus=" + cameraStatus +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", points='" + points + '\'' +
                '}';
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public FaceLogEntity(Long id, Integer status, String logImg, Long faceUserId, String name, Integer faceUserStatus, String idCard, String phone, String facePhoto, String photo, Long cameraId, String number, String username, String password, String region, String ip, Integer port, Integer cameraStatus, int x, int y, int width, int height, String points) {
        this.id = id;
        this.status = status;
        this.logImg = logImg;
        this.faceUserId = faceUserId;
        this.name = name;
        this.faceUserStatus = faceUserStatus;
        this.idCard = idCard;
        this.phone = phone;
        this.facePhoto = facePhoto;
        this.photo = photo;
        this.cameraId = cameraId;
        this.number = number;
        this.username = username;
        this.password = password;
        this.region = region;
        this.ip = ip;
        this.port = port;
        this.cameraStatus = cameraStatus;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.points = points;
    }

    public Long getFaceUserId() {
        return faceUserId;
    }

    public void setFaceUserId(Long faceUserId) {
        this.faceUserId = faceUserId;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    public FaceLogEntity(Long id, Integer status, String logImg, Long faceUserId, String name, Integer faceUserStatus, String idCard, String phone, String facePhoto, String photo, Long cameraId, String number, String username, String password, String region, String ip, Integer port, Integer cameraStatus) {
        this.id = id;
        this.status = status;
        this.logImg = logImg;
        this.faceUserId = faceUserId;
        this.name = name;
        this.faceUserStatus = faceUserStatus;
        this.idCard = idCard;
        this.phone = phone;
        this.facePhoto = facePhoto;
        this.photo = photo;
        this.cameraId = cameraId;
        this.number = number;
        this.username = username;
        this.password = password;
        this.region = region;
        this.ip = ip;
        this.port = port;
        this.cameraStatus = cameraStatus;
    }

    public String getLogImg() {
        return logImg;
    }

    public void setLogImg(String logImg) {
        this.logImg = logImg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFaceUserStatus() {
        return faceUserStatus;
    }

    public void setFaceUserStatus(Integer faceUserStatus) {
        this.faceUserStatus = faceUserStatus;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFacePhoto() {
        return facePhoto;
    }

    public void setFacePhoto(String facePhoto) {
        this.facePhoto = facePhoto;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getCameraStatus() {
        return cameraStatus;
    }

    public void setCameraStatus(Integer cameraStatus) {
        this.cameraStatus = cameraStatus;
    }

    public FaceLogEntity() {
    }

    public FaceLogEntity(Long id, Integer status, String name, Integer faceUserStatus, String idCard, String phone, String facePhoto, String photo, String number, String username, String password, String region, String ip, Integer port, Integer cameraStatus) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.faceUserStatus = faceUserStatus;
        this.idCard = idCard;
        this.phone = phone;
        this.facePhoto = facePhoto;
        this.photo = photo;
        this.number = number;
        this.username = username;
        this.password = password;
        this.region = region;
        this.ip = ip;
        this.port = port;
        this.cameraStatus = cameraStatus;
    }
}

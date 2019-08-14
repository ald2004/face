package com.face.sdk.main;


import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 *
 */
public class FaceUserEntity implements Serializable {
    private long id;
    private int status;
    private String name;
    private float[] embedding;
    private String photo;
    private String facePhoto;
    private String idCard;
    private String phone;
    private String des;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getFacePhoto() {
        return facePhoto;
    }

    public void setFacePhoto(String facePhoto) {
        this.facePhoto = facePhoto;
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

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    @Override
    public String toString() {
        return "FaceUserEntity{" +
                "id=" + id +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", embedding=" + Arrays.toString(embedding) +
                ", photo='" + photo + '\'' +
                ", facePhoto='" + facePhoto + '\'' +
                ", idCard='" + idCard + '\'' +
                ", phone='" + phone + '\'' +
                ", des='" + des + '\'' +
                '}';
    }

    public FaceUserEntity(long id, int status, String name, float[] embedding, String photo, String facePhoto, String idCard, String phone, String des) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.embedding = embedding;
        this.photo = photo;
        this.facePhoto = facePhoto;
        this.idCard = idCard;
        this.phone = phone;
        this.des = des;
    }

    public FaceUserEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public FaceUserEntity(long id, int status, String name, float[] embedding) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.embedding = embedding;
    }
}

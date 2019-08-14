package com.face.sdk.jni;

import java.util.Arrays;

public class FACE_BOX {
    public float score;
    public int x;
    public int y;
    public int width;
    public int height;
    public float area;
    public float points[];
    public byte faceImg[];
    public int imgRows;
    public int imgCols;
    public int imgChs;
    public float embedding[];
    // 水平角度
    public float angle;

    //(FIIIIF[F[BIII[FF)V
    public FACE_BOX(float score, int x, int y, int width, int height, float area, float[] points, byte[] faceImg, int imgRows, int imgCols, int imgChs, float[] embedding, float angle) {
        this.score = score;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.area = area;
        this.points = points;
        this.faceImg = faceImg;
        this.imgRows = imgRows;
        this.imgCols = imgCols;
        this.imgChs = imgChs;
        this.embedding = embedding;
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
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

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public float[] getPoints() {
        return points;
    }

    public void setPoints(float[] points) {
        this.points = points;
    }

    public byte[] getFaceImg() {
        return faceImg;
    }

    public void setFaceImg(byte[] faceImg) {
        this.faceImg = faceImg;
    }

    public int getImgRows() {
        return imgRows;
    }

    public void setImgRows(int imgRows) {
        this.imgRows = imgRows;
    }

    public int getImgCols() {
        return imgCols;
    }

    public void setImgCols(int imgCols) {
        this.imgCols = imgCols;
    }

    public int getImgChs() {
        return imgChs;
    }

    public void setImgChs(int imgChs) {
        this.imgChs = imgChs;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }


    @Override
    public String toString() {
        return "FACE_BOX{" +
                "score=" + score +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", area=" + area +
                ", points=" + Arrays.toString(points) +
                ", imgRows=" + imgRows +
                ", imgCols=" + imgCols +
                ", imgChs=" + imgChs +
                ", embedding=" + Arrays.toString(embedding) +
                '}';
    }


    public FACE_BOX() {
    }

}
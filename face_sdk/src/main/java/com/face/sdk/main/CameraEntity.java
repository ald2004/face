package com.face.sdk.main;

import java.io.Serializable;

public class CameraEntity implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String ip;
    private Integer port;
    private Integer status;
    private String number;
    private String region;

    @Override
    public String toString() {
        return "CameraEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", status=" + status +
                ", number='" + number + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CameraEntity(Long id, String username, String password, String ip, Integer port, Integer status, String number) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.status = status;
        this.number = number;
    }

    public CameraEntity() {
    }

    public CameraEntity(Long id, String username, String password, String ip, Integer port, Integer status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

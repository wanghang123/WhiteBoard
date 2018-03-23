package com.protruly.whiteboard.entity;

import java.io.Serializable;

/**
 * Desc:升级信息
 *
 * @author wang
 * @time 2017/4/11.
 */
public class UpdateInfo implements Serializable{
    private String name;
    private String version;
    private String size;
    private String download;
    private String time;
    private String message;
    private String md5;

    public UpdateInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", size='" + size + '\'' +
                ", download='" + download + '\'' +
                ", time='" + time + '\'' +
                ", message='" + message + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}

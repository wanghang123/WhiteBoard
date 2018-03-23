package com.yinghe.whiteboardlib.bean;

/**
 * Desc: 文件信息
 *
 * @author wang
 * @time 2017/6/14.
 */
public class FileInfo {
    private String md5filename;
    private String uid;
    private int filesize;
    private String requstip;
    private int status;

    private int succsize;
    private String filename;
    private String appid;
    private String errmsg;
    private String suffix;

    private String url;
    private int mode;

    public FileInfo() {
    }

    public String getMd5filename() {
        return md5filename;
    }

    public void setMd5filename(String md5filename) {
        this.md5filename = md5filename;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getFilesize() {
        return filesize;
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public String getRequstip() {
        return requstip;
    }

    public void setRequstip(String requstip) {
        this.requstip = requstip;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSuccsize() {
        return succsize;
    }

    public void setSuccsize(int succsize) {
        this.succsize = succsize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "md5filename='" + md5filename + '\'' +
                ", uid='" + uid + '\'' +
                ", filesize=" + filesize +
                ", requstip='" + requstip + '\'' +
                ", status=" + status +
                ", succsize=" + succsize +
                ", filename='" + filename + '\'' +
                ", appid='" + appid + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", suffix='" + suffix + '\'' +
                ", url='" + url + '\'' +
                ", mode=" + mode +
                '}';
    }
}

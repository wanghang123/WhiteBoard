package com.yinghe.whiteboardlib.bean;

/**
 * Desc:
 *
 * @author Administrator
 * @time 2017/6/20.
 */
public class RespPwdData {
    private String filename;
    private String token;

    public RespPwdData() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "RespPwdData{" +
                "filename='" + filename + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}

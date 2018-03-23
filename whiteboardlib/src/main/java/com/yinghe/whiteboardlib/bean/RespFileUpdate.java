package com.yinghe.whiteboardlib.bean;

import java.util.List;

/**
 * Desc: 上传文件之后的响应
 *
 * @author wang
 * @time 2017/6/14.
 */
public class RespFileUpdate {
    private int ret;
    private int errcode;
    private String msg;

    private List<FileInfo> data;

    public RespFileUpdate() {
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<FileInfo> getData() {
        return data;
    }

    public void setData(List<FileInfo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespFileUpdate{" +
                "ret=" + ret +
                ", errcode=" + errcode +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

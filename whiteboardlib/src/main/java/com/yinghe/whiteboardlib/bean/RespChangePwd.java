package com.yinghe.whiteboardlib.bean;

import java.util.List;

/**
 * Desc: 修改密码的返回
 *
 * @author Administrator
 * @time 2017/6/20.
 */
public class RespChangePwd {
    private int ret;
    private int errcode;
    private String msg;

    private List<RespPwdData> data;

    public RespChangePwd() {
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

    public List<RespPwdData> getData() {
        return data;
    }

    public void setData(List<RespPwdData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RespChangePwd{" +
                "ret=" + ret +
                ", errcode=" + errcode +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

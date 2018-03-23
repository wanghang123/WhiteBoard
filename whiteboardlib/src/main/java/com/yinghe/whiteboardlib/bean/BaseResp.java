package com.yinghe.whiteboardlib.bean;

/**
 * Desc:响应的基类
 *
 * @author Administrator
 * @time 2017/6/20.
 */
public class BaseResp {
    protected int ret;
    protected int errcode;
    protected String msg;

    public BaseResp() {
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
}

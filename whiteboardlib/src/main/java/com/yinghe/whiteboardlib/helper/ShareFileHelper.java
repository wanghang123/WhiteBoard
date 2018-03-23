package com.yinghe.whiteboardlib.helper;

import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.yinghe.whiteboardlib.bean.RespChangePwd;
import com.yinghe.whiteboardlib.bean.RespFileUpdate;
import com.yinghe.whiteboardlib.callback.BeanCallBack;
import com.yinghe.whiteboardlib.callback.HttpCallBack;
import com.yinghe.whiteboardlib.utils.CommConsts;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Desc:文件分享帮助类
 *
 * @author Administrator
 * @time 2017/6/20.
 */
public class ShareFileHelper {
    /**
     * 上传文件到服务器
     */
    public void uploadFileToServer(String localFilePath, String encrypt, boolean isEncrypt, HttpCallBack<RespFileUpdate> httpCallBack){
        LogUtils.d("文件上传");
        File file = new File(localFilePath);
        // 回调
        BeanCallBack<RespFileUpdate> beanCallBack = new BeanCallBack<RespFileUpdate>() {
            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtils.d("上传文件失败!");
                e.printStackTrace();
                OkHttpUtils.getInstance().cancelTag("postFile");

                // 生成二维码失败
                httpCallBack.onError();
            }

            @Override
            public void onResponse(RespFileUpdate response, int id) {
                OkHttpUtils.getInstance().cancelTag("postFile");

                LogUtils.d("上传文件成功!" + response);
                httpCallBack.onResponse(response);
            }
        };

        // 上传文件
        Map<String, String> params = new HashMap<>();
        params.put("appid","storePDF");// 业务ID
        params.put("uid","uid123");// 用户ID或设备ID

        // 有加密字段
        LogUtils.d("isEncrypt->" + isEncrypt +"   encrypt:" + encrypt);
        if (isEncrypt && !TextUtils.isEmpty(encrypt)){
            params.put("token", encrypt);// 认证字符串
        } else {
            params.put("token", "");// 认证字符串
        }

        OkHttpUtils.post()
                .tag("postFile")
                .url(CommConsts.UPDLOAD_URL)
                .params(params)
                .addFile("mFile", file.getName(), file)
                .addHeader("Accept-Encoding", "utf-8")
                .build()
                .execute(beanCallBack);
    }

    /**
     * 修改文件密码
     */
    public void changeFilePwd(String md5filename, String encrypt){
        LogUtils.d("修改文件密码");
        if (TextUtils.isEmpty(md5filename)){
            LogUtils.d("文件不存在");
            return;
        }

        final String callTag = "PWD_URL";
        // 回调
        BeanCallBack<RespChangePwd> beanCallBack = new BeanCallBack<RespChangePwd>() {
            @Override
            public void onError(Call call, Exception e, int id) {
                OkHttpUtils.getInstance().cancelTag(callTag);
                e.printStackTrace();
            }

            @Override
            public void onResponse(RespChangePwd response, int id) {
                OkHttpUtils.getInstance().cancelTag(callTag);
                LogUtils.d("response->" + response);
            }
        };

        // 上传文件
        Map<String, String> params = new HashMap<>();
        params.put("appid", "storePDF");// 业务ID
        params.put("uid", "uid123");// 用户ID或设备ID
        if (TextUtils.isEmpty(encrypt)){
            encrypt = "";
        }
        params.put("token", encrypt);// 认证字符串 有加密字段
        LogUtils.d("encrypt:" + encrypt);

        String url = CommConsts.PWD_URL + "/" + md5filename;
        OkHttpUtils.post()
                .tag(callTag)
                .url(url)
                .params(params)
                .addHeader("Accept-Encoding", "utf-8")
                .build()
                .execute(beanCallBack);
    }
}

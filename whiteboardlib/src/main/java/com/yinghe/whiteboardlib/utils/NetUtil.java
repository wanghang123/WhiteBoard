package com.yinghe.whiteboardlib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 当前网络状态
 * Created by wlb on 2017-02-08 0008.
 */
public class NetUtil {
    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    /**
     * 有线网络
     */
    public static final int TYPE_ETHERNET = 9;

    public static int getNetWorkState(Context context) {

        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            int type  = activeNetworkInfo.getType();
            if (type == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (type == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }  else if (type == (ConnectivityManager.TYPE_ETHERNET)) {
                return TYPE_ETHERNET;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }
}

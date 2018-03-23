package com.yinghe.whiteboardlib.utils;

/**
 * Desc:View工具类
 *
 * @author wang
 * @time 2017/7/6.
 */
public class ViewUtils {
    private static long lastClickTime;

    /**
     * 防止短时间内重复点击
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 800) {
            return true;
        }

        lastClickTime = time;
        return false;
    }

    /**
     * 防止短时间内重复点击
     * @param interval
     * @return
     */
    public static boolean isFastDoubleClick(final int interval) {
        int tmpInterval = interval;
        if (interval < 100){
            tmpInterval = 800;
        }

        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < tmpInterval) {
            return true;
        }

        lastClickTime = time;
        return false;
    }
}

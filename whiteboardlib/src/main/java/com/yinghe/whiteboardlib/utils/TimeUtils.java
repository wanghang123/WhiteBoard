package com.yinghe.whiteboardlib.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间处理工具
 * Created by Nereo on 2015/4/8.
 */
public class TimeUtils {
    public final static String DATE_PATTERN_DEFAULT = "yyyy-MM-dd";
    public final static String DATE_PATTERN_FILE_NAME = "yyyy-MM-dd_HHmmss";
    public final static String DATE_PATTERN_MMDD = "MMdd";

    public static String timeFormat(long timeMillis, String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static String formatPhotoDate(long time){
        return timeFormat(time, DATE_PATTERN_DEFAULT);
    }

    public static String formatPhotoDate(String path){
        File file = new File(path);
        if(file.exists()){
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01";
    }

    /**
     * 获得时间字符串
     * @return
     */
    public static String getNowTime(String pattern) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        return sDateFormat.format(new Date());
    }

    /**
     * 获得时间字符串
     * @return
     */
    public static String getNowTimeString() {
        return getNowTime(DATE_PATTERN_FILE_NAME);
    }
}

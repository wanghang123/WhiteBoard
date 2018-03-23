package com.yinghe.whiteboardlib.utils;

import java.util.UUID;

/**
 * Desc:生成随机ID
 *
 * @author wang
 * @time 2017/7/18.
 */
public class UUIDGenerator {
    /**
     * 获得一个UUID
     * @return String UUID
     */
    public static String getUUID(){
        String s = UUID.randomUUID().toString();
        //去掉“-”符号
        return s.replace("-","");
    }
}

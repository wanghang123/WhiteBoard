package com.yinghe.whiteboardlib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.LruCache;

import java.io.File;


/**
 * Desc:图片缓存
 *
 * @author wang
 * @time 2017/7/18.
 */
public class BitmapCache {
    // LRUCahce 池子
    private static LruCache<String, Bitmap> mCache;

    // 二级缓存
    private ACache mACache;

    private String bitmapCacheDir;// 图片缓存目录

    public BitmapCache(Context context){
        // 创建内存缓存
        // 一级缓存初始化
        long memory = Runtime.getRuntime().maxMemory();
        int maxSize = (int) (memory / 8);
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        // 创建硬盘缓存
        String cacheDir = getDiskCacheDir(context);

        // 缓存文件夹
        bitmapCacheDir = cacheDir + File.separator + DrawConsts.BITMAP_CACHE_DIR;

        // 创建硬盘缓存
        mACache = ACache.getByDir(bitmapCacheDir);
    }

    private String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 获得缓存图片
     * @param key
     * @return
     */
    public Bitmap getBitmapFromCache(final String key){
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(key)){
            return bitmap;
        }

        bitmap = mCache.get(key);
        if (bitmap == null || bitmap.isRecycled()){
            bitmap = mACache.getAsBitmap(key);
        }

        return bitmap;
    }

    /**
     * 缓存图片
     *
     * @param key
     * @param value
     */
    public void putBitmapToCache(final String key, final Bitmap value){
        // 缓存到内存中
        mCache.put(key, value);

        // 缓存到硬盘中
        mACache.put(key, value);
    }

    /**
     * 清除图片缓存
     */
    public void clearDiskCache(){
        File dir = new File(bitmapCacheDir);
        FileUtils.deleteFile(dir);
    }
}

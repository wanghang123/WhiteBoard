package com.yinghe.whiteboardlib.bean;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChiEr on 16/6/16.
 */
public class SketchData {
    public List<PhotoRecord> photoRecordList;// 图片记录
    public List<StrokeRecord> strokeRecordList;// 画笔记录
    public List<StrokeRecord> strokeRedoList;// 重做画笔记录

    public Bitmap thumbnailBM;//缩略图文件
    public Bitmap backgroundBM;// 背景图

    public int strokeType;
    public int editMode;

    public SketchData() {
        strokeRecordList = new ArrayList<>();
        photoRecordList = new ArrayList<>();
        strokeRedoList = new ArrayList<>();
        backgroundBM = null;
        thumbnailBM = null;
    }

}

package com.yinghe.whiteboardlib.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.SparseArray;

/**
 * 画笔记录
 */
public class StrokeRecord {
    public int type;//记录类型
    public Paint paint;//笔类
    public Path path;//画笔路径数据
    public SparseArray<Path> pathList;//多点触控时的画笔路径数据

    public RectF rect; //圆、矩形区域
    public Matrix matrix;//图形变化矩阵
    public Bitmap rectBitmap;// 拾取的图片
    public String bitmapID;// 图片的ID
    public int actionMode = 0;// 操作模式

    public float scaleMax = 3;

    public int areaEraserSize;// 面积擦除的尺寸

    // 文字绘制
    public String text;//文字
    public TextPaint textPaint;//笔类
    public int textOffX;
    public int textOffY;
    public int textWidth;//文字位置

    public boolean hasDraw = false;

    public StrokeRecord(int type) {
        this.type = type;
    }
}
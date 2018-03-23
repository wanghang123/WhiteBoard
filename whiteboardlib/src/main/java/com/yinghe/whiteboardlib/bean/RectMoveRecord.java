package com.yinghe.whiteboardlib.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Desc:画笔迁移的记录
 *
 * @author wang
 * @time 2017/7/5.
 */
public class RectMoveRecord {
    public Matrix matrix;//图形
    public RectF rectSrc = new RectF();
}

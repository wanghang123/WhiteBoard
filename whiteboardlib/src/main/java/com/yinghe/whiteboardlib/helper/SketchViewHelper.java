package com.yinghe.whiteboardlib.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

import com.yinghe.whiteboardlib.bean.PhotoRecord;
import com.yinghe.whiteboardlib.utils.BitmapUtils;

import java.io.File;

import static com.yinghe.whiteboardlib.utils.CommConsts.SIMPLE_SCALE;

/**
 * SketchView帮助类
 *
 * @author wang
 * @time on 2017/3/23.
 */
public class SketchViewHelper {
    private Context mContext;

    public SketchViewHelper(Context context) {
        mContext = context;
    }

    /**
     * 初始化图片记录
     *
     * @param view
     * @param bitmap
     * @return
     */
    public PhotoRecord initPhotoRecord(View view, Bitmap bitmap) {
        PhotoRecord newRecord = new PhotoRecord();
        newRecord.bitmap = bitmap;
        newRecord.photoRectSrc = new RectF(0, 0, newRecord.bitmap.getWidth(), newRecord.bitmap.getHeight());
        newRecord.scaleMax = getMaxScale(view, newRecord.photoRectSrc);//放大倍数
        newRecord.scaleMin = getMinScale(view, newRecord.photoRectSrc);//缩小倍数
        newRecord.matrix = new Matrix();
        newRecord.matrix.postTranslate(view.getWidth() / 2 - bitmap.getWidth() / 2, view.getHeight() / 2 - bitmap.getHeight() / 2);
        return newRecord;
    }

    /**
     * 获得最大缩放值
     *
     * @param view
     * @param photoSrc
     * @return
     */
    private float getMaxScale(View view, RectF photoSrc) {
        return Math.max(view.getWidth(), view.getHeight()) / Math.max(photoSrc.width(), photoSrc.height());
//        SCALE_MIN = SCALE_MAX / 5;
    }

    /**
     * 获得最大缩放值
     *
     * @param view
     * @param photoSrc
     * @return
     */
    private float getMinScale(View view, RectF photoSrc) {
        return Math.min(view.getWidth() / 5, view.getHeight() / 5) / Math.min(photoSrc.width(), photoSrc.height());
//        SCALE_MIN = SCALE_MAX / 5;
    }

    /**
     * 从SD卡中获得图片
     *
     * @param path
     * @return
     */
    public Bitmap getSDCardPhoto(String path) {
        File file = new File(path);
        if (file.exists()) {
            return BitmapUtils.decodeSampleBitMapFromFile(mContext, path, SIMPLE_SCALE);
        } else {
            return null;
        }
    }

    /**
     * 从assets中获得图片
     *
     * @param path
     * @return
     */
    public Bitmap getAssetsPhoto(String path) {
        return BitmapUtils.getBitmapFromAssets(mContext, path);
    }

    /**
     * 从路径中获得图片
     *
     * @param path
     * @return
     */
    public Bitmap getSampleBitMap(String path) {
        Bitmap sampleBM = null;
        if (path.contains("/mnt/") || path.contains(Environment.getExternalStorageDirectory().toString())) {
            sampleBM = getSDCardPhoto(path);
        } else {
            sampleBM = getAssetsPhoto(path);
        }

        return sampleBM;
    }

    /**
     * 判断点在编辑区域内
     *
     * @param record
     * @param downPoint
     * @return
     */
    public boolean isPointInEditRect(PhotoRecord record, float[] downPoint) {
        if (record != null) {
            float[] invertPoint = new float[2];
            Matrix invertMatrix = new Matrix();
            record.matrix.invert(invertMatrix);
            invertMatrix.mapPoints(invertPoint, downPoint);
            return record.photoRectSrc.contains(invertPoint[0], invertPoint[1]);
        }
        return false;
    }

    /**
     * 判断点在矩形区域内
     *
     * @param rectF
     * @param matrix
     * @param downPoint
     * @return
     */
    public boolean isPointInRect(RectF rectF, Matrix matrix, float[] downPoint) {
        if (rectF != null) {
            if (matrix == null){
                return rectF.contains(downPoint[0], downPoint[1]);
            } else {
                float[] invertPoint = new float[2];
                Matrix invertMatrix = new Matrix();
                matrix.invert(invertMatrix);
                invertMatrix.mapPoints(invertPoint, downPoint);
                return rectF.contains(invertPoint[0], invertPoint[1]);
            }
        }

        return false;
    }

    /**
     * 计算两个触摸点的中点坐标
     */
    public void calMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 计算旋转角度
     *
     * @param event
     * @return 角度值
     */
    public float calRotation(MotionEvent event) {
        double deltaX = (event.getX(0) - event.getX(1));
        double deltaY = (event.getY(0) - event.getY(1));
        double radius = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radius);
    }
}

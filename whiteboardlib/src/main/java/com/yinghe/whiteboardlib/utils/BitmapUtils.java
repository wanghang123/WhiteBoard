package com.yinghe.whiteboardlib.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.WindowManager;

import com.yinghe.whiteboardlib.bean.StrokeRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static com.yinghe.whiteboardlib.utils.CommConsts.SIMPLE_SCALE;

/**
 * Created by TangentLu on 2015/8/19.
 */
public class BitmapUtils {


    public static boolean isLandScreen(Context context) {
        int ori =context.getResources().getConfiguration().orientation;//获取屏幕方向
        return ori == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static Bitmap decodeSampleBitMapFromFile(Context context, String filePath, float sampleScale) {
        //先得到bitmap的高宽
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        //再用屏幕一半高宽、缩小后的高宽对比，取小值进行缩放
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int reqWidth = wm.getDefaultDisplay().getWidth();
        int reqHeight = wm.getDefaultDisplay().getWidth();
        int scaleWidth = (int) (options.outWidth * sampleScale);
        int scaleHeight = (int) (options.outHeight * sampleScale);
        reqWidth = Math.min(reqWidth, scaleWidth);
        reqHeight = Math.min(reqHeight, scaleHeight);
        options = sampleBitmapOptions(context, options, reqWidth, reqHeight);
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        Log.e("xxx", bm.getByteCount() + "");
        return bm;
    }
    public static Bitmap decodeSampleBitMapFromResource(Context context, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        options = sampleBitmapOptions(context, options, reqWidth, reqHeight);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resId, options);
        Log.e("xxx", bm.getByteCount() + "");
        return bm;
    }


    public static Bitmap createBitmapThumbnail(Bitmap bitMap, boolean needRecycle, int newHeight, int newWidth) {
        if (bitMap == null){
            return null;
        }

        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 计算缩放比例
        float scale = Math.min((float) newWidth / width, (float) (newHeight) / height);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, true);
        if (needRecycle) {
            bitMap.recycle();
            bitMap = null;
        }
        return newBitMap;
    }

    public static BitmapFactory.Options sampleBitmapOptions(
            Context context, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int targetDensity = context.getResources().getDisplayMetrics().densityDpi;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        double xSScale = ((double) options.outWidth) / ((double) reqWidth);
        double ySScale = ((double) options.outHeight) / ((double) reqHeight);

        double startScale = xSScale > ySScale ? xSScale : ySScale;

        options.inScaled = true;
        options.inDensity = (int) (targetDensity * startScale);
        options.inTargetDensity = targetDensity;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return options;
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap getBitmapFromAssets(Context context,String path){
        InputStream open = null;
        Bitmap bitmap = null;
        try {
            String temp =  path;
            open = context.getAssets().open(temp);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options = sampleBitmapOptions(context, options, 10, 10);
            bitmap = BitmapFactory.decodeStream(open, null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从路径中获得图片
     *
     * @param mContext
     * @param path
     * @return
     */
    public static Bitmap getSampleBitMap(Context mContext,String path) {
        Bitmap sampleBM = null;
        File file = new File(path);
        if (file.exists()){
            sampleBM = BitmapUtils.decodeSampleBitMapFromFile(mContext, path, SIMPLE_SCALE);
        } else {
            sampleBM = BitmapUtils.getBitmapFromAssets(mContext, path);
        }
//        if (path.contains("/mnt/") || path.contains(Environment.getExternalStorageDirectory().toString())) {
//            File file = new File(path);
//            if (file.exists()){
//                sampleBM = BitmapUtils.decodeSampleBitMapFromFile(mContext, path, SIMPLE_SCALE);
//            }
//        } else {
//            sampleBM = BitmapUtils.getBitmapFromAssets(mContext, path);
//        }

        return sampleBM;
    }


    /**
     * 通过颜色创建位图
     * @param colorARGB should like 0x8800ff00
     * @return
     */
    public static Bitmap createBitmapFromARGB(int width, int height, int colorARGB) {
        int[] argb = new int[width * height];

        for (int i = 0; i < argb.length; i++) {
            argb[i] = colorARGB;
        }

        return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     *
     * @param colorARGB should like 0x8800ff00
     * @return
     */
    public static Bitmap createBitmapFromARGB2(int width, int height, int colorARGB) {
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(colorARGB);

        return mBitmap;
    }

    /**
     * 保存bitmap图片到sdcard中
     *
     * @param filePath
     * @param newBM
     * @param compress
     */
    public static boolean saveBitmapToSdcard(String filePath, Bitmap newBM, int compress){
        FileOutputStream out = null;
        try {
            File f = new File(filePath);
            if (!f.getParentFile().exists()){
                f.getParentFile().mkdirs();
            }

            if (!f.exists()) {
                f.createNewFile();
            } else {
                f.delete();
            }
            out = new FileOutputStream(f);

            if (compress >= 1 && compress <= 100) {
                newBM.compress(Bitmap.CompressFormat.JPEG, compress, out);
            } else {
                newBM.compress(Bitmap.CompressFormat.JPEG, 80, out);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            newBM.recycle();
            newBM = null;
            if (out != null){
                try {
                    out.close();
                } catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 拾取图片
     *
     * @param source
     * @param curStrokeRecord
     * @param path
     * @return
     */
    public static Bitmap pickupBitmapByPathRect(Bitmap source, StrokeRecord curStrokeRecord, Path path){
        // 获得移动矩形区域
        int x = (int)curStrokeRecord.rect.left;
        int y = (int)curStrokeRecord.rect.top;
        int width = (int)(curStrokeRecord.rect.right - curStrokeRecord.rect.left);
        int height = (int)(curStrokeRecord.rect.bottom - curStrokeRecord.rect.top);

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setColor(Color.argb(255, 0, 255, 255));
        strokePaint.setStyle(Paint.Style.FILL);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(20);

        // 获得dst图片
        // 将path绘制到图片中
        Path path1 = new Path(path);
        Bitmap pathBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas pathCanvas = new Canvas(pathBitmap);
        pathCanvas.drawPath(path1, strokePaint);

        Bitmap dstBitmap = Bitmap.createBitmap(pathBitmap, x, y, width, height, null, false);

        // 获得拾取的图片
        Bitmap srcBitmap = Bitmap.createBitmap(source, x, y, width, height, null, false);

        // 只取SRC与DST相交的SRC部分
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.save(Canvas.ALL_SAVE_FLAG);

        // 绘制DST图片
        canvas.drawBitmap(dstBitmap, 0, 0, null);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 绘制SRC图片
        canvas.drawBitmap(srcBitmap, 0, 0, paint);

        strokePaint.setXfermode(null);
        canvas.restore();

        try{
            if (!pathBitmap.isRecycled()){
                pathBitmap.recycle();
                pathBitmap = null;
            }

            if (!dstBitmap.isRecycled()){
                dstBitmap.recycle();
                dstBitmap = null;
            }

            if (!srcBitmap.isRecycled()){
                srcBitmap.recycle();
                srcBitmap = null;
            }

            paint = null;
            strokePaint = null;
        } catch (Exception e){
            e.printStackTrace();
        }

        return out;
    }

    /**
     * 释放图片
     * @param bitmap
     */
    public static void releaseBitmap(Bitmap bitmap){
        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

}

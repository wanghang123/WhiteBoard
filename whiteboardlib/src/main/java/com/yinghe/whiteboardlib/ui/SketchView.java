/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.yinghe.whiteboardlib.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.yinghe.whiteboardlib.R;
import com.yinghe.whiteboardlib.bean.PhotoRecord;
import com.yinghe.whiteboardlib.bean.SketchData;
import com.yinghe.whiteboardlib.bean.StrokeRecord;
import com.yinghe.whiteboardlib.helper.SketchViewHelper;
import com.yinghe.whiteboardlib.listener.OnDrawChangedListener;
import com.yinghe.whiteboardlib.listener.OnPhotoRecordChangeListener;
import com.yinghe.whiteboardlib.listener.TextWindowCallback;
import com.yinghe.whiteboardlib.utils.BitmapCache;
import com.yinghe.whiteboardlib.utils.BitmapUtils;
import com.yinghe.whiteboardlib.utils.DrawConsts;
import com.yinghe.whiteboardlib.utils.MathUtils;
import com.yinghe.whiteboardlib.utils.ScreenUtils;
import com.yinghe.whiteboardlib.utils.UUIDGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_DRAG;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_MOVE_OVER;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_NONE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_PICKUP;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_ROTATE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ACTION_SCALE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.BOARD_STROKE_WIDTH;
import static com.yinghe.whiteboardlib.utils.DrawConsts.DEFAULT_ERASER_SIZE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.DEFAULT_STROKE_SIZE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_MOVE_RECT;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_PHOTO;
import static com.yinghe.whiteboardlib.utils.DrawConsts.EDIT_STROKE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.ERASER_STROKE_WIDTH;
import static com.yinghe.whiteboardlib.utils.DrawConsts.MAX_TOUCH_POINTS;
import static com.yinghe.whiteboardlib.utils.DrawConsts.MIN_LEN;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_CIRCLE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_DRAW;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_ERASER;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_ERASER_CIRCLE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_ERASER_RECT;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_LINE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_MOVE_RECORD;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_PHOTO;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_RECTANGLE;
import static com.yinghe.whiteboardlib.utils.DrawConsts.STROKE_TYPE_TEXT;
import static com.yinghe.whiteboardlib.utils.MathUtils.spacing;

/**
 * 画板
 *
 * @author wang
 * @date 2017/03/10
 */
public class SketchView extends View{
    private final String TAG = getClass().getSimpleName();

    private TextWindowCallback textWindowCallback;

    private float strokeSize = DEFAULT_STROKE_SIZE;
    private int strokeRealColor = Color.RED;//画笔实际颜色
    private int selectPickupColor = Color.WHITE;//选择拾取画笔的颜色
    private int strokeColor = Color.RED;//画笔颜色
    private int strokeAlpha = 255;//画笔透明度
    private float eraserSize = DEFAULT_ERASER_SIZE;

    /**
     * 四个边角图片
     */
    Bitmap mirrorMarkBM = BitmapFactory.decodeResource(getResources(), R.drawable.mark_copy);
    Bitmap deleteMarkBM = BitmapFactory.decodeResource(getResources(), R.drawable.mark_delete);
    Bitmap rotateMarkBM = BitmapFactory.decodeResource(getResources(), R.drawable.mark_rotate);
    Bitmap resetMarkBM = BitmapFactory.decodeResource(getResources(), R.drawable.mark_reset);
    Bitmap stampMarkBM = BitmapFactory.decodeResource(getResources(), R.drawable.mark_stamp);
    /**
     * 四个边角区域
     */
    RectF markerCopyRect = new RectF(0, 0, mirrorMarkBM.getWidth(), mirrorMarkBM.getHeight());//镜像标记边界
    RectF markerDeleteRect = new RectF(0, 0, deleteMarkBM.getWidth(), deleteMarkBM.getHeight());//删除标记边界
    RectF markerRotateRect = new RectF(0, 0, rotateMarkBM.getWidth(), rotateMarkBM.getHeight());//旋转标记边界
    RectF markerResetRect = new RectF(0, 0, resetMarkBM.getWidth(), resetMarkBM.getHeight());//旋转标记边界
    RectF markerStampRect = new RectF(0, 0, stampMarkBM.getWidth(), stampMarkBM.getHeight());//融合标记边界

    private Bitmap mBitmap;// 临时图片
    private Canvas mCanvas;// 临时画布

    // 增量画布，用来绘制移动过程中的操作步骤
    private Bitmap dBitmap;// 增量图片
    private Canvas dCanvas;// 增量画布

    private Bitmap photoBitmap;// 保存图片记录的图片
    private Canvas photoCanvas;// 保存图片的画布

    private Paint strokePaint;// 画笔
    private Paint eraserPaint;// 橡皮擦的画笔
    private Paint mBitmapPaint;// 画布的画笔
    private Paint mergePaint;// 图片合成的画笔
    private Paint clearPaint;// 清除画布画笔

    private Paint selectedPaint;// 用来选中的的路径画笔
    private Paint eraserIconPaint;// 橡皮擦跟随的图标

    Paint boardPaint;
    private PaintFlagsDrawFilter pfd;
    private PorterDuffXfermode mergeXfermode;

    private PointF[] downPointFs, prePointFs, curPointFs;// 记录点击的点坐标数组
    private PointF downPointF, prePointF, curPointF;// 记录点击的点坐标数组
    private PointF mid;// 中点对象
    private float saveRotate = 0F;// 保存了的角度值
    float oldDist = 1f;

    private int mWidth, mHeight;

    private Context mContext;
    private SketchViewHelper mHelper;// 白板帮助类
    private BitmapCache mBitmapCache;// 图片缓存工具类

    SketchData curSketchData;// 白板记录数据

    StrokeRecord curStrokeRecord;// 画笔数据记录
    PhotoRecord curPhotoRecord;

    private boolean isUndoOrRedo = false;// 进行撤销或者重做操作

    int actionMode;// 操作模式
    private int editMode = EDIT_STROKE;// 编辑模式
    private int lastEditMode = EDIT_STROKE;// 上一次的编辑模式

    public boolean isTouch = false;// 是否开始触控事件
    private boolean isHasPhotoLayer = false;// 是否存在图片层

    private static float SCALE_MAX = 4.0f;
    private static float SCALE_MIN = 0.2f;
    private static float SCALE_MIN_LEN;

    private List<Integer> curPointerIdArr;// 记录当前多点触控的手指索引个数

    private HandlerThread mDataThread;
    private Handler mDataHandler;//

    // 记录橡皮擦的上一次的中点位置
    private float lastMidX;
    private float lastMidY;

    /**
     * 缩放手势
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private int strokeType = STROKE_TYPE_DRAW;
    private int lastStrokeType = STROKE_TYPE_DRAW;

    private final static Lock LOCK = new ReentrantLock();

    private OnPhotoRecordChangeListener onPhotoRecordChangeListener;
    private OnDrawChangedListener onDrawChangedListener;

    public SketchView(Context context, AttributeSet attr) {
        super(context, attr);
//        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 关闭硬件加速

        this.mContext = context;
        initParams(context);

        this.setFocusableInTouchMode(true);
        this.setFocusable(true);
        this.setKeepScreenOn(true);
        if (isFocusable()) {
            mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    onScaleAction(detector);
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                }
            });
        }
        invalidate();
    }

    /**
     * 初始化参数
     *
     * @param context
     */
    private void initParams(Context context) {
        // 初始化画笔
        initPaint();

        mid = new PointF();

        // 初始化点
        initTouchParams();

        SCALE_MIN_LEN = ScreenUtils.dip2px(context, MIN_LEN);

        // 初始化工具类
        mHelper = new SketchViewHelper(context);
        mBitmapCache = new BitmapCache(context);

        mDataThread = new HandlerThread("mDataThread-message");
        mDataThread.start();
        mDataHandler = new DataHandler(this, mDataThread.getLooper());

    }

    /**
     * 初始化点击的参数
     */
    private void initTouchParams() {
        // 初始化点
        initPointArr();

        // 初始化触控索引
        initPointerIndexArr();
    }

    /**
     * 初始化触控索引
     */
    private void initPointerIndexArr(){
        if(curPointerIdArr == null){
            curPointerIdArr = new ArrayList<>();
        }

        curPointerIdArr.clear();
    }

    /**
     * 初始化点
     */
    private void initPointArr(){
        downPointF = new PointF();
        prePointF = new PointF();
        curPointF = new PointF();

        if (downPointFs == null){
            downPointFs = new PointF[MAX_TOUCH_POINTS];
        }
        if (curPointFs == null){
            curPointFs = new PointF[MAX_TOUCH_POINTS];
        }
        if (prePointFs == null){
            prePointFs = new PointF[MAX_TOUCH_POINTS];
        }

        for(int i = 0; i < MAX_TOUCH_POINTS; i++){
            curPointFs[i] = new PointF();
            downPointFs[i] = new PointF();
            prePointFs[i] = new PointF();
        }
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        // 清除画布画笔
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // 图片画笔
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        // 图片合成画笔
        mergePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mergeXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

        // 普通绘制画笔设置
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setColor(strokeRealColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(strokeSize);

        // 普通绘制画笔设置
        eraserIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        eraserIconPaint.setAntiAlias(true);
        eraserIconPaint.setDither(true);
        eraserIconPaint.setColor(Color.WHITE);
        eraserIconPaint.setStyle(Paint.Style.FILL);
        eraserIconPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserIconPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserIconPaint.setStrokeWidth(eraserSize);

        //橡皮擦
        eraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        eraserPaint.setAlpha(0);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setStrokeWidth(ScreenUtils.dip2px(mContext, ERASER_STROKE_WIDTH));

        //圈选橡皮擦的画笔
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        selectedPaint.setAntiAlias(true);
        selectedPaint.setDither(true);
        selectedPaint.setColor(selectPickupColor);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeJoin(Paint.Join.ROUND);
        selectedPaint.setStrokeCap(Paint.Cap.ROUND);
        selectedPaint.setStrokeWidth(2);
        PathEffect effects = new DashPathEffect(new float[] { 20.0F, 10.0F, 5.0F, 10.0F}, 1F);
        selectedPaint.setPathEffect(effects);

        // 初始化方框画笔
        boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        boardPaint.setColor(Color.GRAY);
        boardPaint.setAntiAlias(true);
        boardPaint.setDither(true);
        boardPaint.setStrokeWidth(ScreenUtils.dip2px(mContext, BOARD_STROKE_WIDTH));
        boardPaint.setStyle(Paint.Style.STROKE);

        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG |
                Paint.FILTER_BITMAP_FLAG);
    }

    /**
     * 初始化临时画布
     */
    private void initTmpCanvas() {
        // 回收图片
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.isRecycled();
            mBitmap = null;
            mCanvas = null;
        }

        // 创建临时画布
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);
        mCanvas.setDrawFilter(pfd);
    }

    /**
     * 初始化增量画布
     */
    private void initPhotoCanvas() {
        // 回收图片
        if (photoBitmap != null && !photoBitmap.isRecycled()) {
            photoBitmap.isRecycled();
            photoBitmap = null;
            photoCanvas = null;
        }

        // 创建临时画布
        photoBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        photoCanvas = new Canvas(photoBitmap);
        photoCanvas.setDrawFilter(pfd);
    }

    /**
     * 初始化增量画布
     */
    private void initDCanvas() {
        // 回收图片
        if (dBitmap != null && !dBitmap.isRecycled()) {
            dBitmap.isRecycled();
            dBitmap = null;
            dCanvas = null;
        }

        // 创建临时画布
        dBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        dCanvas = new Canvas(dBitmap);
        dCanvas.setDrawFilter(pfd);
    }

    public void setStrokeAlpha(int mAlpha) {
        this.strokeAlpha = mAlpha;
        calculateColor();
        strokePaint.setStrokeWidth(strokeSize);
    }

    public void setStrokeColor(int color) {
        strokeColor = color;
        calculateColor();
        strokePaint.setColor(strokeRealColor);
    }

    private void calculateColor() {
        strokeRealColor = Color.argb(strokeAlpha, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor));
    }

    /**
     * 释放资源
     */
    public void releaseData(){
        // 先判断是否已经回收
        for (PhotoRecord record : curSketchData.photoRecordList) {
            if (record != null && record.bitmap != null && !record.bitmap.isRecycled()) {
                record.bitmap.recycle();
                record.bitmap = null;
            }
        }

        if (curSketchData.backgroundBM != null && !curSketchData.backgroundBM.isRecycled()) {
            // 回收并且置为null
            curSketchData.backgroundBM.recycle();
            curSketchData.backgroundBM = null;
        }
        curSketchData.strokeRecordList.clear();
        curSketchData.photoRecordList.clear();
        curSketchData.strokeRedoList.clear();
        curPhotoRecord = null;
        curStrokeRecord = null;

        // 回收图片
        if (dBitmap != null && !dBitmap.isRecycled()) {
            dBitmap.isRecycled();
            dBitmap = null;
            dCanvas = null;
        }

        // 回收图片
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.isRecycled();
            mBitmap = null;
            mCanvas = null;
        }

        curPointerIdArr.clear();
        curPointerIdArr = null;

        downPointFs = null;
        prePointFs = null;
        curPointFs = null;


        curSketchData.strokeRecordList = null;
        curSketchData.photoRecordList = null;
        curSketchData.strokeRedoList = null;
        curSketchData = null;

        curStrokeRecord = null;
        curPhotoRecord = null;

        //释放资源
        mDataThread.quit();
        mDataHandler.removeCallbacksAndMessages(null);
        mDataHandler = null;

        // 清除图片缓存
        mBitmapCache.clearDiskCache();
        System.gc();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        LogUtils.d("onSizeChanged， w->%s, h->%s", w, h);

        mWidth = w;
        mHeight = h;

        // 初始化临时画布
        initTmpCanvas();
        // 初始化增量画布
        initDCanvas();
        // 初始化图片画布
        initPhotoCanvas();

        drawHistory();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 绘制所有历史的记录
     */
    private void drawHistory(){
        if (curSketchData != null) {

            boolean isDrawBoard = false;
            if (editMode == EDIT_PHOTO){
                isDrawBoard = true;
            }

            // 绘制图片记录
            drawPhotoRecords(photoCanvas, curSketchData, isDrawBoard);

            // 绘制所有的画笔记录:绘制直线、矩形、圆形和文字
            drawStrokeRecords(mCanvas, curSketchData, true);
        }
    }

    /**
     * 对移动点的处理
     *
     * @param event
     */
    private void handleMovePoints(MotionEvent event, PointF[]  pointFs){
        // 设置当前点和触控索引数组
        int pointerId = -1;
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++){
            // 获得当前触控索引
            pointerId = event.getPointerId(i);
            //            LogUtils.d("多点触控，i-->%s, pointerId-->%s" ,i , pointerId);
            if (pointerId < 0 || pointerId >= MAX_TOUCH_POINTS){
                continue;
            }

            // 记录当前触控索引
            if (curPointerIdArr.contains(pointerId)){
                // 获得当前点的坐标
                pointFs[pointerId].x = event.getX(i);
                pointFs[pointerId].y = event.getY(i);
                pointFs[pointerId].y = pointFs[pointerId].y >= mHeight ? mHeight - 1 : pointFs[pointerId].y;
            }
        }
    }

    /**
     * 将curPointFs的值赋给prePointFs
     */
    private void setPrePointsByCurPoints(){
        // 将curPointFs的值赋给prePointFs
        int pointerIndex = -1;
        int size = curPointerIdArr.size();
        for (int i = 0; i < size; i++){
            pointerIndex = curPointerIdArr.get(i);
            if (pointerIndex < 0 || pointerIndex >= MAX_TOUCH_POINTS){
                continue;
            }

            prePointFs[pointerIndex].x = curPointFs[pointerIndex].x;
            prePointFs[pointerIndex].y = curPointFs[pointerIndex].y;
        }
    }

    /**
     * 创建画笔迁移记录
     * @param event
     */
    private void createMoveRectRecord(MotionEvent event){
        switch (actionMode){
            case DrawConsts.ACTION_MOVE_OVER:// 画笔迁移完成
            case DrawConsts.ACTION_NONE:// 初始状态
                curStrokeRecord = new StrokeRecord(DrawConsts.STROKE_TYPE_MOVE_RECORD);
                curStrokeRecord.path = new Path();
                curStrokeRecord.matrix = new Matrix();
                curStrokeRecord.actionMode = DrawConsts.ACTION_PICKUP;
                curStrokeRecord.bitmapID = UUIDGenerator.getUUID();

                selectPickupColor = Color.YELLOW;
                selectedPaint.setColor(selectPickupColor);
                actionMode = DrawConsts.ACTION_PICKUP;
                LogUtils.d("画笔迁移, actionMode = ACTION_PICKUP");
                break;
            case DrawConsts.ACTION_PICKUP:{
                // 判断触点不在方框中
                float[] downPoint = new float[]{event.getX(), event.getY()};
                boolean isPointInRect = mHelper.isPointInRect(curStrokeRecord.rect, curStrokeRecord.matrix, downPoint);
                if (!isPointInRect){
                    // 触点不在区域内，则直接绘制
                    LogUtils.d("触点不在区域内，则直接绘制");
                    actionMode = DrawConsts.ACTION_NONE;
                    curStrokeRecord.actionMode = DrawConsts.ACTION_NONE;
                } else {
                    actionMode = DrawConsts.ACTION_DRAG;
                    curStrokeRecord.actionMode = DrawConsts.ACTION_DRAG;
                    LogUtils.d("触点在区域内，则开始拖动");
                }
                break;
            }
            case DrawConsts.ACTION_DRAG:{
                LogUtils.d("画笔迁移, actionMode = ACTION_DRAG");
                // 判断触点不在方框中
                float[] downPoint = new float[]{event.getX(), event.getY()};
                boolean isPointInRect = mHelper.isPointInRect(curStrokeRecord.rect, curStrokeRecord.matrix, downPoint);
                if (!isPointInRect){
                    // 触点不在区域内，则直接绘制
                    LogUtils.d("触点不在区域内，则直接绘制");
                    actionMode = DrawConsts.ACTION_MOVE_OVER;
                    curStrokeRecord.actionMode = DrawConsts.ACTION_MOVE_OVER;
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 创建新的画笔记录
     */
    private void createNewPathRecord(MotionEvent event){
        if (!curSketchData.strokeRedoList.isEmpty()){
            curSketchData.strokeRedoList.clear();
        }

        // 编辑模式不为图片时，判断是否点击了图片记录
        if (isHasPhotoLayer && editMode != EDIT_PHOTO){
            float[] downPoint = new float[]{event.getX(), event.getY()};
            boolean isSelectPhoto = mHelper.isPointInEditRect(curPhotoRecord, downPoint);
            if (!isSelectPhoto) {
                isSelectPhoto = selectPhotoRecord(downPoint);
            }

            // 若是选择了图片，则改为图片模式
            if (isSelectPhoto){
                setEditMode(DrawConsts.EDIT_PHOTO);
            }
        }

        switch (editMode){
            case EDIT_STROKE:{ // 画笔记录
                float size = event.getSize() * 1000;
                int areaEraserSize = Math.round(size);
                if (areaEraserSize >= 30){
                    strokeType = DrawConsts.STROKE_TYPE_ERASER_RECT;
                    LogUtils.d("strokeType->%s, areaEraserSize->%s",strokeType, areaEraserSize);

                    curStrokeRecord = new StrokeRecord(DrawConsts.STROKE_TYPE_ERASER_RECT);
                    curStrokeRecord.areaEraserSize = areaEraserSize;
                    curStrokeRecord.pathList = new SparseArray<>();
                    curStrokeRecord.pathList.put(0, new Path());

                    eraserPaint.setStrokeWidth(1f);
                    eraserPaint.setStyle(Paint.Style.FILL);
                    curStrokeRecord.paint = new Paint(eraserPaint);

                    // 获得橡皮擦跟随的图片区域
                    float x = event.getX();
                    float y = event.getY();

                    curStrokeRecord.rect = new RectF();
                    curStrokeRecord.rect.left = x - size * 0.5f;
                    curStrokeRecord.rect.top = y - size * 0.75f;
                    curStrokeRecord.rect.right = x + size * 0.5f;
                    curStrokeRecord.rect.bottom = y + size * 0.75f;
                } else {
                    // 若是面积擦除画笔类型，则使用上一次画笔类型
                    if (strokeType == DrawConsts.STROKE_TYPE_ERASER_RECT){
                        strokeType = lastStrokeType;
                    }
                    curStrokeRecord = new StrokeRecord(strokeType);
                    curStrokeRecord.pathList = new SparseArray<>();

                    // 保存当前画笔类型
                    lastStrokeType = strokeType;
                    LogUtils.d("创建了其他画笔类型,strokeType->%s", strokeType);
                }

                // 当画笔类型不是文字时，添加记录
                if (strokeType != STROKE_TYPE_TEXT){
                    curSketchData.strokeRecordList.add(curStrokeRecord);
                }
                break;
            }
            case EDIT_PHOTO:{ // 图片记录
                break;
            }
            case EDIT_MOVE_RECT:{ // 画笔迁移记录
                createMoveRectRecord(event);
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        // 统一单点和多点
        int action = (event.getAction() & MotionEvent.ACTION_MASK) % 5;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {// 按下
                synchronized (curPointerIdArr){
                    if (event.getPointerCount() == 1){
                        isTouch = true;

                        // 创建新的画笔记录
                        createNewPathRecord(event);
                    }

                    // 当前触控个数
                    int actionIndex = event.getActionIndex();
                    // 获得当前触控索引
                    int pointerId = event.getPointerId(actionIndex);
                    if (pointerId < 0 || pointerId >= MAX_TOUCH_POINTS){
                        break;
                    }

                    LogUtils.d("多点触控，i-->%s, pointerId-->%s" ,actionIndex , pointerId);

                    // 保存当前触控索引
                    if (!curPointerIdArr.contains(pointerId)){
                        curPointerIdArr.add(pointerId);
                    }

                    // 获得当前点的坐标
                    curPointFs[pointerId].x = event.getX(actionIndex);
                    curPointFs[pointerId].y = event.getY(actionIndex);
                    curPointF.x = event.getX();
                    curPointF.y = event.getY();
                    downPointF.x = curPointF.x;
                    downPointF.y = curPointF.y;

                    // 点击按下操作
                    downPointFs[pointerId].x = curPointFs[pointerId].x;
                    downPointFs[pointerId].y = curPointFs[pointerId].y;

                    touchDown(event, pointerId);

                    // 设置prePointFs值
                    prePointFs[pointerId].x = curPointFs[pointerId].x;
                    prePointFs[pointerId].y = curPointFs[pointerId].y;
                    prePointF.x = curPointF.x;
                    prePointF.y = curPointF.y;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {// 移动
                synchronized (curPointerIdArr){
                    // 对移动点的处理
                    handleMovePoints(event, curPointFs);
                    curPointF.x = event.getX();
                    curPointF.y = event.getY();

                    touchMove(event);
                    prePointF.x = curPointF.x;
                    prePointF.y = curPointF.y;

                    // 将curPointFs的值赋给prePointFs
                    setPrePointsByCurPoints();

                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {// 抬起
                try {
                    LOCK.lock();

                    touchUp(event);
                    invalidate();
                } catch (Exception e){
                    e.printStackTrace();
                    isTouch = false;
                } finally {
                    LOCK.unlock();
                }
                break;
            }
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(pfd);

        // 绘制背景
        drawBackground(canvas, curSketchData);

        // 绘制所有记录
        drawAll(canvas);

        // 画笔记录的监听
        if (!isTouch && onDrawChangedListener != null) {
            onDrawChangedListener.onDrawChanged();
        }
    }

    /**
     * 绘制所有的记录
     * @param canvas  UI的主画布
     */
    private void drawAll(Canvas canvas){
        // 撤销或者重做操作时
        if (isUndoOrRedo){
            drawHistory();
            canvas.drawBitmap(mBitmap, 0, 0, mergePaint);
            canvas.drawBitmap(photoBitmap, 0, 0, mergePaint);
            isUndoOrRedo = false;
        } else {
            try {
                LOCK.lock();
                if (editMode == EDIT_PHOTO){
                    drawAllPicture(canvas, true);
                } else {
                    drawAllStroke(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LOCK.unlock();
            }
        }
    }

    /**
     * 绘制所有的画笔记录
     * @param canvas
     */
    private void drawAllStroke(Canvas canvas){
        // 根据editMode，进行不同的处理
        switch (editMode){
            case EDIT_MOVE_RECT:{ // 画笔迁移
                drawStrokeMoveRecord(canvas);
                break;
            }
            case EDIT_STROKE:{ // 画笔记录
                if (strokeType == STROKE_TYPE_ERASER){// 线性橡皮擦
                    // 绘制所有的画笔记录:绘制直线、矩形、圆形和文字
//                    drawStrokeRecords(mCanvas, curSketchData, true);
//                    // 获得保存的图片记录
//                    saveLastBitmap();
//                    canvas.drawBitmap(mBitmap, 0, 0, mergePaint);
//
//                    initTmpCanvas();
                } else if (strokeType == STROKE_TYPE_ERASER_RECT){// 面积橡皮擦
                    // 在增量画布上，绘制擦除痕迹路径
                    clearCanvas(dCanvas);

                    dCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                    dCanvas.drawPath(curStrokeRecord.pathList.get(0), eraserPaint);
                    canvas.drawBitmap(dBitmap, 0, 0, mBitmapPaint);

                    if (!isTouch){
                        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                    }
                    // 绘制跟随的方形橡皮擦
                    if (curStrokeRecord != null && !curStrokeRecord.hasDraw){
                        canvas.save();
                        if (curStrokeRecord.rect != null){
                            canvas.drawRoundRect(curStrokeRecord.rect, 2, 2, eraserIconPaint);
                        }
                        canvas.restore();
                    }
                } else { // 其他画笔记录
                    // 绘制临时画布上的内容
                    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

                    // 直接在画布上绘制当前的路径
                    if (curStrokeRecord != null && !curStrokeRecord.hasDraw){
                        drawOneStrokeRecord(curStrokeRecord, canvas, false);
                    }
                }
                break;
            }
        }

        // 绘制的图片记录
        if (isHasPhotoLayer && photoBitmap != null && !photoBitmap.isRecycled()) {
            canvas.drawBitmap(photoBitmap, 0, 0, mergePaint);
        }
    }

    /**
     * 绘制橡皮擦记录
     *
     * @param canvas
     */
    private void drawEraser(Canvas canvas){
        // 绘制所有的画笔记录:绘制直线、矩形、圆形和文字
//        drawStrokeRecords(mCanvas, curSketchData, true);
//        // 获得保存的图片记录
//        saveLastBitmap();
//        canvas.drawBitmap(mBitmap, 0, 0, mergePaint);
//
//        initTmpCanvas();
    }

    /**
     * 绘制画笔迁移的记录
     *
     * @param canvas
     */
    private void drawStrokeMoveRecord(Canvas canvas){
        // 绘制临时画布上的内容
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        // 绘制中间状态
        switch (actionMode){
            case ACTION_PICKUP:{// 拾取图片
                if (!beyondSelectRect()){
                    canvas.drawPath(curStrokeRecord.path, selectedPaint);
                }
                break;
            }
            case ACTION_DRAG:{ // 移动画笔区域
                canvas.save();
                canvas.setMatrix(curStrokeRecord.matrix);
                canvas.drawPath(curStrokeRecord.path, selectedPaint);
                canvas.drawBitmap(curStrokeRecord.rectBitmap, curStrokeRecord.rect.left , curStrokeRecord.rect.top, mBitmapPaint);
                canvas.restore();
                break;
            }
            case ACTION_MOVE_OVER:{ // 移动完成
                if (curStrokeRecord.hasDraw){
                    // 缓存当前图片
                    mBitmapCache.putBitmapToCache(curStrokeRecord.bitmapID, curStrokeRecord.rectBitmap);

                    // 释放上一次记录中的图片
                    mDataHandler.sendEmptyMessageDelayed(DrawConsts.KEY_RELEASE_RECORD_BITMAP, DrawConsts.MESSAGE_DELAYED_TIME);
                }
                break;
            }
        }
    }

    /**
     * 绘制所有的图片记录
     *
     * @param canvas
     */
    private void drawAllPicture(Canvas canvas, boolean isDrawBoard){
        // 绘制上一次的画笔记录的图片数据
        if (mBitmap != null && !mBitmap.isRecycled()) {
            canvas.drawBitmap(mBitmap, 0, 0, mergePaint);
        }

        // 绘制图片记录
        clearCanvas(photoCanvas);
        drawPhotoRecords(photoCanvas, curSketchData, isDrawBoard);
        canvas.drawBitmap(photoBitmap, 0, 0, mergePaint);
    }

    /**
     * 绘制背景图
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas, SketchData curSketchData) {
        if (curSketchData.backgroundBM != null && !curSketchData.backgroundBM.isRecycled()) {
            Matrix matrix = new Matrix();
            float wScale = (float) canvas.getWidth() / curSketchData.backgroundBM.getWidth();
            float hScale = (float) canvas.getHeight() / curSketchData.backgroundBM.getHeight();
            matrix.postScale(wScale, hScale);
            canvas.drawBitmap(curSketchData.backgroundBM, matrix, null);
        } else {
            canvas.drawColor(getResources().getColor(R.color.sketch_bg_gray));
        }
    }

    /**
     * 绘制画笔记录
     *
     * @param mCanvas 临时画布
     * @param isCompositionPath 是否绘制合成路径
     */
    private void drawStrokeRecords(Canvas mCanvas,SketchData curSketchData, boolean isCompositionPath) {
        if(curSketchData == null || curSketchData.strokeRecordList == null || curSketchData.strokeRecordList.isEmpty()){
            return;
        }

        // 绘制所有画笔路径
        for (StrokeRecord record : curSketchData.strokeRecordList) {
            drawOneStrokeRecord(record, mCanvas, isCompositionPath);
        }
    }

    /**
     * 绘制图片记录
     *
     * @param canvas
     * @param isDrawBoard
     */
    private void drawPhotoRecords(Canvas canvas, SketchData curSketchData, boolean isDrawBoard) {
        // 绘制图片记录
        for (PhotoRecord record : curSketchData.photoRecordList) {
            if (record != null)
                canvas.drawBitmap(record.bitmap, record.matrix, mBitmapPaint);
        }

        // 绘制图片的四个角和中心点
        if (isDrawBoard && editMode == EDIT_PHOTO && curPhotoRecord != null) {
            SCALE_MAX = curPhotoRecord.scaleMax;
            float[] photoCorners = calculatePhotoCorners(curPhotoRecord);//计算图片四个角点和中心点
            drawBoard(canvas, photoCorners);//绘制图形边线
            drawMarks(canvas, photoCorners);//绘制边角图片
        }
    }

    /**
     * 绘制一条画笔路径记录
     * @param record
     * @param canvas
     * @param isCompositionPath
     */
    private void drawOneStrokeRecord(StrokeRecord record, Canvas canvas, boolean isCompositionPath){
        if (record == null){
            return;
        }
        int type = record.type;
        if (type == STROKE_TYPE_ERASER // 绘制线性橡皮擦
                || type == STROKE_TYPE_ERASER_RECT  // 绘制面积橡皮擦
                || type == STROKE_TYPE_ERASER_CIRCLE // 绘制圈选橡皮擦
                ||type == STROKE_TYPE_LINE) { // 绘制直线
            if (record.pathList.size() == 0 || record.pathList.get(0) == null){
                return;
            }

            canvas.drawPath(record.pathList.get(0), record.paint);
        } else if (type == STROKE_TYPE_MOVE_RECORD) { // 画笔迁移
            if (record.actionMode == ACTION_MOVE_OVER){
//                LogUtils.d("绘制ACTION_MOVE_OVER");
                // 清除方框中的内容
                canvas.save(Canvas.ALL_SAVE_FLAG);
                canvas.drawPath(record.path, record.paint);
                canvas.restore();

                Bitmap tmpBitmap = record.rectBitmap;

                // 从缓存中获得图片
                if (tmpBitmap == null || tmpBitmap.isRecycled()){
                    if (!TextUtils.isEmpty(record.bitmapID)){
                        tmpBitmap = mBitmapCache.getBitmapFromCache(record.bitmapID);
                    }
                }

                if (tmpBitmap == null || tmpBitmap.isRecycled()){
                    return;
                }

                // 获得移动图片
                canvas.save();
                canvas.setMatrix(record.matrix);
                canvas.drawBitmap(tmpBitmap, record.rect.left , record.rect.top, mBitmapPaint);
                canvas.restore();
            }
        } else if (type == STROKE_TYPE_DRAW) { // 绘制光滑曲线
            if (isCompositionPath){// 绘制合成路径
//                LogUtils.d("绘制合成路径");
                if (record.path == null){
                    return;
                }
                canvas.drawPath(record.path, record.paint);
            } else {// 绘制分路径
//                LogUtils.d("绘制分路径");
                if (record.pathList == null || record.pathList.size()==0){
                    return;
                }

                // 遍历record.pathList，获得绘制数据
                Path tmpPath = null;
                int len = record.pathList.size();
                for (int i = 0; i < len; i++){
                    tmpPath = record.pathList.get(i);
                    if (tmpPath == null){
                        continue;
                    }

                    canvas.drawPath(tmpPath, record.paint);
                }
            }
        } else if (type == STROKE_TYPE_PHOTO) {// 绘制图片
            Bitmap tmpBitmap = record.rectBitmap;
            // 从缓存中获得图片
            if (tmpBitmap == null || tmpBitmap.isRecycled()){
                if (!TextUtils.isEmpty(record.bitmapID)){
                    tmpBitmap = mBitmapCache.getBitmapFromCache(record.bitmapID);
                }
            }

            if (tmpBitmap == null || tmpBitmap.isRecycled()){
                return;
            }

            canvas.drawBitmap(tmpBitmap, record.matrix, record.paint);
        } else if (type == STROKE_TYPE_CIRCLE) {// 绘制椭圆
            canvas.drawOval(record.rect, record.paint);
        } else if (type == STROKE_TYPE_RECTANGLE) {// 绘制方形
            canvas.drawRect(record.rect, record.paint);
        } else if (type == STROKE_TYPE_TEXT) {// 绘制文字
            if (record.text != null) {
                StaticLayout layout = new StaticLayout(record.text, record.textPaint, record.textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
                canvas.translate(record.textOffX, record.textOffY);
                layout.draw(canvas);
                canvas.translate(-record.textOffX, -record.textOffY);
            }
        }
    }

    /**
     * 绘制图像边线（由于图形旋转或不一定是矩形，所以用Path绘制边线）
     *
     * @param canvas
     * @param photoCorners
     */
    private void drawBoard(Canvas canvas, float[] photoCorners) {
        Path photoBorderPath = new Path();
        photoBorderPath.moveTo(photoCorners[0], photoCorners[1]);
        photoBorderPath.lineTo(photoCorners[2], photoCorners[3]);
        photoBorderPath.lineTo(photoCorners[4], photoCorners[5]);
        photoBorderPath.lineTo(photoCorners[6], photoCorners[7]);
        photoBorderPath.lineTo(photoCorners[0], photoCorners[1]);
        canvas.drawPath(photoBorderPath, boardPaint);
    }

    /**
     * 绘制边角操作图标
     *
     * @param canvas
     * @param photoCorners
     */
    private void drawMarks(Canvas canvas, float[] photoCorners) {
        float x;
        float y;

        // 复制
        x = photoCorners[0] - markerCopyRect.width() / 2;
        y = photoCorners[1] - markerCopyRect.height() / 2;
        markerCopyRect.offsetTo(x, y);
        canvas.drawBitmap(mirrorMarkBM, x, y, null);

        // 融合
        x = (photoCorners[0] + photoCorners[2]) / 2 - markerStampRect.width() / 2;
        y = (photoCorners[1] + photoCorners[3]) / 2 - markerStampRect.height() / 2;
        markerStampRect.offsetTo(x, y);
        canvas.drawBitmap(stampMarkBM, x, y, null);

        // 删除
        x = photoCorners[2] - markerDeleteRect.width() / 2;
        y = photoCorners[3] - markerDeleteRect.height() / 2;
        markerDeleteRect.offsetTo(x, y);
        canvas.drawBitmap(deleteMarkBM, x, y, null);

        // 复位
        x = photoCorners[4] - markerRotateRect.width() / 2;
        y = photoCorners[5] - markerRotateRect.height() / 2;
        markerRotateRect.offsetTo(x, y);
        canvas.drawBitmap(rotateMarkBM, x, y, null);

        // 旋转
        x = photoCorners[6] - markerResetRect.width() / 2;
        y = photoCorners[7] - markerResetRect.height() / 2;
        markerResetRect.offsetTo(x, y);
        canvas.drawBitmap(resetMarkBM, x, y, null);
    }

    /**
     * 绘制记录
     *
     * @param tmpCanvas   临时画布
     * @param isDrawBoard
     */
    private void drawRecord(Canvas tmpCanvas, SketchData curSketchData, boolean isDrawBoard) {
        if (curSketchData != null) {
            // 绘制所有的画笔记录:绘制直线、矩形、圆形和文字
            drawStrokeRecords(tmpCanvas, curSketchData, true);

            // 绘制图片记录
            drawPhotoRecords(tmpCanvas, curSketchData, isDrawBoard);
        }
    }

    /**
     * 计算视图片的四个角落
     *
     * @param record
     * @return
     */
    private float[] calculatePhotoCorners(PhotoRecord record) {
        float[] photoCornersSrc = new float[10];//0,1代表左上角点XY，2,3代表右上角点XY，4,5代表右下角点XY，6,7代表左下角点XY，8,9代表中心点XY
        float[] photoCorners = new float[10];//0,1代表左上角点XY，2,3代表右上角点XY，4,5代表右下角点XY，6,7代表左下角点XY，8,9代表中心点XY
        RectF rectF = record.photoRectSrc;
        photoCornersSrc[0] = rectF.left;
        photoCornersSrc[1] = rectF.top;

        photoCornersSrc[2] = rectF.right;
        photoCornersSrc[3] = rectF.top;

        photoCornersSrc[4] = rectF.right;
        photoCornersSrc[5] = rectF.bottom;

        photoCornersSrc[6] = rectF.left;
        photoCornersSrc[7] = rectF.bottom;

        // 中心点
        photoCornersSrc[8] = rectF.centerX();
        photoCornersSrc[9] = rectF.centerY();
        curPhotoRecord.matrix.mapPoints(photoCorners, photoCornersSrc);

        return photoCorners;
    }

    /**
     * 添加文字画笔记录
     *
     * @param record
     */
    public void addTextStrokeRecord(StrokeRecord record) {
        curSketchData.strokeRecordList.add(record);
        drawOneStrokeRecord(record, mCanvas, true);
        invalidate();
    }

    /**
     * 超出选取的范围
     * @return
     */
    private boolean beyondSelectRect(){
        boolean flag = false;
        if (curStrokeRecord.rect != null){
            boolean beyondWidth = (int)(curStrokeRecord.rect.bottom - curStrokeRecord.rect.top) > ScreenUtils.dip2px(mContext, 500);
            boolean beyondHeight = (int)(curStrokeRecord.rect.right - curStrokeRecord.rect.left) > ScreenUtils.dip2px(mContext, 900);
            if (beyondWidth || beyondHeight){
                flag = true;
            }
        }

        return flag;
    }

    /**
     * 按下操作
     * @param event
     * @param pointerId
     */
    private void touchDown(MotionEvent event, int pointerId) {
        // 根据不同模式进行不同的处理
        switch (editMode){
            case EDIT_STROKE:{// 画笔模式
                touchDownEditStroke(pointerId);
                break;
            }
            case EDIT_MOVE_RECT:{// 移动画笔区域模式
                touchDownEditRect(pointerId);
                break;
            }
            case EDIT_PHOTO:{// 图片模式
                boolean isSelect = touchDownEditPicture();

                // 检测到快速点击了图片
//                if(isSelect
//                        && event.getPointerCount() == 1
//                        && ViewUtils.isFastDoubleClick(500)){
//                    doubleClickSetBg();
//                }

                // 判断是否为缩放
                if (event.getPointerCount() > 1) {// 多点模式
                    LogUtils.d("多点触控，按下操作");
                    oldDist = spacing(event);
                    if ((actionMode == ACTION_DRAG)
                            && (oldDist > DrawConsts.VALID_MIN_LEN)) {//防止误触
                        mHelper.calMidPoint(mid, event);
                        actionMode = ACTION_SCALE;
                        LogUtils.d("多点触控，开始缩放");
                    }

                    // 获得角度
                    saveRotate = mHelper.calRotation(event);
                }

                break;
            }
            default:{
                break;
            }
        }
    }

    /**
     * 移动操作
     *
     * @param event
     */
    private void touchMove(MotionEvent event) {
        switch (editMode) {
            case EDIT_STROKE: {// 绘制画笔模式
                touchMoveEditStroke();
                break;
            }
            case EDIT_MOVE_RECT: {// 画笔迁移模式
                touchMoveEditRect();
                break;
            }
            case EDIT_PHOTO: {// 编辑图片模式
                touchMoveEditPicture(event);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 抬起操作
     */
    private void touchUp(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        Integer pointerId = event.getPointerId(actionIndex);
        curPointerIdArr.remove(pointerId);

        // 最后一个手指抬起的时候
        if (event.getPointerCount() <= 1) {
            isTouch = false;
            curPointerIdArr.clear();

            // 根据编辑模式，做不同的处理
            switch (editMode) {
                case EDIT_PHOTO: {
                    // 图片操作抬起时，判断是否正在操作图片或者点在图片区域
                    if (actionMode == ACTION_NONE) {
                        float[] downPoint = new float[]{event.getX(), event.getY()};
                        boolean isSelectPhoto = mHelper.isPointInEditRect(curPhotoRecord, downPoint);
                        if (!isSelectPhoto) {
                            isSelectPhoto = selectPhotoRecord(downPoint);
                        }
                        LogUtils.d("EDIT_PHOTO isSelectPhoto->" + isSelectPhoto);
                        if (!isSelectPhoto) {
                            LogUtils.d("EDIT_PHOTO lastEditMode->" + lastEditMode);
                            setEditMode(lastEditMode);
                        }
                    }
                    break;
                }
                case EDIT_MOVE_RECT: { // 画笔迁移
                    switch (actionMode) {
                        case ACTION_NONE: { // 没有操作状态的时候, 删除无效记录
                            actionMode = ACTION_NONE;
                            curStrokeRecord.actionMode = DrawConsts.ACTION_NONE;
                            curSketchData.strokeRecordList.remove(curStrokeRecord);

                            // 释放当前记录中的图片
                            releaseStrokeRecordBitmap();
                            break;
                        }
                        case ACTION_PICKUP: { // 拾取图片
                            // 判断拾取的区域超出范围
                            if (beyondSelectRect()){  // 拾取的区域超出范围
                                actionMode = ACTION_NONE;
                                curStrokeRecord.actionMode = DrawConsts.ACTION_NONE;
                                curSketchData.strokeRecordList.remove(curStrokeRecord);

                                // 释放当前记录中的图片
                                releaseStrokeRecordBitmap();

                                String msg = getResources().getString(R.string.tip_extend_select_rect , DrawConsts.EXTEND_WIDTH + "", DrawConsts.EXTEND_HEIGHT + "");
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                            } else { // 没有超出范围
                                // 闭合路径
                                float midX = (curPointFs[0].x + prePointFs[0].x) / 2;
                                float midY = (curPointFs[0].y + prePointFs[0].y) / 2;
                                curStrokeRecord.path.quadTo(prePointFs[0].x, prePointFs[0].y, midX, midY);
                                curStrokeRecord.path.close();

                                mCanvas.save(Canvas.ALL_SAVE_FLAG);
                                mCanvas.restore();

                                // 获得拾取的图片
                                curStrokeRecord.rectBitmap = BitmapUtils.pickupBitmapByPathRect(mBitmap, curStrokeRecord, curStrokeRecord.path);
                            }
                            break;
                        }
                        case ACTION_MOVE_OVER: { // 移动结束
                            if (!beyondSelectRect()){
                                // 触控抬起时，切换为擦除画笔
                                curStrokeRecord.actionMode = DrawConsts.ACTION_MOVE_OVER;

                                // 添加画笔迁移记录，绘制画笔迁移
                                curSketchData.strokeRecordList.add(curStrokeRecord);
                                drawOneStrokeRecord(curStrokeRecord, mCanvas, true);
                                curStrokeRecord.hasDraw = true;
                            } else {
                                actionMode = ACTION_NONE;
                            }
                            break;
                        }
                    }
                    LogUtils.d("touchUp   STROKE_TYPE_MOVE_RECORD, actionMode->" + actionMode);
                    break;
                }
                case EDIT_STROKE: { // 画笔记录
                    // 根据画笔类型，做不同的操作
                    switch (strokeType) {
                        case STROKE_TYPE_ERASER_RECT: {// 接触面积橡皮擦
                            drawOneStrokeRecord(curStrokeRecord, mCanvas, true);
                            curStrokeRecord.hasDraw = true;
                            break;
                        }
                        case STROKE_TYPE_ERASER_CIRCLE: {// 圈选橡皮擦
                            float midX = (curPointFs[0].x + prePointFs[0].x) / 2;
                            float midY = (curPointFs[0].y + prePointFs[0].y) / 2;
                            curStrokeRecord.pathList.get(0).quadTo(prePointFs[0].x, prePointFs[0].y, midX, midY);
                            curStrokeRecord.pathList.get(0).close();

                            // 触控抬起时，切换为擦除画笔
                            eraserPaint.setStyle(Paint.Style.FILL);
                            curStrokeRecord.paint = new Paint(eraserPaint);
                            break;
                        }
                        case STROKE_TYPE_DRAW: {// 曲线画笔
                            // 遍历record.pathList，获得绘制数据
                            if (curStrokeRecord.path == null) {
                                curStrokeRecord.path = new Path();
                            }

                            Path tmpPath = null;
                            int len = curStrokeRecord.pathList.size();
                            for (int i = 0; i < len; i++) {
                                tmpPath = curStrokeRecord.pathList.get(i);
                                if (tmpPath == null) {
                                    continue;
                                }

                                curStrokeRecord.path.addPath(tmpPath);
                            }
                            break;
                        }
                    }

                    // 绘制画笔记录
                    if (strokeType != STROKE_TYPE_ERASER_RECT) {
                        drawOneStrokeRecord(curStrokeRecord, mCanvas, true);
                        curStrokeRecord.hasDraw = true;
                    }
                    break;
                }
            }
        }

    }

    /**
     * 按下的为画笔迁移
     */
    private void touchDownEditRect(int pointerId){
        // 不同类型不同的操作
        switch (actionMode){
            case ACTION_PICKUP:{// 画笔区域的选择过程
                LogUtils.d("画笔迁移,touchDownEditRect, ACTION_PICKUP");
                if(pointerId != 0){
                    return;
                }

//                curStrokeRecord.pathList.put(0, new Path());
                curStrokeRecord.path.moveTo(downPointFs[0].x, downPointFs[0].y);

                RectF rect = new RectF(downPointFs[0].x, downPointFs[0].y, downPointFs[0].x, downPointFs[0].y);
                curStrokeRecord.rect = rect;
                curStrokeRecord.paint = new Paint(selectedPaint); // Clones the mPaint object
                break;
            }
            case DrawConsts.ACTION_DRAG:{// 画笔区域的移动
                eraserPaint.setStyle(Paint.Style.FILL);
                curStrokeRecord.paint = new Paint(eraserPaint);

                // 清除方框中的内容
                mCanvas.save(Canvas.ALL_SAVE_FLAG);
                mCanvas.drawPath(curStrokeRecord.path, curStrokeRecord.paint);
                mCanvas.restore();
                LogUtils.d("画笔迁移,touchDownEditRect, ACTION_DRAG");
                break;
            }
            default:{
                break;
            }
        }
    }

    /**
     * 按下时，操作模式为EDIT_STROKE
     * @param pointerId
     */
    private void touchDownEditStroke(int pointerId) {
        switch (strokeType){
            case STROKE_TYPE_ERASER_RECT:{ // 接触面积橡皮擦
//                if(pointerId != 0){
//                    return;
//                }

                if (curStrokeRecord.pathList == null || curStrokeRecord.pathList.get(0) == null){
                    return;
                }

                curStrokeRecord.pathList.get(0).moveTo(curPointF.x - curStrokeRecord.areaEraserSize * 0.5f,
                        curPointF.y - curStrokeRecord.areaEraserSize * 0.75f);
                break;
            }
            case STROKE_TYPE_ERASER:{// 线性橡皮擦
                curStrokeRecord.pathList.put(0, new Path());
                curStrokeRecord.pathList.get(0).moveTo(downPointFs[0].x, downPointFs[0].y);

                eraserPaint.setStrokeWidth(eraserSize);
                eraserPaint.setStyle(Paint.Style.STROKE);
                curStrokeRecord.paint = new Paint(eraserPaint); // Clones the mPaint object
                break;
            }
            case STROKE_TYPE_ERASER_CIRCLE: {// 橡皮擦圈选
                if(pointerId != 0){
                    return;
                }

                curStrokeRecord.pathList.put(0, new Path());
                curStrokeRecord.pathList.get(0).moveTo(downPointFs[0].x, downPointFs[0].y);

                selectPickupColor = Color.WHITE;
                selectedPaint.setColor(selectPickupColor);
                curStrokeRecord.paint = new Paint(selectedPaint); // Clones the mPaint object
                break;
            }
            case STROKE_TYPE_DRAW:{ // 光滑曲线
                PointF downPointF = curPointFs[pointerId];
                // 初始化路径
                Path tmpPath = curStrokeRecord.pathList.get(pointerId);
                if (tmpPath == null){
                    LogUtils.d("按下手指");
                    curStrokeRecord.pathList.put(pointerId, new Path());
                }

                curStrokeRecord.pathList.get(pointerId).moveTo(downPointF.x, downPointF.y);

                strokePaint.setColor(strokeRealColor);
                strokePaint.setStrokeWidth(strokeSize);
                curStrokeRecord.paint = new Paint(strokePaint);
                break;
            }
            case STROKE_TYPE_LINE:{ // 直线
                curStrokeRecord.pathList.put(0, new Path());
                curStrokeRecord.pathList.get(0).moveTo(downPointFs[0].x, downPointFs[0].y);

                strokePaint.setColor(strokeRealColor);
                strokePaint.setStrokeWidth(strokeSize);
                curStrokeRecord.paint = new Paint(strokePaint);
                break;
            }
            case STROKE_TYPE_RECTANGLE:// 方形
            case STROKE_TYPE_CIRCLE:{ // 圆形
                RectF rect = new RectF(downPointFs[0].x, downPointFs[0].y, downPointFs[0].x, downPointFs[0].y);
                curStrokeRecord.rect = rect;
                strokePaint.setColor(strokeRealColor);
                strokePaint.setStrokeWidth(strokeSize);
                curStrokeRecord.paint = new Paint(strokePaint); // Clones the mPaint object
                break;
            }
            case STROKE_TYPE_TEXT:{ // 文字
                curStrokeRecord.textOffX = (int) downPointFs[0].x;
                curStrokeRecord.textOffY = (int) downPointFs[0].y;
                TextPaint tp = new TextPaint();
                tp.setColor(strokeRealColor);
                curStrokeRecord.textPaint = tp; // Clones the mPaint object
                textWindowCallback.onText(this, curStrokeRecord);
                break;
            }
        }
    }

    /**
     * 按下时，操作模式不为EDIT_STROKE
     */
    private boolean touchDownEditPicture(){
        boolean isSelectPhoto = false;
        float[] downPoint = new float[]{downPointFs[0].x, downPointFs[0].y};

        actionMode = ACTION_NONE;
        LogUtils.d("editMode -->%s", editMode);
        if (editMode == EDIT_PHOTO) {// 编辑图片
            if (isInMarkRect(downPoint)) {// 先判操作标记区域
                LogUtils.d("editMode -->EDIT_PHOTO, 先判操作标记区域");
                isSelectPhoto = true;
                return isSelectPhoto;
            }

            if (mHelper.isPointInEditRect(curPhotoRecord, downPoint)) {//再判断是否点击了当前图片
                LogUtils.d("editMode -->EDIT_PHOTO, 再判断是否点击了当前图片");
                actionMode = ACTION_DRAG;
                isSelectPhoto = true;
                return isSelectPhoto;
            }

            LogUtils.d("editMode -->EDIT_PHOTO, 最后判断是否点击了其他图片");
            isSelectPhoto = selectPhotoRecord(downPoint);//最后判断是否点击了其他图片
            if (isSelectPhoto){
                actionMode = ACTION_DRAG;
            } else {
                actionMode = ACTION_NONE;
            }
        }

        return isSelectPhoto;
    }

    /**
     * 移动时，操作模式为EDIT_MOVE_RECT
     */
    private void touchMoveEditRect(){
        LogUtils.d("画笔迁移,touchMoveEditRect, actionMode->" + actionMode);
        switch (actionMode){
            case ACTION_PICKUP:{ // 拾取画笔区域
                if (curStrokeRecord.path == null){
                    return;
                }

                float midX = (curPointFs[0].x + prePointFs[0].x) / 2;
                float midY = (curPointFs[0].y + prePointFs[0].y) / 2;
                curStrokeRecord.path.quadTo(prePointFs[0].x, prePointFs[0].y, midX, midY);

                float left = (curStrokeRecord.rect.left < midX) ? curStrokeRecord.rect.left : midX;
                float top = (curStrokeRecord.rect.top < midY) ? curStrokeRecord.rect.top : midY;
                float right = (curStrokeRecord.rect.right > midX) ? curStrokeRecord.rect.right : midX;
                float bottom = (curStrokeRecord.rect.bottom > midY) ? curStrokeRecord.rect.bottom : midY;
                curStrokeRecord.rect.set(left, top, right, bottom);
                break;
            }
            case ACTION_DRAG:{ // 拖动操作
                float distanceX = curPointFs[0].x - prePointFs[0].x;
                float distanceY = curPointFs[0].y - prePointFs[0].y;
                if (Math.abs(distanceX) > 1 || Math.abs(distanceY) > 1){
                    curStrokeRecord.matrix.postTranslate(distanceX, distanceY);
                }
                break;
            }
        }


    }

    /**
     * 移动时，操作模式为EDIT_STROKE
     */
    private void touchMoveEditStroke(){
        if (strokeType == STROKE_TYPE_ERASER) {// 橡皮擦
            float midX = (curPointFs[0].x + prePointFs[0].x) / 2;
            float midY = (curPointFs[0].y + prePointFs[0].y) / 2;
            curStrokeRecord.pathList.get(0).quadTo(prePointFs[0].x, prePointFs[0].y, midX, midY);
        } else if (strokeType == STROKE_TYPE_ERASER_RECT) {// 橡皮擦面积擦除
            if (curStrokeRecord.pathList.get(0) == null){
                return;
            }

            // 连接画笔
            if (curStrokeRecord.rect == null){
                curStrokeRecord.rect = new RectF();
            }

            curStrokeRecord.rect.left = curPointF.x - curStrokeRecord.areaEraserSize * 0.5f;
            curStrokeRecord.rect.top = curPointF.y - curStrokeRecord.areaEraserSize * 0.75f;
            curStrokeRecord.rect.right = curPointF.x + curStrokeRecord.areaEraserSize * 0.5f;
            curStrokeRecord.rect.bottom = curPointF.y + curStrokeRecord.areaEraserSize * 0.75f;

            // 添加矩形路径
            curStrokeRecord.pathList.get(0).moveTo(curPointF.x - curStrokeRecord.areaEraserSize * 0.5f,
                    curPointF.y - curStrokeRecord.areaEraserSize * 0.75f);
            curStrokeRecord.pathList.get(0).addRect(curPointF.x - curStrokeRecord.areaEraserSize * 0.5f,
                    curPointF.y - curStrokeRecord.areaEraserSize * 0.75f,
                    curPointF.x + curStrokeRecord.areaEraserSize * 0.5f,
                    curPointF.y + curStrokeRecord.areaEraserSize * 0.75f,
                    Path.Direction.CCW);
        } else if (strokeType == STROKE_TYPE_ERASER_CIRCLE) {// 橡皮擦圈选
            if (curStrokeRecord.pathList.get(0) == null){
                return;
            }

            float midX = (curPointF.x + prePointF.x) / 2;
            float midY = (curPointF.y + prePointF.y) / 2;
            curStrokeRecord.pathList.get(0).quadTo(prePointF.x, prePointF.y, midX, midY);
        } else if (strokeType == STROKE_TYPE_DRAW) {// 光滑曲线
            if (curStrokeRecord.pathList == null || curStrokeRecord.pathList.size() == 0){
                return;
            }

            float preX = 0f;
            float preY = 0f;
            float curX = 0f;
            float curY = 0f;

            float dx = 0;
            float dy = 0;

            float cX = 0;
            float cY = 0;
            int pointerId = -1;
            int size = curPointerIdArr.size();
            for (int i = 0; i < size; i++){
                pointerId = curPointerIdArr.get(i);
                if ((pointerId < 0) || (curStrokeRecord.pathList.get(pointerId) == null)){
                    continue;
                }

                preX = prePointFs[pointerId].x;
                preY = prePointFs[pointerId].y;
                curX = curPointFs[pointerId].x;
                curY = curPointFs[pointerId].y ;

                dx = Math.abs(curX - preX);
                dy = Math.abs(curY - preY);
                //两点之间的距离大于等于1时，生成贝塞尔绘制曲线
                if (dx >= 1 || dy >= 1){
                    //设置贝塞尔曲线的操作点为起点和终点的一半
                    cX = (preX + curX) / 2;
                    cY = (preY + curY) / 2;
                    curStrokeRecord.pathList.get(pointerId).quadTo(preX, preY, cX, cY);
                }
            }
        } else if (strokeType == STROKE_TYPE_LINE) {// 直线
            if (curStrokeRecord.pathList.get(0) == null){
                return;
            }

            curStrokeRecord.pathList.get(0).reset();
            curStrokeRecord.pathList.get(0).moveTo(downPointFs[0].x, downPointFs[0].y);
            curStrokeRecord.pathList.get(0).lineTo(curPointFs[0].x, curPointFs[0].y);
        } else if ((strokeType == STROKE_TYPE_CIRCLE)
                || (strokeType == STROKE_TYPE_RECTANGLE)) {// 方形和圆形
            float left = (downPointFs[0].x < curPointFs[0].x) ? downPointFs[0].x : curPointFs[0].x;
            float top = (downPointFs[0].y < curPointFs[0].y) ? downPointFs[0].y : curPointFs[0].y;
            float right = (downPointFs[0].x > curPointFs[0].x) ? downPointFs[0].x : curPointFs[0].x;
            float bottom = (downPointFs[0].y > curPointFs[0].y) ? downPointFs[0].y : curPointFs[0].y;
            curStrokeRecord.rect.set(left, top, right, bottom);
        }
    }

    /**
     * 移动时，操作模式为EDIT_PICTURE
     * @param event
     */
    private void touchMoveEditPicture(MotionEvent event){
        if (curPhotoRecord != null) {
            if (actionMode == ACTION_DRAG) { // 移动
                onDragAction(curPointFs[0].x - prePointFs[0].x, curPointFs[0].y - prePointFs[0].y);
            } else if (actionMode == ACTION_ROTATE) { // 选择
                onRotateAction(curPhotoRecord);
            }
//            else if (actionMode == ACTION_SCALE
//                    && event.getPointerCount() == 2) { // 缩放
////                mScaleGestureDetector.onTouchEvent(event);
//                onScaleAction(event);
//            }
        }
    }

    /**
     * 判断是否点击了其他图片
     *
     * @param downPoint
     */
    private boolean selectPhotoRecord(float[] downPoint) {
        boolean flag = false;

        if (curSketchData.photoRecordList.size() == 0) {
            return flag;
        }

        // 判断是否选中了图片
        PhotoRecord clickRecord = null;
        for (int i = curSketchData.photoRecordList.size() - 1; i >= 0; i--) {
            PhotoRecord record = curSketchData.photoRecordList.get(i);
            if (mHelper.isPointInEditRect(record, downPoint)) {
                clickRecord = record;
                break;
            }
        }

        if (clickRecord != null) {
//            LogUtils.d("editMode -->EDIT_PHOTO, 选中了图片");
            setCurPhotoRecord(clickRecord);
            flag = true;
        }

        return flag;
    }

    /**
     * 判操作标记区域
     *
     * @param downPoint
     * @return
     */
    private boolean isInMarkRect(float[] downPoint) {
        // 旋转
        if (markerRotateRect.contains(downPoint[0], (int) downPoint[1])) {//判断是否在区域内
            LogUtils.d("选择了选择操作");
            actionMode = ACTION_ROTATE;
            return true;
        }

        // 融合
        if (markerStampRect.contains(downPoint[0], (int) downPoint[1])) {//判断是否在区域内
            LogUtils.d("选择了融合操作");
            curSketchData.photoRecordList.remove(curPhotoRecord);

            // 判断是否有图片层
            if (curSketchData.photoRecordList.isEmpty()){
                isHasPhotoLayer = false;
            }
            // 绘制添加图片记录
            addPhotoRectAndDraw();
            setCurPhotoRecord(null);
            actionMode = ACTION_NONE;
            return true;
        }

        // 删除
        if (markerDeleteRect.contains(downPoint[0], (int) downPoint[1])) {//判断是否在区域内
            LogUtils.d("选择了删除操作");
            curSketchData.photoRecordList.remove(curPhotoRecord);

            // 判断是否有图片层
            if (curSketchData.photoRecordList.isEmpty()){
                isHasPhotoLayer = false;

                // 释放图片
                BitmapUtils.releaseBitmap(curPhotoRecord.bitmap);
            }

            setCurPhotoRecord(null);
            actionMode = ACTION_NONE;
            return true;
        }

        // 复制
        if (markerCopyRect.contains(downPoint[0], (int) downPoint[1])) {//判断是否在区域内
            LogUtils.d("选择了复制操作");
            PhotoRecord newRecord = mHelper.initPhotoRecord(this, curPhotoRecord.bitmap);
            newRecord.matrix = new Matrix(curPhotoRecord.matrix);
            newRecord.matrix.postTranslate(ScreenUtils.dip2px(mContext, 20), ScreenUtils.dip2px(mContext, 20));//偏移小段距离以分辨新复制的图片
            curSketchData.photoRecordList.add(newRecord);

            setCurPhotoRecord(newRecord);
            actionMode = ACTION_DRAG;
            return true;
        }

        // 重置
        if (markerResetRect.contains(downPoint[0], (int) downPoint[1])) {//判断是否在区域内
            LogUtils.d("选择了重置操作");
            curPhotoRecord.matrix.reset();
            curPhotoRecord.matrix.setTranslate(getWidth() / 2.0f - curPhotoRecord.photoRectSrc.width() / 2.0f,
                    getHeight() / 2.0f - curPhotoRecord.photoRectSrc.height() / 2.0f);
            actionMode = ACTION_DRAG;
            invalidate();
            return true;
        }

        return false;
    }

    /**
     * 双击选中的图片，设置背景
     */
    private void doubleClickSetBg(){
        // 删除图片
        curSketchData.photoRecordList.remove(curPhotoRecord);

        // 判断是否有图片层
        if (curSketchData.photoRecordList.isEmpty()){
            isHasPhotoLayer = false;
        }

        // 设置背景
        Bitmap out = Bitmap.createBitmap(curPhotoRecord.bitmap.getWidth(), curPhotoRecord.bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(curPhotoRecord.bitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        curSketchData.backgroundBM = Bitmap.createBitmap(out);

        setCurPhotoRecord(null);
        actionMode = ACTION_NONE;
    }

    /**
     * 缩放旋转动作
     *
     * @param record
     */
    private void onRotateAction(PhotoRecord record) {
        // 计算视图片的四个角落和中心点的坐标
        float[] photoCorners = calculatePhotoCorners(record);

        //放大
        //目前触摸点与图片显示中心距离
        float a = (float) Math.sqrt(Math.pow(curPointFs[0].x - photoCorners[8], 2) + Math.pow(curPointFs[0].y - photoCorners[9], 2));
        //目前上次旋转图标与图片显示中心距离
        float b = (float) Math.sqrt(Math.pow(photoCorners[4] - photoCorners[0], 2) + Math.pow(photoCorners[5] - photoCorners[1], 2)) / 2;

        // 获得中心点
        PointF pointCenter = new PointF(photoCorners[8], photoCorners[9]);

        //设置Matrix缩放参数
        double photoLen = Math.sqrt(Math.pow(record.photoRectSrc.width(), 2) + Math.pow(record.photoRectSrc.height(), 2));
        if ((a >= (photoLen / 2 * SCALE_MIN))
                && (a >= SCALE_MIN_LEN)
                && (a <= (photoLen / 2.0f) * SCALE_MAX)) {
            //这种计算方法可以保持旋转图标坐标与触摸点同步缩放
            if (b == 0.0f) {
                return;
            }

            float scale = a / b;
            LogUtils.d("scale: " + scale);
            record.matrix.postScale(scale, scale, pointCenter.x, pointCenter.y);
        }

        // 计算旋转的角度
        double dAngle = MathUtils.getDAngle(prePointFs[0], curPointFs[0], pointCenter);
        LogUtils.d("dAngle: " + dAngle);
        record.matrix.postRotate((float) dAngle, pointCenter.x, pointCenter.y);
    }

    /**
     * 手势缩放
     *
     * @param event
     */
    private void onScaleAction(MotionEvent event) {
        float newDist = MathUtils.spacing(event);
        if(newDist <= DrawConsts.VALID_MIN_LEN){
            return;
        }

        LogUtils.d("缩放");
        // 缩放
        float scaleFactor = newDist / oldDist;
        float[] photoCorners = calculatePhotoCorners(curPhotoRecord);

        //目前图片对角线长度
        float currentLen = (float) Math.sqrt(Math.pow(photoCorners[0] - photoCorners[4], 2)
                + Math.pow(photoCorners[1] - photoCorners[5], 2));
        // 图片原始长度
        double photoLen = Math.sqrt(Math.pow(curPhotoRecord.photoRectSrc.width(), 2)
                + Math.pow(curPhotoRecord.photoRectSrc.height(), 2));

        //设置Matrix缩放参数
        if ((scaleFactor < 1 && currentLen >= photoLen * curPhotoRecord.scaleMin)
                || (scaleFactor > 1 && currentLen <= photoLen * curPhotoRecord.scaleMax)) {
            LogUtils.d("scaleFactor->%s", scaleFactor);
            curPhotoRecord.matrix.postScale(scaleFactor, scaleFactor, mid.x, mid.y);
        }

        // 旋转
/*            LogUtils.d("旋转");
            float rotate = mHelper.calRotation(event);
            float r = rotate - saveRotate;
            curPhotoRecord.matrix.postRotate(r, mWidth / 2, mHeight / 2);*/
    }

    /**
     * 手势缩放
     *
     * @param detector
     */
    private void onScaleAction(ScaleGestureDetector detector) {
        float[] photoCorners = calculatePhotoCorners(curPhotoRecord);
        //目前图片对角线长度
        float len = (float) Math.sqrt(Math.pow(photoCorners[0] - photoCorners[4], 2) + Math.pow(photoCorners[1] - photoCorners[5], 2));
        double photoLen = Math.sqrt(Math.pow(curPhotoRecord.photoRectSrc.width(), 2) + Math.pow(curPhotoRecord.photoRectSrc.height(), 2));
        float scaleFactor = detector.getScaleFactor();

        //设置Matrix缩放参数
        //设置Matrix缩放参数
        if ((scaleFactor < 1 && len >= photoLen * curPhotoRecord.scaleMin)
                || (scaleFactor > 1 && len <= photoLen * curPhotoRecord.scaleMax)) {
            LogUtils.d("scaleFactor->%s", scaleFactor);
            curPhotoRecord.matrix.postScale(scaleFactor, scaleFactor, photoCorners[8], photoCorners[9]);
        }
//        if ((scaleFactor <= SCALE_MAX) && (scaleFactor >= SCALE_MIN)) {
//            Log.e(scaleFactor + "", scaleFactor + "");
//            if (center == null){
//                curPhotoRecord.matrix.postScale(scaleFactor, scaleFactor, photoCorners[8], photoCorners[9]);
//            } else {
//                curPhotoRecord.matrix.postScale(scaleFactor, scaleFactor, center.x, center.y);
//            }
//        }
    }

    /**
     * 缩放旋转动作
     *
     * @param record
     */
    private void onRotateScaleAction(PhotoRecord record, float scale) {
        // 计算视图片的四个角落和中心点的坐标
        float[] photoCorners = calculatePhotoCorners(record);

        //放大
        //目前触摸点与图片显示中心距离
        float a = (float) Math.sqrt(Math.pow(curPointFs[0].x - photoCorners[8], 2) + Math.pow(curPointFs[0].y - photoCorners[9], 2));
        //目前上次旋转图标与图片显示中心距离
        float b = (float) Math.sqrt(Math.pow(photoCorners[4] - photoCorners[0], 2) + Math.pow(photoCorners[5] - photoCorners[1], 2)) / 2;

        // 获得中心点
        PointF pointCenter = new PointF(photoCorners[8], photoCorners[9]);

        //设置Matrix缩放参数
        double photoLen = Math.sqrt(Math.pow(record.photoRectSrc.width(), 2) + Math.pow(record.photoRectSrc.height(), 2));
        if ((a >= (photoLen / 2 * SCALE_MIN))
                && (a >= SCALE_MIN_LEN)
                && (a <= (photoLen / 2.0f) * SCALE_MAX)) {
            //这种计算方法可以保持旋转图标坐标与触摸点同步缩放
            if (scale > 0.0f) {
                LogUtils.d("scale: " + scale);
                record.matrix.postScale(scale, scale, pointCenter.x, pointCenter.y);
            }
        }

        // 计算旋转的角度
        double dAngle = MathUtils.getDAngle(prePointFs[0], curPointFs[0], pointCenter);
        LogUtils.d("dAngle: " + dAngle);
        record.matrix.postRotate((float) dAngle, pointCenter.x, pointCenter.y);
    }

    /**
     * 平移动作
     *
     * @param distanceX
     * @param distanceY
     */
    private void onDragAction(float distanceX, float distanceY) {
        curPhotoRecord.matrix.postTranslate((int) distanceX, (int) distanceY);
    }

    /*
     * 删除一笔
     */
    public void undo() {
        if (curSketchData.strokeRecordList.size() > 0) {
            // 处理画笔迁移的状态
            boolean flag = handleHalfMoveRect();// 是否删除绘制
            if (!flag){
                curSketchData.strokeRedoList.add(curSketchData.strokeRecordList.get(curSketchData.strokeRecordList.size() - 1));
                curSketchData.strokeRecordList.remove(curSketchData.strokeRecordList.size() - 1);
            }

            isUndoOrRedo = true;
            clearCanvas(mCanvas);
            clearCanvas(photoCanvas);
            invalidate();
        }
    }

    /**
     * 撤销
     */
    public void redo() {
        if (curSketchData.strokeRedoList.size() > 0) {
            // 改变图片个数
            StrokeRecord addRecord = curSketchData.strokeRedoList.get(curSketchData.strokeRedoList.size() - 1);
            curSketchData.strokeRecordList.add(addRecord);
            curSketchData.strokeRedoList.remove(curSketchData.strokeRedoList.size() - 1);
        }

        isUndoOrRedo = true;
        clearCanvas(mCanvas);
        clearCanvas(photoCanvas);
        invalidate();
    }

    /**
     * 设置画笔或者橡皮擦的尺寸
     *
     * @param size
     * @param eraserOrStroke
     */
    public void setSize(int size, int eraserOrStroke) {
        switch (eraserOrStroke) {
            case STROKE_TYPE_DRAW:
                strokeSize = size;
                break;
            case STROKE_TYPE_ERASER:
                eraserSize = size;
                break;
            default:
                break;
        }
    }

    /**
     * 清空画板
     */
    public void clearAll() {
        isHasPhotoLayer = false;

        // 先判断是否已经回收
        for (PhotoRecord record : curSketchData.photoRecordList) {
            if (record != null && record.bitmap != null && !record.bitmap.isRecycled()) {
                record.bitmap.recycle();
                record.bitmap = null;
            }
        }

        // 释放背景的图片
        if (curSketchData.backgroundBM != null && !curSketchData.backgroundBM.isRecycled()) {
            // 回收并且置为null
            curSketchData.backgroundBM.recycle();
            curSketchData.backgroundBM = null;
        }

        // 释放当前记录中的图片
        releaseStrokeRecordBitmap();

        curSketchData.strokeRecordList.clear();
        curSketchData.photoRecordList.clear();
        curSketchData.strokeRedoList.clear();
        curPhotoRecord = null;
        curStrokeRecord = null;

        // 清空画布
        clearCanvas(mCanvas);
        clearCanvas(dCanvas);
        clearCanvas(photoCanvas);

        System.gc();
        invalidate();
    }

    /**
     * 获得画板的结果图片
     *
     * @param sketchData
     * @return
     */
    @NonNull
    public Bitmap getResultBitmap(SketchData sketchData) {
        // 清空画布
        clearCanvas(dCanvas);

        Bitmap newBM = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBM);
        canvas.setDrawFilter(pfd);//抗锯齿

        // 先绘制背景
        drawBackground(canvas, sketchData);

        // 绘制所有的画笔记录:绘制图片、曲线、直线、矩形、圆形和文字等
        drawRecord(dCanvas, curSketchData, false);

        // 将临时画布绘制到UI上
        if (dBitmap != null) {
            canvas.drawBitmap(dBitmap, 0, 0, mergePaint);
        }

        return newBM;
    }

    /**
     * 获得当前画板的结果图片
     *
     * @return
     */
    @NonNull
    public Bitmap getResultBitmap() {
        // 清空画布
        clearCanvas(dCanvas);

        Bitmap newBM = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBM);
        canvas.setDrawFilter(pfd);//抗锯齿

        // 先绘制背景
        drawBackground(canvas, curSketchData);

        // 绘制所有的画笔记录:绘制图片、曲线、直线、矩形、圆形和文字等
        drawRecord(dCanvas, curSketchData, false);

        // 将临时画布绘制到UI上
        if (dBitmap != null) {
            canvas.drawBitmap(dBitmap, 0, 0, mergePaint);
        }

        return newBM;
    }

    /**
     * 获得当前的缩略图
     */
    @NonNull
    public Bitmap getThumbnailResultBitmap() {
        return BitmapUtils.createBitmapThumbnail(getResultBitmap(), true, ScreenUtils.dip2px(mContext, 200), ScreenUtils.dip2px(mContext, 200));
    }

    /**
     * 处理画笔移动时，只操作了一半的情况
     */
    private boolean handleHalfMoveRect(){
        boolean flag = false;

        // 处理画笔迁移的状态
        if (editMode == EDIT_MOVE_RECT
                && curStrokeRecord != null
                && curStrokeRecord.type == DrawConsts.STROKE_TYPE_MOVE_RECORD){
            if (actionMode == DrawConsts.ACTION_PICKUP
                    || actionMode == DrawConsts.ACTION_DRAG){
                actionMode = DrawConsts.ACTION_NONE;
                curStrokeRecord.actionMode = DrawConsts.ACTION_NONE;
                curSketchData.strokeRecordList.remove(curStrokeRecord);

                // 释放当前记录中的图片
                releaseStrokeRecordBitmap();
                flag = true;
            }
        }

        return flag;
    }

    /**
     * 释放当前记录中的图片
     */
    private void releaseStrokeRecordBitmap(){
        if (curStrokeRecord.rectBitmap != null
                && !curStrokeRecord.rectBitmap.isRecycled()){
            curStrokeRecord.rectBitmap.recycle();
            curStrokeRecord.rectBitmap = null;
        }
    }

    /**
     * 在画布中添加图片
     *
     * @param path
     */
    public void addPhotoRecord(String path) {
        Bitmap sampleBM = BitmapUtils.getSampleBitMap(mContext, path);
        if (sampleBM != null) {
            PhotoRecord newRecord = mHelper.initPhotoRecord(this, sampleBM);
            setCurPhotoRecord(newRecord);
            isHasPhotoLayer = true;
        } else {
            Toast.makeText(mContext, getResources().getString(R.string.tip_photo_path_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 给画布设置背景
     *
     * @param path
     */
    public void setBackgroundByPath(String path) {
        Bitmap sampleBM = BitmapUtils.getSampleBitMap(mContext, path);
        if (sampleBM != null) {
            curSketchData.backgroundBM = sampleBM;
            invalidate();
        } else {
            Toast.makeText(mContext, getResources().getString(R.string.tip_photo_path_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新画板数据
     *
     * @param sketchData
     */
    public void updateSketchData(SketchData sketchData) {
        // 处理画笔移动时，只操作了一半的情况
        handleHalfMoveRect();

        // 清空画布
        clearCanvas(photoCanvas);
        clearCanvas(dCanvas);
        clearCanvas(mCanvas);

        setSketchData(sketchData);

        drawHistory();
        invalidate();
    }

    /**
     * 设置当前图片记录
     *
     * @param record
     */
    private void setCurPhotoRecord(PhotoRecord record) {
        if (record != null){
            curSketchData.photoRecordList.remove(record);
            curSketchData.photoRecordList.add(record);
        }
        curPhotoRecord = record;
        invalidate();
    }

    /**
     * 添加图片记录
     */
    public void addPhotoRectAndDraw(){
        if (this.editMode == EDIT_PHOTO
                && curPhotoRecord != null){
            LogUtils.d("addPhotoRectAndDraw");

            curStrokeRecord = new StrokeRecord(DrawConsts.STROKE_TYPE_PHOTO);
            curStrokeRecord.rectBitmap = curPhotoRecord.bitmap;
            curStrokeRecord.matrix = curPhotoRecord.matrix;
            curStrokeRecord.rect = curPhotoRecord.photoRectSrc;
            curStrokeRecord.paint = mBitmapPaint;

            // 缓存图片
            curStrokeRecord.bitmapID = UUIDGenerator.getUUID();
            mBitmapCache.putBitmapToCache(curStrokeRecord.bitmapID, curStrokeRecord.rectBitmap);

            curSketchData.strokeRecordList.add(curStrokeRecord);
            curStrokeRecord.hasDraw = true;
            drawOneStrokeRecord(curStrokeRecord, mCanvas, true);

            invalidate();
        }
    }

    /**
     * 设置编辑模式
     *
     * @param editMode
     */
    public void setEditMode(int editMode) {
        if (editMode != EDIT_PHOTO){// 非图片模式模式
            lastEditMode = editMode;// 保存当前编辑模式

            // 绘制图片记录
            clearCanvas(photoCanvas);
            drawPhotoRecords(photoCanvas, curSketchData, false);
        }

        // 处理无效的画笔迁移记录
        if (this.editMode == EDIT_MOVE_RECT){
            if (actionMode == ACTION_PICKUP || actionMode==ACTION_DRAG){
                if (curStrokeRecord != null){
                    actionMode = ACTION_NONE;
                    curStrokeRecord.actionMode = ACTION_NONE;
                    curSketchData.strokeRecordList.remove(curStrokeRecord);
                    isUndoOrRedo = true;
                }

                clearCanvas(dCanvas);
                clearCanvas(mCanvas);
                invalidate();
            }
        }

        if (editMode == DrawConsts.EDIT_MOVE_RECT){
            actionMode = ACTION_NONE;
        }

        this.editMode = editMode;
        invalidate();
    }

    /**
     * 清理画布canvas
     *
     * @param temptCanvas
     */
    public void clearCanvas(Canvas temptCanvas) {
        temptCanvas.drawPaint(clearPaint);
    }

    public int getEditMode() {
        return editMode;
    }

    public void setTextWindowCallback(TextWindowCallback textWindowCallback) {
        this.textWindowCallback = textWindowCallback;
    }

    public void setOnDrawChangedListener(OnDrawChangedListener listener) {
        this.onDrawChangedListener = listener;
    }

    public void setOnPhotoRecordChangeListener(OnPhotoRecordChangeListener listener) {
        this.onPhotoRecordChangeListener = listener;
    }

    public void setStrokeType(int strokeType) {
        lastStrokeType = strokeType;
        this.strokeType = strokeType;
    }

    public int getStrokeType() {
        return strokeType;
    }

    public int getStrokeSize() {
        return Math.round(this.strokeSize);
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public int getRedoCount() {
        return curSketchData.strokeRedoList != null ? curSketchData.strokeRedoList.size() : 0;
    }

    /**
     * 获得画笔记录
     *
     * @param curSketchData
     * @return
     */
    public int getRecordCount(SketchData curSketchData) {
        SketchData tmp = curSketchData;
        if (curSketchData == null){ // curSketchData为null时，设置当前画笔记录
            tmp = this.curSketchData;
        }

        return (tmp.strokeRecordList != null) ?
                (tmp.strokeRecordList.size()) : 0;
    }

    /**
     * 获得当前的画笔数
     * @return
     */
    public int getStrokeRecordCount() {
        return (curSketchData.strokeRecordList != null) ?
                curSketchData.strokeRecordList.size() : 0;
    }

    public int getPhotoRecordCount() {
        return (curSketchData.photoRecordList != null) ?
                curSketchData.photoRecordList.size() : 0;
    }

    public void setSketchData(SketchData sketchData) {
        this.curSketchData = sketchData;
        curPhotoRecord = null;
//        invalidate();
    }

    /**
     * 释放上一个迁移记录的图片
     */
    private void releaseLastRecordBitmap(){
        LogUtils.d("releaseLastRecordBitmap");
        int len = curSketchData.strokeRecordList.size();
        if (len > 1){
            StrokeRecord lastStrokeRecord = curSketchData.strokeRecordList.get(len - 2);
            if (lastStrokeRecord != null
                    && lastStrokeRecord.type == STROKE_TYPE_MOVE_RECORD){
                BitmapUtils.releaseBitmap(lastStrokeRecord.rectBitmap);
                lastStrokeRecord.rectBitmap = null;
                System.gc();
            }
        }
    }

    /**
     * UI异步处理
     */
    public static final class DataHandler extends Handler {
        WeakReference<SketchView> weakReference;

        public DataHandler(SketchView sketchView, Looper looper) {
            super(looper);
            this.weakReference = new WeakReference<>(sketchView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SketchView sketchView = weakReference.get();

            if (sketchView == null) {
                return;
            }

            // 开始处理
            switch (msg.what){
                case 0://

                    break;
                case DrawConsts.KEY_RELEASE_RECORD_BITMAP:{ // 释放上一个迁移记录的图片
                    sketchView.releaseLastRecordBitmap();
                    break;
                }
                default:
                    break;
            }
        }
    }
}
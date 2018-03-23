package com.yinghe.whiteboardlib.utils;

import android.graphics.Color;

/**
 * 绘制相关的常量
 *
 * @author wang
 * @time on 2017/3/23.
 */
public interface DrawConsts {
    /**
     * SketchView中常量
     */
    int STROKE_TYPE_ERASER = 1;// 橡皮擦
    int STROKE_TYPE_DRAW = 2;// 光滑曲线
    int STROKE_TYPE_LINE = 3;// 直线
    int STROKE_TYPE_CIRCLE = 4;// 圆形
    int STROKE_TYPE_RECTANGLE = 5;// 方形

    int STROKE_TYPE_TEXT = 6;// 文字
    int STROKE_TYPE_ERASER_CIRCLE = 7;// 橡皮擦圈选
    int STROKE_TYPE_PHOTO = 8;// 图片
    int STROKE_TYPE_MOVE_RECORD = 9;// 画笔迁移
    int STROKE_TYPE_ERASER_RECT = 10;// 橡皮擦接触面积擦除

    float TOUCH_TOLERANCE = 4;
    int EDIT_STROKE = 1;// 画笔模式
    int EDIT_PHOTO = 2;// 图片模式
    int EDIT_VIDEO = 3;// 视频模式
    int EDIT_MOVE_RECT = 4;// 画笔迁移模式

    // 操作模式
    int ACTION_NONE = 0;
    int ACTION_DRAG = 1;
    int ACTION_SCALE = 2;
    int ACTION_ROTATE = 3;

    int ACTION_PICKUP = 4;// 拾取画笔区域
    int ACTION_MOVE_OVER = 5;// 画笔区域移动结束

    int DEFAULT_STROKE_SIZE = 7;
    int DEFAULT_STROKE_ALPHA = 100;
    int DEFAULT_ERASER_SIZE = 50;

    // 缩放的最小长度
    int MIN_LEN = 20;

    float SCALE_MAX = 4.0f;
    float SCALE_MIN = 0.2f;
    float SCALE_MIN_LEN = 20f;

    // 画笔尺寸
    float BOARD_STROKE_WIDTH= 0.8f;
    float ERASER_STROKE_WIDTH = 30f;

    int VALID_MIN_LEN = 3;// 连个手指间点击的最小距离

    String IMAGE_SAVE_SUFFIX = ".jpg";// 图片保存的后缀

    String FILE_TYPE = "*/*";// 文件类型

    // 视频区域的大小
    int VIDEO_RECORD_WIDTH = 300;
    int VIDEO_RECORD_HEIGHT = 300;

    // 弹框的y轴偏移
    int POPUP_WIN_YOFF = 8;

    /**
     * WhiteBoardFragment中常量
     */
    int COLOR_BLACK = Color.parseColor("#ff000000");
    int COLOR_RED = Color.parseColor("#ffff4444");
    int COLOR_GREEN = Color.parseColor("#ff99cc00");
    int COLOR_ORANGE = Color.parseColor("#ffffbb33");
    int COLOR_BLUE = Color.parseColor("#ff33b5e5");

    // UI的透明度值
    float BTN_ALPHA = 0.4f;
    float BTN_ALL_SHOW = 1.0f;

    int MAX_TOUCH_POINTS = 10;// 多点触控的最大值

    // 画板中图片数量的最大值
    int SELECT_IMAGES_MAX = 5;

    // 消息延迟发送的时间
    int MESSAGE_DELAYED_TIME = 10;
    // Handler消息发送Key
    int KEY_RELEASE_RECORD_BITMAP = 1; // 释放上一次的记录中的图片

    // 选取超出区域的范围
    int EXTEND_WIDTH = 500;
    int EXTEND_HEIGHT = 900;

    // 图片缓存目录
    String BITMAP_CACHE_DIR = "Bitmap_Cache";
}

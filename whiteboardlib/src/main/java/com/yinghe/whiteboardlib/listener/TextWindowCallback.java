package com.yinghe.whiteboardlib.listener;

import android.view.View;

import com.yinghe.whiteboardlib.bean.StrokeRecord;

/**
 * 文字窗口回调监听
 *
 * @author wang
 * @time on 2017/3/23.
 */
public interface TextWindowCallback {
    void onText(View view, StrokeRecord record);
}

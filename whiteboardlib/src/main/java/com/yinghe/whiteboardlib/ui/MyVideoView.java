package com.yinghe.whiteboardlib.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.yinghe.whiteboardlib.utils.DensityUtil;

/**
 * @author Administrator
 * @time on 2017/3/23.
 */
public class MyVideoView extends VideoView {
    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(DensityUtil.dip2px(getContext(),300), widthMeasureSpec);
        int height = getDefaultSize(DensityUtil.dip2px(getContext(),300), heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}

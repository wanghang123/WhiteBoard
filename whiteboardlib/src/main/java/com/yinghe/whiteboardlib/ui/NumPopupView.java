package com.yinghe.whiteboardlib.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.yinghe.whiteboardlib.R;
import com.yinghe.whiteboardlib.utils.DrawConsts;
import com.yinghe.whiteboardlib.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:数字键弹框
 *
 * @author Administrator
 * @time 2017/6/21.
 */
public class NumPopupView {
    int pupWindowsDPWidth = 300;//弹窗宽度，单位DP
    int menuPupWindowsDPHeight = 180;//画笔弹窗高度，单位DP

    private Activity activity;
    private TextView textView;

    private PopupWindow numPopupWindow;

    private List<String> mList;

    private ConfirmCallback mConfirmCallback;
    private CancelCallback mCancelCallback;

    public NumPopupView(Activity activity, TextView textView) {
        this.activity = activity;
        this.textView = textView;

        mList = new ArrayList<>();
        initView();
    }

    public void setConfirmCallback(ConfirmCallback confirmCallback) {
        mConfirmCallback = confirmCallback;
    }

    public void setCancelCallback(CancelCallback cancelCallback) {
        mCancelCallback = cancelCallback;
    }

    private void initView(){
        // 菜单弹框布局
        View numPopupLayout = activity.getLayoutInflater().inflate(R.layout.pop_nums_layout, null);
        TextView num1 = (TextView) numPopupLayout.findViewById(R.id.nums_1);
        TextView num2 = (TextView) numPopupLayout.findViewById(R.id.nums_2);
        TextView num3 = (TextView) numPopupLayout.findViewById(R.id.nums_3);
        TextView num4 = (TextView) numPopupLayout.findViewById(R.id.nums_4);
        TextView num5 = (TextView) numPopupLayout.findViewById(R.id.nums_5);

        TextView num6 = (TextView) numPopupLayout.findViewById(R.id.nums_6);
        TextView num7 = (TextView) numPopupLayout.findViewById(R.id.nums_7);
        TextView num8 = (TextView) numPopupLayout.findViewById(R.id.nums_8);
        TextView num9 = (TextView) numPopupLayout.findViewById(R.id.nums_9);
        TextView num0 = (TextView) numPopupLayout.findViewById(R.id.nums_0);

        TextView numBack = (TextView) numPopupLayout.findViewById(R.id.nums_back);

        Button confirm = (Button) numPopupLayout.findViewById(R.id.positiveButton);
        Button cancel = (Button) numPopupLayout.findViewById(R.id.negativeButton);

        // 设置监听
        num1.setOnClickListener(mOnClickListener);
        num2.setOnClickListener(mOnClickListener);
        num3.setOnClickListener(mOnClickListener);
        num4.setOnClickListener(mOnClickListener);
        num5.setOnClickListener(mOnClickListener);

        num6.setOnClickListener(mOnClickListener);
        num7.setOnClickListener(mOnClickListener);
        num8.setOnClickListener(mOnClickListener);
        num9.setOnClickListener(mOnClickListener);
        num0.setOnClickListener(mOnClickListener);

        numBack.setOnClickListener(mOnClickListener);
        confirm.setOnClickListener(mOnClickListener);
        cancel.setOnClickListener(mOnClickListener);

        //菜单弹窗
        numPopupWindow = new PopupWindow(activity);
        numPopupWindow.setContentView(numPopupLayout);//设置主体布局

        // 设置弹窗大小
        numPopupWindow.setWidth(ScreenUtils.dip2px(activity, pupWindowsDPWidth));//宽度
        numPopupWindow.setHeight(ScreenUtils.dip2px(activity, menuPupWindowsDPHeight));//高度
        numPopupWindow.setFocusable(true);
        numPopupWindow.setBackgroundDrawable(new BitmapDrawable());//设置空白背景
        numPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画
    }

    /**
     * 显示数字键弹框
     *
     * @param anchor
     */
    public void showNumPopupWindow(View anchor) {
        numPopupWindow.showAsDropDown(anchor, ScreenUtils.dip2px(activity, pupWindowsDPWidth + 10), ScreenUtils.dip2px(activity, DrawConsts.POPUP_WIN_YOFF + 20));
    }

    public void dismiss() {
        numPopupWindow.dismiss();
    }

    private String getString(){
        StringBuffer stringBuffer = new StringBuffer();
        if (!mList.isEmpty()){
            for (String s:mList){
                stringBuffer.append(s);
            }
        }

        return stringBuffer.toString();
    }

    /**
     * 确认按钮监听事件
     */
    @FunctionalInterface
    public interface ConfirmCallback{
        void onClick();
    }

    /**
     * 取消按钮监听事件
     */
    @FunctionalInterface
    public interface CancelCallback{
        void onClick();
    }

    /**
     * 点击事件监听
     */
    private View.OnClickListener mOnClickListener = v -> {
        int id = v.getId();

        if (id == R.id.negativeButton){// 取消按钮
            LogUtils.d("取消");
            mList.clear();
            numPopupWindow.dismiss();
            textView.setText("");

            if (mCancelCallback != null){
                mCancelCallback.onClick();
            }
        } else if (id == R.id.positiveButton){// 确认按钮
            LogUtils.d("确认");
            numPopupWindow.dismiss();

            if (mConfirmCallback != null){
                mConfirmCallback.onClick();
            }
        } else if (id == R.id.nums_back){// 回退键
            LogUtils.d("退格");
            if (!mList.isEmpty()){
                int len = mList.size();
                mList.remove(len - 1);

                String string = getString();
                textView.setText(string);
            }
        } else {// 数字键
//            LogUtils.d("数字键");
            if (v instanceof TextView){
                TextView nums = (TextView) v;
                String numStr = nums.getText().toString();
                mList.add(numStr);

                String string = getString();
                textView.setText(string);
            }
        }
    };
}

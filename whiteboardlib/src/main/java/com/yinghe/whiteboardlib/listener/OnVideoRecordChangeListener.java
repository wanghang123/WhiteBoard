package com.yinghe.whiteboardlib.listener;

/**
 * 视频区域变换
 *
 * @author wang
 * @time on 2017/3/23.
 */
public interface OnVideoRecordChangeListener {
    void videoTranslate(String videoID, float dx, float dy);
    void deleteRecord(String videoID);
}

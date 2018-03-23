package com.yinghe.whiteboardlib.helper;

import android.content.Context;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.yinghe.whiteboardlib.utils.ACache;
import com.yinghe.whiteboardlib.utils.CommConsts;
import com.yinghe.whiteboardlib.utils.TimeUtils;

/**
 * Desc:文件保存帮助类
 *
 * @author wang
 * @time 2017/7/15.
 */
public class SaveFileHelper {

    /**
     * 创建文件夹(会议记录)名称
     * @param context
     * @return
     */
    public String createMeetingDirName(Context context) {
        String mmdd = TimeUtils.getNowTime(TimeUtils.DATE_PATTERN_MMDD);
        String saveDirName = String.format(CommConsts.MEETING_DIR_NAME, mmdd, "");

        ACache aCache = ACache.get(context);
        String nextDir = aCache.getAsString(CommConsts.KEY_NEXT_MEETING_DIR);
        LogUtils.d("createMeetingDirName nextDir->%s", nextDir);

        // 检测缓存的名称是否有效
        if (!TextUtils.isEmpty(nextDir)
                && nextDir.contains(saveDirName)){ // 若保存的名称有效，则使用保存的名称
            return nextDir;
        } else {  // 否则使用默认的名字，并保存默认名称
            saveDirName += "01";
            aCache.put(CommConsts.KEY_NEXT_MEETING_DIR, saveDirName);
        }
        return saveDirName;
    }

    /**
     * 保存下一个会议记录的文件夹名称
     *
     * @param context
     * @param saveNameCurrent
     */
    public void saveNextMeetingDirName(Context context, String saveNameCurrent) {
        // 获得缓存中名称
        ACache aCache = ACache.get(context);
        String dirCache = aCache.getAsString(CommConsts.KEY_NEXT_MEETING_DIR);

        // 若没有使用缓存中名称，则直接返回
        if (!TextUtils.equals(saveNameCurrent, dirCache)){
            return;
        }

        // 若使用缓存中名称，则创建下一个缓存名称
        String mmdd = TimeUtils.getNowTime(TimeUtils.DATE_PATTERN_MMDD);
        String saveDirName = String.format(CommConsts.MEETING_DIR_NAME, mmdd, "");

        // 递增保存会议记录的(下一个)文件夹名称
        if (!TextUtils.isEmpty(dirCache)
                && dirCache.contains(saveDirName)){
            // 获得当前序列
            String seqCurrent = dirCache.replace(saveDirName, "");
            try{
                int seq = Integer.valueOf(seqCurrent);
                if (seq < 0){
                    return;
                }

                seq += 1;

                // 获得下一个序列
                String seqNext;
                if (seq < 10){
                    seqNext = "0" + seq;
                } else {
                    seqNext = "" + seq;
                }

                // 保存下一个目录名
                String dirNext = saveDirName + seqNext;
                aCache.put(CommConsts.KEY_NEXT_MEETING_DIR, dirNext);
                LogUtils.d("saveNextMeetingDirName dirNext->%s", dirNext);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

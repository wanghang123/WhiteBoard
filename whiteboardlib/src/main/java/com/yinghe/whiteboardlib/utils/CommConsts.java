package com.yinghe.whiteboardlib.utils;

import android.os.Environment;

/**
 * 常量
 *
 * @author wang
 * @time on 2017/3/22.
 */
public interface CommConsts {
    String VIDEO_UNSPECIFIED = "video/*";

    // 测试视频地址
    String TEST_VIDEO_FILE_PATH = Environment.getExternalStorageDirectory().toString() + "/testVideo/wlb2.mp4";

    // 绘制内容图片保存的文件夹
    String FILE_PATH_LOCAL_DIR = Environment.getExternalStorageDirectory().toString() + "/WhiteBoard/";
    String FILE_PATH_SHARE_FILES_DIR = Environment.getExternalStorageDirectory().toString() + "/WhiteBoard/share_files";

    String MEETING_DIR = "/meeting";

    float SIMPLE_SCALE = 0.5f;//图片载入的缩放倍数

    // 视频格式后缀
    String[] VIDEO_SUFFIXS = new String[]{"avi", "rmvb", "rm", "asf" ,"divx", "mpg", "mpeg", "mpe", "wmv", "mp4", "mkv", "vob"};
    // office/wps文档后缀
    String[] DOC_SUFFIXS = new String[]{"doc", "docx", "xls", "xlsx" ,"ppt", "pptx", "txt", "pdf", "wps", "dps", "et"};

    String[] IMG_SUFFIXS = new String[]{"png", "jpg", "jpeg"};

    // 画板数量的最大值
    int PAGES_MAX = 5;

    // 服务器的URL地址
//    String BASE_URL = "http://yun11.luckshow.cn:8099/pot/";
    String BASE_URL = "http://yun8.luckshow.cn/pot/";
    String UPDLOAD_URL = BASE_URL + "upload";
    String DOWN_URL = BASE_URL + "down";
    String SIZE_URL = BASE_URL + "size";
    String SHOW_URL = BASE_URL + "show";
    String SHARE_URL = BASE_URL + "share";
    String PWD_URL = BASE_URL + "pwd";

    // 缓存的键值
    String KEY_MD5_FILE_NAME = "md5filename";
    String KEY_MEETING_SEQ = "MeetingSeq";

    // 会议记录的文件夹名
    String MEETING_DIR_NAME = "%s会议记录%s";
    String KEY_NEXT_MEETING_DIR = "KEY_NEXT_MEETING_DIR";
}

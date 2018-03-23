package com.yinghe.whiteboardlib.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.List;

/**
 * 调用系统接口
 *
 * @author wang
 * @time on 2017/3/14.
 */
public class AppUtils {
    /*
	 * 调用系统设置app函数
	 */
    public static void callSysSettingApp(Context mContext) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.Settings");
        intent.setComponent(cn);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
//            ToastUtils.showLongMessage(mContext, R.string.str_no_apk);
        }
    }

    /**
     * 回到桌面
     *
     * @param mContext
     */
    public static void gotoHome(Context mContext){
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(home);
    }

    /**
     * 使用WPS打开文档
     *
     * @param activity
     * @param path
     * @return
     */
    public static boolean openDocFileByWPS(Activity activity, String path) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("OpenMode", "ReadOnly");
        bundle.putBoolean("SendCloseBroad", true);
        bundle.putString("ThirdPackage", activity.getApplication().getPackageName());
        bundle.putBoolean("ClearBuffer", true);
        bundle.putBoolean("ClearTrace", true);
        bundle.putBoolean("ClearFile", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");

        // 设置数据
        LogUtils.d("path -->%s",path );

        File file = new File(path);
        if (file == null || !file.exists())
        {                        return false;
        }

        LogUtils.d("Uri");
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        intent.putExtras(bundle);
        try {
            LogUtils.d("打开wps");
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            LogUtils.d("请先安装wps！");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 用来判断服务是否运行.
     * @param mContext
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 保存完图片之后，可以在图库中查看
     *
     * @param context
     * @param filePath
     */
    public static void noticeMediaScan(Context context, String filePath){
        //发送Sd卡的就绪广播,要不然在手机图库中不存在
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(new File(filePath));
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + filePath)));
        }
    }

    /**
     * 安装APP
     * @param context
     * @param appFilePath
     */
    public static void installAPK(Context context, String appFilePath){
        // 下载完成,弹出窗口安装方式
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + appFilePath),
                "application/vnd.android.package-archive");
        context.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 检测APP是否与硬件匹配
     */
    public static void checkPermission(){
        // 检测是否有meeting文件夹
//        File meetingDir = new File(CommConsts.MEETING_DIR);
//        if (!meetingDir.exists()){
//            android.os.Process.killProcess(android.os.Process.myPid());
//        }
    }

    /**
     * 获得缩放的规格
     *
     * @param imagePath
     * @param width
     * @param height
     * @return
     */
    public static BitmapFactory.Options getOptions(String imagePath, float width, float height){
        BitmapFactory.Options op = new BitmapFactory.Options();
        //inJustDecodeBounds
        //If set to true, the decoder will return null (no bitmap), but the out…
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, op); //获取尺寸信息

        int wRatio = (int)Math.ceil(op.outWidth/ width);
        int hRatio = (int)Math.ceil(op.outHeight/ height);
        //如果超出指定大小，则缩小相应的比例
        if(wRatio > 1 && hRatio > 1){
            if(wRatio > hRatio){
                op.inSampleSize = wRatio;
            }else{
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;

        return op;
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * @param nextVersion
     * @param currentVersion
     * @return
     */
    public static int compareVersion(String nextVersion, String currentVersion) throws Exception {
        if (nextVersion == null || currentVersion == null) {
            throw new Exception("compareVersion error:illegal params.");
        }
        String[] versionArray1 = nextVersion.split("\\.");//注意此处为正则匹配，不能用"."；
        String[] versionArray2 = currentVersion.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
        int diff = 0;
        while (idx < minLength
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
                && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符
            ++idx;
        }
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }


}

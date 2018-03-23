package com.protruly.whiteboard;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.protruly.permissions.AfterPermissionGranted;
import com.protruly.permissions.EasyPermissions;
import com.protruly.permissions.PermissionConsts;
import com.protruly.whiteboard.entity.UpdateInfo;
import com.protruly.whiteboard.ui.DownLoading;
import com.yinghe.whiteboardlib.callback.BeanCallBack;
import com.yinghe.whiteboardlib.fragment.WhiteBoardFragment;
import com.yinghe.whiteboardlib.utils.AppUtils;
import com.yinghe.whiteboardlib.utils.MD5Util;
import com.yinghe.whiteboardlib.utils.NetUtil;
import com.yinghe.whiteboardlib.utils.ScreenUtils;
import com.yinghe.whiteboardlib.utils.ViewUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 主窗口
 *
 * @author wanghang
 * @date 2017/03/3
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String mDownloadDir = Environment.getExternalStorageDirectory().getPath() + "/";

    private WhiteBoardFragment whiteBoardFragment;// 画板

    private DownLoading downLoading;
    private String versionName; //版本号

    private static String updateAPPUrl = "";// 下载最新APK的地址
    private static String updateAPPMd5 = "";// 下载最新APK的MD5值

    public final static String KEY_FRAGMENT_TAG = "wb";// fragment的tag

    public static Handler mHandler;

    private boolean isExit = false;// 是否退出的标识

    // 对话框UI
    Dialog dialog;
    TextView mTitle;
    TextView mMessage;
    EditText mEditText;
    Button mConfirm; //确定按钮
    Button mCancel; //取消按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!BuildConfig.DEBUG){
            AppUtils.checkPermission();
        }
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);

        LogUtils.d("释放whiteBoardFragment");
        FragmentTransaction ts = getSupportFragmentManager().beginTransaction();
        ts.remove(whiteBoardFragment);

        mHandler.removeCallbacksAndMessages(null);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 初始化操作
     */
    private void init(){
        mHandler = new UIHandler(this);
        showMainUI();

        // 检查版本号
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            LogUtils.d("versionName-->%s", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "";
        }

        //升级
        downLoading = (DownLoading) findViewById(R.id.downloading);
        checkAppUpdate();
    }

    /**
     * 从服务器端读取版本号相关信息
     * 检查是否需要升级
     *
     */
    private void checkAppUpdate() {
        if (NetUtil.NETWORK_NONE == NetUtil.getNetWorkState(getApplicationContext())){
            LogUtils.i("网络连接异常，请检查网络。");
            return;
        }

        // 获得HTTP回调
        BeanCallBack<UpdateInfo> beanCallBack = new BeanCallBack<UpdateInfo>() {
            @Override
            public void onError(Call call, Exception e, int id) {
                LogUtils.i("查询远程版本失败..");
//                e.printStackTrace();
            }

            @Override
            public void onResponse(UpdateInfo updateInfo, int id) {
                if (null == updateInfo){
                    return;
                }

                // 检测版本不一致
                if (checkVersionNotEquals(updateInfo, versionName)) {
                    updateAPPMd5 = updateInfo.getMd5();
                    showUpdateDialog(updateInfo);
                }
            }
        };

        // 获得参数
        final String apkFileName = getResources().getString(R.string.apk_file_name).toLowerCase();
        final String checkAPPUrl = this.getResources().getString(R.string.check_apk_url);

        // 检查是否需要升级
        Map<String,String> params = new HashMap<>();
        params.put("name", apkFileName);
        params.put("version", versionName);
        OkHttpUtils.get()
                .params(params)
                .url(checkAPPUrl)
                .tag(this)
                .addHeader("Accept-Encoding", "utf-8")
                .build()
                .execute(beanCallBack);
    }

    /**
     * 检测版本不一致
     * @param updateInfo
     * @return
     */
    private boolean checkVersionNotEquals(UpdateInfo updateInfo, String versionName){
        LogUtils.i("updateInfo->%s",updateInfo.toString());

        boolean flag = false;
        String tmpAppName = updateInfo.getName().toLowerCase();
        String tmpVersionName = updateInfo.getVersion();
        updateAPPUrl = updateInfo.getDownload();
        LogUtils.i("updateAPPUrl->%s",updateAPPUrl);

        if (TextUtils.isEmpty(tmpVersionName)){
            return flag;
        }

        try{
            float tmpVersion = Float.valueOf(tmpVersionName);
            float version = Float.valueOf(versionName);
            final String apkName = getResources().getString(R.string.apk_name).toLowerCase();
            // 检测版本是否一致
            flag = (tmpVersion > version)
                    && !TextUtils.isEmpty(updateAPPUrl)
                    && tmpAppName.contains(apkName)
                    && (tmpAppName.lastIndexOf(".apk") > 0);
        } catch (Exception e){
            e.printStackTrace();
            return flag ;
        }

        return flag ;
    }

    /**
     * 显示升级对话框
     */
    private void showUpdateDialog(UpdateInfo updateInfo) {
        mEditText.setVisibility(View.GONE);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.update_title);
        String msg = getResources().getString(R.string.update_message, updateInfo.getVersion(), updateInfo.getMessage());
        mMessage.setText(msg);
        mMessage.setVisibility(View.VISIBLE);

        LogUtils.d("升级对话框");
        mConfirm.setOnClickListener(v -> {
            // 防止快速重复点击
            if (ViewUtils.isFastDoubleClick()){
                Toast.makeText(MainActivity.this, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
                return;
            }

            // 开启服务进行更新
            LogUtils.i("升级..");
            updateAppTask();
            showDownLoading();

            dialog.dismiss();
        });

        mCancel.setOnClickListener(v -> {
            // 防止快速重复点击
            if (ViewUtils.isFastDoubleClick()){
                Toast.makeText(MainActivity.this, getResources().getString(R.string.fast_double_click_tip), Toast.LENGTH_SHORT).show();
                return;
            }

            LogUtils.i("取消升级..");
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * 下载apk文件
     *
     * @param appUrl
     */
    private void downloadAPKFile(String appUrl) {
        final String apkFileName = getResources().getString(R.string.apk_file_name);
        OkHttpUtils.get().url(appUrl).tag(this).build().execute(new FileCallBack(mDownloadDir, apkFileName) {
            @Override
            public void onError(Call call, Exception e, int id) {
                // 更新进度
                if (MainActivity.mHandler != null){
                    Message message = MainActivity.mHandler.obtainMessage(1, -1);
                    MainActivity.mHandler.sendMessage(message);
                }
            }

            @Override
            public void onResponse(File file, int id) {
                try{
                    // 比较文件的md5值
                    String md5Str = MD5Util.getFileMD5(file);
                    if (updateAPPMd5.equals(md5Str)){
                        LogUtils.d("下载完成安装APP的md5一致");
                        // 下载完成安装APP
                        AppUtils.installAPK(MainActivity.this, file.getAbsolutePath());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void inProgress(float progress, long total, int id) {
                //progress*100为当前文件下载进度，total为文件大小
                // 更新下载进度
                int tmpProgress = (int) (progress * 100);
                if (tmpProgress % 5 == 0) {
                    // 更新进度
                    if (MainActivity.mHandler != null){
                        Message message = MainActivity.mHandler.obtainMessage(1, tmpProgress);
                        MainActivity.mHandler.sendMessage(message);
                    }
                }
            }
        });
    }

    /**
     * 进度条进度刷新
     */
    private void showDownLoading() {
        downLoading.setVisibility(View.VISIBLE);
    }

    /**
     * 更新进度条
     *
     * @param process
     */
    private void updateProcess(int process){
        downLoading.setProgress(process);
        if (100 == process) {
            downLoading.finishLoad();
        }
        if (-1 == process) {
            downLoading.setStop(true);
        }
    }

    /**
     * 显示主界面
     */
    private void showMainUI(){
        FragmentTransaction ts = getSupportFragmentManager().beginTransaction();
        whiteBoardFragment = WhiteBoardFragment.newInstance();
        ts.replace(R.id.fl_main, whiteBoardFragment, KEY_FRAGMENT_TAG).commit();

        initDialogView();// 初始化对话框UI
    }

    /**
     * 初始化对话框UI
     */
    private void initDialogView(){
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null);
        mTitle = (TextView) dialogView.findViewById(R.id.title);
        mEditText = (EditText) dialogView.findViewById(R.id.message_edit);
        mMessage = (TextView) dialogView.findViewById(R.id.message);
        mConfirm = (Button) dialogView.findViewById(R.id.positiveButton);
        mCancel = (Button) dialogView.findViewById(R.id.negativeButton);

        dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);

        // 设置对话框的大小
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = ScreenUtils.dip2px(this, 400); // 宽度
        lp.height = ScreenUtils.dip2px(this, 300); // 高度

        dialogWindow.setAttributes(lp);
    }

    /**
     * 下载apk任务
     */
    @AfterPermissionGranted(PermissionConsts.REQUEST_STORAGE)
    private void updateAppTask(){
        String[] permissins =  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, permissins)) {
            downloadAPKFile(updateAPPUrl);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    PermissionConsts.REQUEST_STORAGE, permissins);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this,
                getString(R.string.rationale_ask_again),
                R.string.setting, R.string.cancel, perms);
    }

    /**
     * UI异步处理
     */
    public static final class UIHandler extends Handler {
        WeakReference<MainActivity> weakReference;

        public UIHandler(MainActivity activity) {
            super();
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();

            if (activity == null || activity.isFinishing()) {
                return;
            }

            // 开始处理更新UI
            switch (msg.what){
                case 0:// 退出
                    activity.isExit = false;
                    break;
                case 1:
                    int process = (int)msg.obj;
                    activity.updateProcess(process);
                    break;
                default:
                    break;
            }
        }
    }
}

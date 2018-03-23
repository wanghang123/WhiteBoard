package com.protruly.fileselect;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author Administrator
 * @time 2017/5/18.
 */
public class FileUtils {
    private final static String TAG = FileUtils.class.getSimpleName();
    public static List<String> getMountPathList() {
        List<String> pathList = new ArrayList<String>();
        final String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();//取得当前JVM的运行时环境
        try {
            Process p = run.exec(cmd);//执行命令
            BufferedInputStream inputStream = new BufferedInputStream(p.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                Log.i(TAG,line);
                //输出信息内容：  /data/media /storage/emulated/0 sdcardfs rw,nosuid,nodev,relatime,uid=1023,gid=1023 0 0
                String[] temp = TextUtils.split(line," ");
                //分析内容可看出第二个空格后面是路径
                String result = temp[1];
                File file = new File(result);
                //类型为目录、可读、可写，就算是一条挂载路径
                if (file.isDirectory() && file.canRead() && file.canWrite()) {
                    Log.d(TAG,file.getAbsolutePath());
                    pathList.add(result);
                }

                // 检查命令是否执行失败
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    Log.e(TAG,"命令执行失败!");
                }
            }
            bufferedReader.close();
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG,e.toString());
            //命令执行异常，就添加默认的路径
            pathList.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        return pathList;
    }

    public static String[] getVolumePaths(Context context){
        try {
            StorageManager  sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            String[] volumePaths = (String[]) sm.getClass().getMethod("getVolumePaths", new  Class[0]).invoke(sm, new  Object[]{});
            return volumePaths;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

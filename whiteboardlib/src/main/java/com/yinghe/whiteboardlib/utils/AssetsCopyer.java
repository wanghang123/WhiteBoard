package com.yinghe.whiteboardlib.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 复制asset中的文件
 *
 * @author wang
 * @time on 2017/3/24.
 */
public class AssetsCopyer {
    private static final String TAG = "AssetsCopyer";

    public static void releaseAssets(Context context, String assetsDir,
                                     String releaseDir) {

//      Log.d(TAG, "context: " + context + ", " + assetsDir);
        if (TextUtils.isEmpty(releaseDir)) {
            return;
        } else if (releaseDir.endsWith("/")) {
            releaseDir = releaseDir.substring(0, releaseDir.length() - 1);
        }

        if (TextUtils.isEmpty(assetsDir) || assetsDir.equals("/")) {
            assetsDir = "";
        } else if (assetsDir.endsWith("/")) {
            assetsDir = assetsDir.substring(0, assetsDir.length() - 1);
        }

        AssetManager assets = context.getAssets();
        try {
            String[] fileNames = assets.list(assetsDir);//只能获取到文件(夹)名,所以还得判断是文件夹还是文件
            if (fileNames.length > 0) {// is dir
                for (String name : fileNames) {
                    if (!TextUtils.isEmpty(assetsDir)) {
                        name = assetsDir + File.separator + name;//补全assets资源路径
                    }
//                    Log.i(, brian name= + name);
                    String[] childNames = assets.list(name);//判断是文件还是文件夹
                    if (!TextUtils.isEmpty(name) && childNames.length > 0) {
                        checkFolderExists(releaseDir + File.separator + name);
                        releaseAssets(context, name, releaseDir);//递归, 因为资源都是带着全路径,
                        //所以不需要在递归是设置目标文件夹的路径
                    } else {
                        InputStream is = assets.open(name);
//                        FileUtil.writeFile(releaseDir + File.separator + name, is);
                        writeFile(releaseDir + File.separator + name, is);
                    }
                }
            } else {// is file
                InputStream is = assets.open(assetsDir);
                // 写入文件前, 需要提前级联创建好路径, 下面有代码贴出
//                FileUtil.writeFile(releaseDir + File.separator + assetsDir, is);
                writeFile(releaseDir + File.separator + assetsDir, is);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean writeFile(String fileName, InputStream in) throws IOException
    {
        boolean bRet = true;
        try {
            OutputStream os = new FileOutputStream(fileName);
            byte[] buffer = new byte[4112];
            int read;
            while((read = in.read(buffer)) != -1)
            {
                os.write(buffer, 0, read);
            }
            in.close();
            in = null;
            os.flush();
            os.close();
            os = null;
//          Log.v(TAG, "copyed file: " + fileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bRet = false;
        }
        return bRet;
    }

    private static void checkFolderExists(String path)
    {
        File file = new File(path);
        if((file.exists() && !file.isDirectory()) || !file.exists())
        {
            file.mkdirs();
        }
    }

    /**
     * 复制单个文件
     *
     * @param myContext
     * @param assetsName 要复制的文件名
     * @param savePath 要保存的路径
     *  testCopy(Context context)是一个测试例子。
     */
    public static void copy(Context myContext, String assetsName,
                            String savePath) {
        String filename = savePath;

        File file = new File(savePath);
        // 如果目录不中存在，创建这个目录
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if (file.exists()){
            return;
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = myContext.getResources().getAssets()
                    .open(assetsName);
            fos = new FileOutputStream(filename);
            byte[] buffer = new byte[7168];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void testCopy(Context context) {
        String path = context.getFilesDir().getAbsolutePath() + "test.txt";
        String name="test.txt";
        copy(context, name, path);
    }
}


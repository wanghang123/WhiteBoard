package com.yinghe.whiteboardlib.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 文件操作类
 * Created by Nereo on 2015/4/8.
 */
public class FileUtils {

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    public static File createTmpFile(Context context) throws IOException{
        File dir = null;
        if(TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (!dir.exists()) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
                if (!dir.exists()) {
                    dir = getCacheDirectory(context, true);
                }
            }
        }else{
            dir = getCacheDirectory(context, true);
        }
        return File.createTempFile(JPEG_FILE_PREFIX, JPEG_FILE_SUFFIX, dir);
    }


    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir;
            }
        }
        return individualCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 将URI转换为文件路径
     *
     * @param context
     * @param contentUri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = null;
//        String[] proj = { MediaStore.Images.Media.DATA };
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(columnIndex);
        }
        cursor.close();
        return res;
    }

    public static String getPhotoPathFromContentUri(Context context, Uri uri) {
        String photoPath = "";
        if(context == null || uri == null) {
            return photoPath;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if(isExternalStorageDocument(uri)) {
                String [] split = docId.split(":");
                if(split.length >= 2) {
                    String type = split[0];
                    if("primary".equalsIgnoreCase(type)) {
                        photoPath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            }
            else if(isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                photoPath = getDataColumn(context, contentUri, null, null);
            }
            else if(isMediaDocument(uri)) {
                String[] split = docId.split(":");
                if(split.length >= 2) {
                    String type = split[0];
                    Uri contentUris = null;
                    if("image".equals(type)) {
                        contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    else if("video".equals(type)) {
                        contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }
                    else if("audio".equals(type)) {
                        contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[] { split[1] };
                    photoPath = getDataColumn(context, contentUris, selection, selectionArgs);
                }
            }
        }
        else if("file".equalsIgnoreCase(uri.getScheme())) {
            photoPath = uri.getPath();
        }
        else {
            photoPath = getDataColumn(context, uri, null, null);
        }

        return photoPath;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return null;
    }

    /**
     * 判断文件是否为视频格式
     *
     * @param filePath
     * @return
     */
    public static boolean isVideoFile(String filePath){
        // 判断是否有后缀
        int endIndex = filePath.lastIndexOf(".");
        if (endIndex <= 0){
            return false;
        }

        // 判断是否为视频格式
        String suffix = filePath.substring(endIndex + 1);
        List<String> list = java.util.Arrays.asList(CommConsts.VIDEO_SUFFIXS);
        return list.contains(suffix.toLowerCase());
    }

    /**
     * 判断文件是否为视频格式
     *
     * @param filePath
     * @return
     */
    public static boolean isDOCFile(String filePath){
        // 判断是否有后缀
        int endIndex = filePath.lastIndexOf(".");
        if (endIndex <= 0){
            return false;
        }

        // 判断是否为视频格式
        String suffix = filePath.substring(endIndex + 1);
        List<String> list = java.util.Arrays.asList(CommConsts.DOC_SUFFIXS);
        return list.contains(suffix.toLowerCase());
    }

    /**
     * 判断文件是否为视频格式
     *
     * @param filePath
     * @return
     */
    public static boolean isImageFile(String filePath){
        // 判断是否有后缀
        int endIndex = filePath.lastIndexOf(".");
        if (endIndex <= 0){
            return false;
        }

        // 判断是否为视频格式
        String suffix = filePath.substring(endIndex + 1);
        List<String> list = java.util.Arrays.asList(CommConsts.IMG_SUFFIXS);
        return list.contains(suffix.toLowerCase());
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file
     *            要删除的根目录
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) { // 文件不存在时
            return false;
        } else {
            // 若是文件，则直接删除
            if (file.isFile()) {
                file.delete();
                return true;
            }

            // 若是文件夹，则递归删除
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                // 没有子文件，直接删除文件夹
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return true;
                }

                // 有子文件，递归删除子文件
                for (File f : childFile) {
                    deleteFile(f);
                }

                // 最后删除文件夹
                file.delete();
                return true;
            }

            return false;
        }
    }

    /**
     * 不删除文件目录，只递归删除文件夹下的子文件
     *
     * @param dir 目录
     */
    public static boolean deleteChildFiles(File dir) {
        if (dir == null || !dir.exists()) { // 文件不存在时
            return false;
        } else {
            // 若是文件夹，则递归删除
            if (dir.isDirectory()) {
                File[] childFile = dir.listFiles();
                // 没有子文件，直接删除文件夹
                if (childFile == null || childFile.length == 0) {
                    return true;
                }

                // 有子文件，递归删除子文件
                for (File f : childFile) {
                    if (f.isDirectory()){
                        deleteChildFiles(f);
                    } else {
                        f.deleteOnExit();
                    }
                }

                return true;
            }

            return false;
        }
    }

}

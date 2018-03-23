package com.yinghe.whiteboardlib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5帮助类
 * Created by wlb on 2017/4/28.
 */
public class MD5Util {
    /**
     * get file md5
     * @param file
     * @return 文件存在返回MD5码,文件不存在返回空.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String getFileMD5(File file) throws NoSuchAlgorithmException, IOException {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        digest = MessageDigest.getInstance("MD5");
        in = new FileInputStream(file);
        while ((len = in.read(buffer, 0, 1024)) != -1) {
            digest.update(buffer, 0, len);
        }
        in.close();
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();

        for(int i = 0; i < bytes.length; ++i) {
            int digital = bytes[i];
            if(digital < 0) {
                digital += 256;
            }

            if(digital < 16) {
                md5str.append("0");
            }

            md5str.append(Integer.toHexString(digital));
        }

        return md5str.toString().toUpperCase();
    }

    public static String bytesToMD5(byte[] input) {
        String md5str = null;

        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            byte[] buff = e.digest(input);
            md5str = bytesToHex(buff);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return md5str;
    }

    public static String FileItemToMD5(File item) {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(item);
            byte[] buffer = new byte[8192];
            boolean len = false;

            int len1;
            while((len1 = in.read(buffer)) > 0) {
                e.update(buffer, 0, len1);
            }

            in.close();
            return bytesToHex(e.digest());
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static String fileToMD5(File file) {
        if(file == null) {
            return null;
        } else if(!file.exists()) {
            return null;
        } else if(!file.isFile()) {
            return null;
        } else {
            FileInputStream fis = null;

            try {
                MessageDigest e = MessageDigest.getInstance("MD5");
                fis = new FileInputStream(file);
                byte[] buff = new byte[1024];
                boolean len = false;

                while(true) {
                    int len1 = fis.read(buff, 0, buff.length);
                    if(len1 == -1) {
                        fis.close();
                        return bytesToHex(e.digest());
                    }

                    e.update(buff, 0, len1);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
                return null;
            }
        }
    }

    public static String strToMD5(String str) {
        byte[] input = str.getBytes();
        return bytesToMD5(input);
    }
}

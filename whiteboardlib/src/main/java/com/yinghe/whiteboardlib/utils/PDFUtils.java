package com.yinghe.whiteboardlib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Desc:PDF生成工具
 *
 * @author wang
 * @time 2017/6/14.
 */
public class PDFUtils {
    /**
     * 通过多张图片创建一个PDF文件
     *
     * @param imageDir
     */
    public static boolean createPDFByMutiImage(String imageDir) {
        boolean createFlag = false;

        // 判断是否为文件夹
        File dir = new File(imageDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return createFlag;
        }

        // 判断是否有子文件
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return createFlag;
        }

        // 生成PDF文件
        //output file path
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            String dest = imageDir + ".pdf";
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
            Rectangle pageSize = writer.getPageSize();
            float width = pageSize.getWidth();
            float height = pageSize.getHeight();

            // step 3: we open the document
            document.open();

            // step 4: we open the document
            // add image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Image jpg = null;
            int len = files.length;
            File file = null;
            Bitmap bitmap = null;
            String path = "";

            // 添加图片数据
            //获取比例大小
            final float DISPLAY_WIDTH = width;
            final float DISPLAY_HEIGHT = height;
//            LogUtils.d("DISPLAY_WIDTH -->%s, DISPLAY_HEIGHT-->%s", DISPLAY_WIDTH, DISPLAY_HEIGHT);
            BitmapFactory.Options op = null;
            Paragraph paragraph = null;

            for (int i = 0; i < len; i++) {
                file = files[i];
                path = file.getAbsolutePath();

                // 判断是否为图片
                if (!FileUtils.isImageFile(path)) {
                    continue;
                }

                stream.reset();

                // 添加图片
                op = AppUtils.getOptions(path, DISPLAY_WIDTH, DISPLAY_HEIGHT);
                bitmap = BitmapFactory.decodeFile(path, op);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                jpg = Image.getInstance(stream.toByteArray());
                jpg.setAlignment(Image.MIDDLE);
//                jpg.scaleAbsolute(700, 350);
                document.add(jpg);

                // 添加文字显示
                paragraph = new Paragraph("" + (i + 1) + ".jpg");
                paragraph.setAlignment(Image.MIDDLE);
                document.add(paragraph);

                // 若每两个图片，并且不是最后一个，则创建下一页
                if ((i != 0)
                        && (i % 2 != 0)
                        && (i != len - 1)) {
                    document.newPage();
                }
            }

            createFlag = true;
            return createFlag;
        } catch (Exception e) {
            e.printStackTrace();
            createFlag = false;
            return createFlag;
        } finally {
            try {
                // step 4: Close document
                document.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}

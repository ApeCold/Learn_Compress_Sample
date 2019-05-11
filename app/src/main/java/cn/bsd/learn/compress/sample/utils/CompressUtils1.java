package cn.bsd.learn.compress.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressUtils1 {
    /**
     * 压缩图片文件到bitmap
     *
     * @param filePath 图片文件地址
     * @param quality  要压缩的质量
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int quality) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        //计算采样，现在主流手机比较多是800*400分辨率
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        //用样本大小集解码位图
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            return null;
        }

        //读取图片角度
        int degree = readPictureDegree(filePath);
        //旋转位图
        bm = rotateBitmap(bm, degree);
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWith, int reqHeight) {
        //Raw height and width of icon
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWith) {
            //Calculate ratios of height and width to requested height and
            //width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWith);

            //Choose ths smallest ratio as inSampleSize value,this will
            //guarantee
            //a final icon with both dimensions larger than or equal to the
            //requested height and width.
            inSampleSize = heightRatio < widthRatio ? widthRatio : widthRatio;
        }
        return inSampleSize;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        //Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    private static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * Bitmap对象保存成图片文件
     * @param bitmap
     * @param filePath
     */
    public static void saveBitmapFile(Bitmap bitmap,String filePath){
        File file = new File(filePath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File saveImageFile(Context context,Bitmap bitmap,
                                     String fileName){
        if (fileName==null) {
            System.out.println("save fileName can not be null");
            return null;
        }
        fileName = fileName+".png";
        String path= context.getFilesDir().getAbsolutePath();
        String lastFilePath = path+"/"+fileName;
        File file = new File(lastFilePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName,Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}

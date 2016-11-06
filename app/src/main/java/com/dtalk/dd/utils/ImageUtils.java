/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtalk.dd.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

public class ImageUtils {
//	public static String getThumbnailImagePath(String imagePath) {
//		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
//		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
//		EMLog.d("msg", "original image path:" + imagePath);
//		EMLog.d("msg", "thum image path:" + path);
//		return path;
//	}
    public static final String CACHE_IMAGE_FILE_PATH = Environment.getExternalStorageDirectory() + "/dtalk/image/";
    public final static String SDCARD_MNT = "/mnt/sdcard";
    public final static String SDCARD = "/sdcard";
    public final static String DCIM = "/DCIM/Camera/";
    
    /** 请求相册 */
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
    /** 请求相机 */
    public static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 1;
    /** 请求裁剪 */
    public static final int REQUEST_CODE_GETIMAGE_BYCROP = 2;
    
    /**
     * 写图片文件
     * 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
     * @throws IOException 
     */
    public static void saveImage(Context context, String fileName, Bitmap bitmap) throws IOException 
    { 
        saveImage(context, fileName, bitmap, 100);
    }
    public static void saveImage(Context context, String fileName, Bitmap bitmap, int quality) throws IOException 
    { 
        if(bitmap==null || fileName==null || context==null) return;     

        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, quality, stream);
        byte[] bytes = stream.toByteArray();
        fos.write(bytes);           
        fos.close();
    }
    
    /**
     * 写图片文件到SD卡
     * @throws IOException 
     */
    public static void saveImageToSD(String fileName, Bitmap bitmap, int quality) throws IOException
    {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        File dir = new File(CACHE_IMAGE_FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir+"/"+fileName+".png");
        if(bitmap != null) {
            FileOutputStream fos = new FileOutputStream(file);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, quality, stream);
            byte[] bytes = stream.toByteArray();
            fos.write(bytes);           
            fos.close();
        }
    }

    public static void saveImageToSD(File file, Bitmap bitmap, int quality) throws IOException
    {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        if(bitmap != null) {
            FileOutputStream fos = new FileOutputStream(file);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
            byte[] bytes = stream.toByteArray();
            fos.write(bytes);
            fos.close();
        }
    }
    
    /**
     * 写图片文件到SD卡 CACHE_FILE_PATH
     * @param imageUrl
     * @param fileName
     */
    public static void saveImageFile(String imageUrl,String fileName){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        File dir = new File(CACHE_IMAGE_FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            in = url.openStream();
            fos = new FileOutputStream(file);
            int len = -1;
            byte[] data = new byte[1024];
            while((len = in.read(data))!=-1){
                fos.write(data, 0, len);
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(fos != null)
                    fos.close();
                if(in != null)
                    in.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    public static String saveBitmapByte (byte[] data, String fileName) {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return null;
        }
        File dir = new File(CACHE_IMAGE_FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try{
                if(fos != null)
                    fos.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }
    
    /**
     * 获取bitmap
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getBitmap(Context context,String fileName) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = context.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }finally{
            try {
                fis.close();
            } catch (Exception e) {}
        }
        return bitmap;
    }
    /**
     * 获取bitmap
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath) {
        return getBitmapByPath(filePath, null);
    }
    public static Bitmap getBitmapByPath(String filePath, Options opts) {
        FileInputStream fis = null;
        Bitmap bitmap =null; 
        try { 
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis,null,opts);
        } catch (FileNotFoundException e) {  
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally{
            try {
                fis.close();
            } catch (Exception e) {}
        }
        return bitmap;
    }
    public static Bitmap getBitmapByPath(String filePath, Options opts, int size) {
        Bitmap bitmap =null;
        try { 
            int rotation = getExifOrientation(filePath);
            Options options = new Options();
            options.inPreferredConfig = Config.RGB_565;
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(filePath, options);
            int bigOne = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
            if(bigOne > size){
                options.inSampleSize = (int) (bigOne/size);
            }
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath,options);
            if(bitmap == null)
                return null;
            Matrix matrix = new Matrix();
            if(rotation != 0){
                matrix.setRotate(rotation);
            }
            Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (finalBitmap != bitmap) {
                bitmap.recycle();
            }
            return finalBitmap;
        } catch (Exception e) {
            return null;
        } 
    }
    
    public static void compressBitmapAndWriteToFile(String filePath, Options opts,int size, String newFilePath) {
        Bitmap bitmap =null;
        try { 
            Options options = new Options();
            options.inPreferredConfig = Config.RGB_565;
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(filePath, options);
            int bigOne = options.outWidth > options.outHeight ? options.outHeight : options.outWidth;
            if(bigOne > size){
                int Ratio = Math.round((float) bigOne / (float) size);
                options.inSampleSize = Ratio;
            }
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(filePath,options);
            FileOutputStream fos = new FileOutputStream(newFilePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 90, stream);
            byte[] bytes = stream.toByteArray();
            fos.write(bytes);           
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public static Bitmap getBitmapByImageUrl(String imageUrl, Options opts,int size) {
        Bitmap bitmap =null;
        try { 
            URL url = new URL(imageUrl);
            //int rotation = getJpgRotation(filePath);
            opts=new Options();
            opts.inPreferredConfig = Config.RGB_565;
            opts.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(url.openStream(), null, opts);
            int bigOne = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
            if(bigOne > size){
                opts.inSampleSize = (int) (bigOne/size);
            }
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(url.openStream(), null, opts);
            if(bitmap == null)
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return bitmap;
    }
    
    public static void saveImageFileInDirNamedFileNameWithImageUrl(String imageUrl, String dirpath, String fileName){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        File dir = new File(dirpath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            int length = conn.getContentLength();
            if(length == -1){
                length = 260000;
            }
            int count = 0;
            int progress = 0;
//          Intent intent = new Intent();
//          intent.putExtra(ValueClass.INTENT_DOWNLOAD_PROGRESS, progress);
//          intent.setAction(ValueClass.ACTION_DOWNLOAD_PROGRESS);//action与接收器相同
//          AppManager.getAppManager().currentActivity().sendBroadcast(intent);
            in = url.openStream();
            fos = new FileOutputStream(file);
            int len = -1;
            byte[] data = new byte[1024];
            while((len = in.read(data))!=-1){
                count += len;
                progress = (int)(((float)count / length) * 100);
//              intent.putExtra(ValueClass.INTENT_DOWNLOAD_PROGRESS, progress);
//              intent.setAction(ValueClass.ACTION_DOWNLOAD_PROGRESS);//action与接收器相同
//              AppManager.getAppManager().currentActivity().sendBroadcast(intent);
                fos.write(data, 0, len);
                fos.flush();
            }
//          intent.putExtra(ValueClass.INTENT_DOWNLOAD_PROGRESS, 100);
//          intent.setAction(ValueClass.ACTION_DOWNLOAD_PROGRESS);//action与接收器相同
//          AppManager.getAppManager().currentActivity().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(fos != null)
                    fos.close();
                if(in != null)
                    in.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    /**
     * 获取bitmap
     * @param file
     * @return
     */
    public static Bitmap getBitmapByFile(File file) {
        FileInputStream fis = null;
        Bitmap bitmap =null; 
        try { 
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {  
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally{
            try {
                fis.close();
            } catch (Exception e) {}
        }
        return bitmap;
    }
    
    /**
     * 使用当前时间戳拼接一个唯一的文件名
     * @param format
     * @return
     */
    public static String getTempFileName() 
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
        String fileName = format.format( new Timestamp( System.currentTimeMillis()) );
        return fileName;
    }
    
    /**
     * 获取照相机使用的目录
     * @return
     */
    public static String getCamerPath()
    {
        return Environment.getExternalStorageDirectory() + File.separator +  "FounderNews" + File.separator;
    }
    
    /**
     * 判断当前Url是否标准的content://样式，如果不是，则返回绝对路径
     * @param uri
     * @return
     */
    public static String getAbsolutePathFromNoStandardUri(Uri mUri)
    {   
        String filePath = null;
        
        String mUriString = mUri.toString();
        mUriString = Uri.decode(mUriString);
        
        String pre1 = "file://" + SDCARD + File.separator;
        String pre2 = "file://" + SDCARD_MNT + File.separator;
        
        if( mUriString.startsWith(pre1) )
        {    
            filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + mUriString.substring( pre1.length() );
        }
        else if( mUriString.startsWith(pre2) )
        {
            filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + mUriString.substring( pre2.length() );
        }
        return filePath;
    }
    
     /**
     * 通过uri获取文件的绝对路径
     * @param uri
     * @return
     */
    public static String getAbsoluteImagePath(Activity context,Uri uri) 
    {
        String imagePath = "";
        String [] proj={MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = context.managedQuery( uri,
                        proj,       // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null);      // Order-by clause (ascending by name)
        
        if(cursor!=null)
        {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if(  cursor.getCount()>0 && cursor.moveToFirst() )
            {
                imagePath = cursor.getString(column_index);
            }
        }
        
        return imagePath;
    }
    
//    /**
//     * 获取图片缩略图
//     * 只有Android2.1以上版本支持
//     * @param imgName
//     * @param kind   MediaStore.Images.Thumbnails.MICRO_KIND
//     * @return
//     */
//    public static Bitmap loadImgThumbnail(Activity context, String imgName, int kind)
//    {
//        Bitmap bitmap = null;
//
//        String[] proj = { MediaStore.Images.Media._ID,
//                        MediaStore.Images.Media.DISPLAY_NAME };
//
//        @SuppressWarnings("deprecation")
//        Cursor cursor = context.managedQuery(
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
//                        MediaStore.Images.Media.DISPLAY_NAME + "='" + imgName +"'", null, null);
//
//        if ( cursor!=null && cursor.getCount()>0 && cursor.moveToFirst() )
//        {
//            ContentResolver crThumb = context.getContentResolver();
//            Options options = new Options();
//            options.inSampleSize = 1;
//            bitmap = getThumbnail(crThumb, cursor.getInt(0), kind, options);
//        }
//        return bitmap;
//    }
//
    public static Bitmap loadImgThumbnail(String filePath, int w, int h) {
        Bitmap bitmap = getBitmapByPath(filePath);
        return zoomBitmap(bitmap, w, h);
    }
    
    /**
     * 获取SD卡中最新图片路径
     * @return
     */
    @SuppressWarnings("unused")
    public static String getLatestImage(Activity context)
    {
        String latestImage = null;
        String[] items = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA }; 
        @SuppressWarnings("deprecation")
        Cursor cursor = context.managedQuery(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                                        items, 
                                        null,
                                        null, 
                                        MediaStore.Images.Media._ID + " desc");
        
        if( cursor != null && cursor.getCount()>0 )
        {
            cursor.moveToFirst();
            for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
            {
                latestImage = cursor.getString(1);
                break;
            }
        }
        
        return latestImage;
    }
    
    /**
     * 计算缩放图片的宽高
     * @param img_size
     * @param square_size
     * @return
     */
    public static int[] scaleImageSize(int[] img_size, int square_size) {
        if(img_size[0] <= square_size && img_size[1] <= square_size)
            return img_size;
        double ratio = square_size / (double)Math.max(img_size[0], img_size[1]);
        return new int[]{(int)(img_size[0] * ratio),(int)(img_size[1] * ratio)};
    }
    
    /**
     * 创建缩略图
     * @param context
     * @param largeImagePath 原始大图路径
     * @param thumbfilePath 输出缩略图路径
     * @param square_size 输出图片宽度
     * @param quality 输出图片质量
     * @throws IOException
     */
    public static void createImageThumbnail(Context context, String largeImagePath, String thumbfilePath, int square_size, int quality) throws IOException
    {
        Options opts = new Options();
        opts.inSampleSize = 1;
        //原始图片bitmap
        Bitmap cur_bitmap = getBitmapByPath(largeImagePath, opts);
        
        if(cur_bitmap == null) return;
        
        //原始图片的高宽
        int[] cur_img_size = new int[]{cur_bitmap.getWidth(),cur_bitmap.getHeight()};
        //计算原始图片缩放后的宽高
        int[] new_img_size = scaleImageSize(cur_img_size, square_size);
        //生成缩放后的bitmap
        Bitmap thb_bitmap = zoomBitmap(cur_bitmap, new_img_size[0], new_img_size[1]);
        //生成缩放后的图片文件
        saveImageToSD(thumbfilePath, thb_bitmap, quality);
    }
    
    /**
     * 放大缩小图片
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newbmp = null;
        if(bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidht = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidht, scaleHeight);
            newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }
        return newbmp;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap) {
        //获取这个图片的宽和高   
        int width = bitmap.getWidth();   
        int height = bitmap.getHeight();    
        //定义预转换成的图片的宽度和高度   
        int newWidth = 200;   
        int newHeight = 200;     
        //计算缩放率，新尺寸除原始尺寸   
        float scaleWidth = ((float) newWidth) / width;   
        float scaleHeight = ((float) newHeight) / height;    
        //创建操作图片用的matrix对象   
        Matrix matrix = new Matrix();    
        //缩放图片动作   
        matrix.postScale(scaleWidth, scaleHeight);  
        //旋转图片 动作   
        //matrix.postRotate(45);   
        //创建新的图片   
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }
    
    /**
     * (缩放)重绘图片 
     * @param context Activity
     * @param bitmap
     * @return
     */
    public static Bitmap reDrawBitMap(Activity context,Bitmap bitmap){ 
        DisplayMetrics dm = new DisplayMetrics(); 
        context.getWindowManager().getDefaultDisplay().getMetrics(dm); 
        int rWidth = dm.widthPixels; 
        //float rHeight=dm.heightPixels/dm.density+0.5f; 
        //float rWidth=dm.widthPixels/dm.density+0.5f; 
        //int height=bitmap.getScaledHeight(dm); 
        //int width = bitmap.getScaledWidth(dm); 
        int width = bitmap.getWidth(); 
        float zoomScale; 
        /**方式1**/
//      if(rWidth/rHeight>width/height){//以高为准 
//          zoomScale=((float) rHeight) / height; 
//      }else{ 
//          //if(rWidth/rHeight<width/height)//以宽为准 
//          zoomScale=((float) rWidth) / width; 
//      } 
        /**方式2**/
//      if(width*1.5 >= height) {//以宽为准
//          if(width >= rWidth)
//              zoomScale = ((float) rWidth) / width;
//          else
//              zoomScale = 1.0f;
//      }else {//以高为准
//          if(height >= rHeight)
//              zoomScale = ((float) rHeight) / height;
//          else
//              zoomScale = 1.0f;
//      }
        /**方式3**/
        if(width >= rWidth)
            zoomScale = ((float) rWidth) / width;
        else
            zoomScale = 1.0f;
        //创建操作图片用的matrix对象  
        Matrix matrix = new Matrix();  
        //缩放图片动作  
        matrix.postScale(zoomScale, zoomScale);  
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
        return resizedBitmap; 
    }  
    
    /**
     * 将Drawable转化为Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }

    /**
     * 获得圆角图片的方法
     * @param bitmap
     * @param roundPx 一般设成14
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * 获得带倒影的图片方法
     * @param bitmap
     * @return
     */
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
                width, height / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + height / 2), Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x00ffffff, TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }
    
    /**
     * 将bitmap转化为drawable
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        @SuppressWarnings("deprecation")
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }
    
    /**
     * 获取图片类型
     * @param file
     * @return
     */
    public static String getImageType(File file){
        if(file == null||!file.exists()){
            return null;
        }
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            String type = getImageType(in);
            return type;
        } catch (IOException e) {
            return null;
        }finally{
            try{
                if(in != null){
                    in.close();
                }
            }catch(IOException e){
            }
        }
    }
    
    /**
     * detect bytes's image type by inputstream
     * @param in
     * @return
     * @see #getImageType(byte[])
     */
    public static String getImageType(InputStream in) {
        if(in == null){
            return null;
        }
        try{
            byte[] bytes = new byte[8];
            in.read(bytes);
            return getImageType(bytes);
        }catch(IOException e){
            return null;
        }
    }

    /**
     * detect bytes's image type
     * @param bytes 2~8 byte at beginning of the image file  
     * @return image mimetype or null if the file is not image
     */
    public static String getImageType(byte[] bytes) {
        if (isJPEG(bytes)) {
            return "image/jpeg";
        }
        if (isGIF(bytes)) {
            return "image/gif";
        }
        if (isPNG(bytes)) {
            return "image/png";
        }
        if (isBMP(bytes)) {
            return "application/x-bmp";
        }
        return null;
    }

    private static boolean isJPEG(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == (byte)0xFF) && (b[1] == (byte)0xD8);
    }

    private static boolean isGIF(byte[] b) {
        if (b.length < 6) {
            return false;
        }
        return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        if (b.length < 8) {
            return false;
        }
        return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78
                && b[3] == (byte) 71 && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == 0x42) && (b[1] == 0x4d);
    }
    
    /**
     * 获取图像的旋转方向
     * @param picUrl
     * @return
     */
//    public static int getJpgRotation(String picUrl)
//    {
//            if(picUrl == null)
//                    return 0;
//            if(picUrl.endsWith(".jpg") == false && picUrl.endsWith(".JPG") ==  false && picUrl.endsWith(".dat") == false)
//                    return 0;
//            ExifInterface exif;
//            try {
//                    exif = new ExifInterface(picUrl);
//                    String rotateStr = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//                    if(rotateStr != null && rotateStr.length() > 0)
//                    {
//                            int rotate =  Integer.parseInt(rotateStr);
//                            switch(rotate)
//                            {
//                            case ExifInterface.ORIENTATION_ROTATE_90:
//                                    return 90;
//                            case ExifInterface.ORIENTATION_ROTATE_180:
//                                    return 180;
//                            case ExifInterface.ORIENTATION_ROTATE_270:
//                                    return 270;
//                            }
//                    }
//            } catch (Exception e) {
//            }
//            return 0;
//    }
    
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }
    
    /**
     * 图片灰度化
     * @param bmpOriginal
     * @return
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // 灰色
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    
    
    //缩放图片
    
    public static enum ScalingLogic {
        CROP, FIT
    }
    
    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }
    
    
    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
       if (scalingLogic == ScalingLogic.CROP) {
       final float srcAspect = (float)srcWidth / (float)srcHeight;
       final float dstAspect = (float)dstWidth / (float)dstHeight;
           if (srcAspect > dstAspect) {
               final int srcRectWidth = (int)(srcHeight * dstAspect);
               final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
               return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
           } else {
               final int srcRectHeight = (int)(srcWidth / dstAspect);
               final int scrRectTop = (int)(srcHeight - srcRectHeight) / 2;
               return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
           }
       } else {
           return new Rect(0, 0, srcWidth, srcHeight);
       }
    }
    
    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
          if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;
            if (srcAspect > dstAspect) {
              return new Rect(0, 0, dstWidth, (int)(dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int)(dstHeight * srcAspect), dstHeight);
            }
          } else {
            return new Rect(0, 0, dstWidth, dstHeight);
          }
    }
    
    public static Bitmap decodeFile(String pathName, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
          Options options = new Options();
          options.inJustDecodeBounds = true;
          BitmapFactory.decodeFile(pathName, options);
          options.inJustDecodeBounds = false;
          options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth, dstHeight, scalingLogic);
          Bitmap unscaledBitmap = BitmapFactory.decodeFile(pathName, options);
          return unscaledBitmap;
    }
    
    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
      if (scalingLogic == ScalingLogic.CROP) {
        final float srcAspect = (float)srcWidth / (float)srcHeight;
        final float dstAspect = (float)dstWidth / (float)dstHeight;
        if (srcAspect > dstAspect) {
          return srcWidth / dstWidth;
        } else {
          return srcHeight / dstHeight;
        }
      } else {
        final float srcAspect = (float)srcWidth / (float)srcHeight;
        final float dstAspect = (float)dstWidth / (float)dstHeight;
        if (srcAspect > dstAspect) {
          return srcHeight / dstHeight;
        } else {
          return srcWidth / dstWidth;
        }
      }
    }
    
    /**
     * convert dip 2 px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue){ 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int)(dipValue * scale + 0.5f); 
    } 
    
    /**
     * convert px 2 dip
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue){ 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int)(pxValue / scale + 0.5f); 
    }  
    
    /**
     * convert sp 2 px
     * @param context
     * @param pxValue
     * @return
     */
    public static int sp2px(Context context, float spValue){ 
        final float scale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int)(spValue * scale + 0.5f); 
    } 
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @SuppressWarnings("deprecation")
    public static int getDisplayWidth(Context context){
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int sdk = Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.HONEYCOMB_MR2) {
            width = wm.getDefaultDisplay().getWidth();
        }
        else{
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        return width;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @SuppressWarnings("deprecation")
    public static int getDisplayHeighth(Context context){
        int height = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int sdk = Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.HONEYCOMB_MR2) {
            height = wm.getDefaultDisplay().getHeight();
        }
        else{
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            height = size.y;
        }
        return height;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @SuppressWarnings("deprecation")
    public static String getScreenSize(Context context){
        String client_screen = null;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int sdk = Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.HONEYCOMB_MR2) {
            client_screen = wm.getDefaultDisplay().getWidth() + "x" + wm.getDefaultDisplay().getHeight();
        }else {
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            client_screen = size.x + "x" + size.y;
        }
        return client_screen;
    }
    
    public static void PhotoChooseOption(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] item = {"拍照","相册"};
        builder.setItems(item, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface arg0, int arg1) {
                switch (arg1) {
                    case 0://拍照
                        String savePath = "";
                        //判断是否挂载了SD卡
                        String storageState = Environment.getExternalStorageState();        
                        if(storageState.equals(Environment.MEDIA_MOUNTED)){
                            savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + DCIM;//存放照片的文件夹
                            File savedir = new File(savePath);
                            if (!savedir.exists()) {
                                savedir.mkdirs();
                            }
                        }
                        
                        //没有挂载SD卡，无法保存文件
                        if(StringUtils.empty(savePath)){
                            Toast.makeText(context, "没有SD卡",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String fileName = timeStamp + ".jpg";//照片命名
                        File out = new File(savePath, fileName);
                        Uri uri = Uri.fromFile(out);
                        
//                      ValueClass.PathForTakeCamera = savePath + fileName;//该照片的绝对路径
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                      intent.putExtra("photo_path", savePath + fileName);
                        context.startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
                        break;
                    case 1://用户相册
                        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT); 
                        intent1.addCategory(Intent.CATEGORY_OPENABLE); 
                        intent1.setType("image/*"); 
                        context.startActivityForResult(Intent.createChooser(intent1, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
                        break;
                }               
            }
        });
        builder.create().show();
    }
    
    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     * 
     * @param imagesrc
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 600, 600);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }
    
    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     * 
     * @param imagesrc
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int pixel) {
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, pixel, pixel);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }
    
    /**
     * 计算图片的缩放值
     * 
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 使用此加载框架的imageloader加载的图片，设置了缓存后，下次使用，手工从缓存取出来用，这时特别要注意，不能直接使用：
     * imageLoader.getMemoryCache().get(uri)来获取，因为在加载过程中，key是经过运算的，而不单单是uri,而是：
     * String memoryCacheKey = MemoryCacheUtil.generateKey(uri, targetSize);
     *
     * @return
     */
    public static Bitmap getBitmapFromCache(String uri,ImageLoader imageLoader){//这里的uri一般就是图片网址
        List<String> memCacheKeyNameList = MemoryCacheUtils.findCacheKeysForImageUri(uri, imageLoader.getMemoryCache());
        if(memCacheKeyNameList != null && memCacheKeyNameList.size() > 0){
            for(String each:memCacheKeyNameList){
            }
            return imageLoader.getMemoryCache().get(memCacheKeyNameList.get(0));
        }

        return null;
    }


    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "DownloadJianXi");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + appDir.getPath())));
    }

}

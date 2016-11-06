/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */package com.dtalk.dd.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;


import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * 文件工具类 Created by Jorstin on 2015/3/18.
 */
public class FileUtils {

	public static String getSecondLevelDirectory(String fileName) {
		if(TextUtils.isEmpty(fileName) || fileName.length() < 4) {
			return null;
		}

		String sub1 = fileName.substring(0, 2);
		String sub2 = fileName.substring(2, 4);
		return sub1 + File.separator + sub2;
	}

	/**
	 * 
	 * @param root
	 * @param fileName
	 * @return
	 */
	public static String getMD5FileDir(String root, String fileName) {
		// FileAccessor.IMESSAGE_IMAGE + File.separator +
		// FileAccessor.getSecondLevelDirectory(fileNameMD5)+ File.separator;
		if (TextUtils.isEmpty(root)) {
			return null;
		}
		File file = new File(root);
		if (!file.exists()) {
			file.mkdirs();
		}

		File fullPath = new File(file,
				getSecondLevelDirectory(fileName));
		if (!fullPath.exists()) {
			fullPath.mkdirs();
		}
		return fullPath.getAbsolutePath();
	}

	@SuppressLint("NewApi")
	public static Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			// retriever.setMode(MediaMetadataRetriever.);
			retriever.setDataSource(filePath);

			bitmap = retriever.getFrameAtTime(1000);

		} catch (Exception ex) {

		} finally {
			try {
				retriever.release();

			} catch (RuntimeException ex) {
			}

		}
		return bitmap;

	}

	/**
	 * 转换成单位
	 * 
	 * @param length
	 * @return
	 */
	public static String formatFileLength(long length) {
		if (length >> 30 > 0L) {
			float sizeGb = Math.round(10.0F * (float) length / 1.073742E+009F) / 10.0F;
			return sizeGb + " GB";
		}
		if (length >> 20 > 0L) {
			return formatSizeMb(length);
		}
		if (length >> 9 > 0L) {
			float sizekb = Math.round(10.0F * (float) length / 1024.0F) / 10.0F;
			return sizekb + " KB";
		}
		return length + " B";
	}

	/**
	 * 转换成Mb单位
	 * 
	 * @param length
	 * @return
	 */
	public static String formatSizeMb(long length) {
		float mbSize = Math.round(10.0F * (float) length / 1048576.0F) / 10.0F;
		return mbSize + " MB";
	}

	/**
	 * 检查SDCARD是否可写
	 * 
	 * @return
	 */
	public static boolean checkExternalStorageCanWrite() {
		try {
			boolean mouted = Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
			if (mouted) {
				boolean canWrite = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath())
						.canWrite();
				if (canWrite) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 返回文件的图标
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getFileIcon(String fileName) {
		String fileType = fileName.toLowerCase();
		if (isDocument(fileType)) {
			return R.drawable.file_attach_doc;
		}
		if (isPic(fileType)) {
			return R.drawable.file_attach_img;
		}

		if (isCompresseFile(fileType)) {
			return R.drawable.file_attach_rar;
		}
		if (isTextFile(fileType)) {
			return R.drawable.file_attach_txt;
		}
		if (isPdf(fileType)) {
			return R.drawable.file_attach_pdf;
		}

		if (isPPt(fileType)) {
			return R.drawable.file_attach_ppt;
		}

		if (isXls(fileType)) {
			return R.drawable.file_attach_xls;
		}
		return R.drawable.file_attach_ohter;
	}

	public static String nullAsNil(String str) {
		if (str == null) {
			return "";
		}
		return str;
	}
	/**
	 * 是否图片
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isPic(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".bmp") || lowerCase.endsWith(".png")
				|| lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")
				|| lowerCase.endsWith(".gif");
	}

	/**
	 * 是否压缩文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isCompresseFile(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".rar") || lowerCase.endsWith(".zip")
				|| lowerCase.endsWith(".7z") || lowerCase.endsWith("tar")
				|| lowerCase.endsWith(".iso");
	}

	/**
	 * 是否音频
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isAudio(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".mp3") || lowerCase.endsWith(".wma")
				|| lowerCase.endsWith(".mp4") || lowerCase.endsWith(".rm");
	}

	/**
	 * 是否文档
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isDocument(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".doc") || lowerCase.endsWith(".docx")
				|| lowerCase.endsWith("wps");
	}

	/**
	 * 是否Pdf
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isPdf(String fileName) {
		return nullAsNil(fileName).toLowerCase().endsWith(".pdf");
	}

	/**
	 * 是否Excel
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isXls(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".xls") || lowerCase.endsWith(".xlsx");
	}

	/**
	 * 是否文本文档
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isTextFile(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".txt") || lowerCase.endsWith(".rtf");
	}

	/**
	 * 是否Ppt
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isPPt(String fileName) {
		String lowerCase = nullAsNil(fileName).toLowerCase();
		return lowerCase.endsWith(".ppt") || lowerCase.endsWith(".pptx");
	}

	/**
	 * decode file length
	 * 
	 * @param filePath
	 * @return
	 */
	public static int decodeFileLength(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return 0;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return 0;
		}
		return (int) file.length();
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean checkFile(String filePath) {
		if (TextUtils.isEmpty(filePath) || !(new File(filePath).exists())) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param filePath
	 * @param seek
	 * @param length
	 * @return
	 */
	public static byte[] readFlieToByte(String filePath, int seek, int length) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		if (length == -1) {
			length = (int) file.length();
		}

		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			byte[] bs = new byte[length];
			randomAccessFile.seek(seek);
			randomAccessFile.readFully(bs);
			randomAccessFile.close();
			return bs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int copyFile(File src, String filename, byte[] buffer) {
		if (!src.exists()) {
			return -1;
		}
		return copyFile(src.getAbsolutePath(), filename, buffer);
	}

	/**
	 * 拷贝文件
	 * 
	 * @param fileDir
	 * @param fileName
	 * @param buffer
	 * @return
	 */
	public static int copyFile(String fileDir, String fileName, byte[] buffer) {
		if (buffer == null) {
			return -2;
		}

		try {
			File file = new File(fileDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			File resultFile = new File(file, fileName);
			if (!resultFile.exists()) {
				resultFile.createNewFile();
			}
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream(resultFile, true));
			bufferedOutputStream.write(buffer);
			bufferedOutputStream.flush();
			bufferedOutputStream.close();
			return 0;

		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 根据文件名和后缀 拷贝文件
	 * 
	 * @param fileDir
	 * @param fileName
	 * @param ext
	 * @param buffer
	 * @return
	 */
	public static int copyFile(String fileDir, String fileName, String ext,
			byte[] buffer) {
		return copyFile(fileDir, fileName + ext, buffer);
	}

	/**
	 * 根据后缀名判断是否是图片文件
	 * 
	 * @param type
	 * @return 是否是图片结果true or false
	 */
	public static boolean isImage(String type) {
		if (type != null
				&& (type.equals("jpg") || type.equals("gif")
						|| type.equals("png") || type.equals("jpeg")
						|| type.equals("bmp") || type.equals("wbmp")
						|| type.equals("ico") || type.equals("jpe"))) {
			return true;
		}
		return false;
	}

	public static String getFileExt(String fileName) {

		if (TextUtils.isEmpty(fileName)) {

			return "";
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length());
	}

	public static String getVideoMsgUrl(String url) {

		if (TextUtils.isEmpty(url)) {

			return "";
		}
		return url.substring(url.lastIndexOf("_") + 1,
				url.length());

	}

	// 根缓存目录
	private static String cacheRootPath = "";

	/**
	 * sd卡是否可用
	 *
	 * @return
	 */
	public static boolean isSdCardAvailable() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 创建根缓存目录
	 *
	 * @return
	 */
	public static String createRootPath() {
		if (isSdCardAvailable()) {
			// /sdcard/Android/data/<application package>/cache
			cacheRootPath = IMApplication.getInstance().getExternalCacheDir()
					.getPath();
		} else {
			// /data/data/<application package>/cache
			cacheRootPath = IMApplication.getInstance().getCacheDir().getPath();
		}
		return cacheRootPath;
	}

	/**
	 * 创建文件夹
	 *
	 * @param dirPath
	 * @return 创建失败返回""
	 */
	private static String createDir(String dirPath) {
		try {
			File dir = new File(dirPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return dir.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dirPath;
	}

	/**
	 * 获取图片缓存目录
	 *
	 * @return 创建失败,返回""
	 */
	public static String getImageCachePath() {
		String path = createDir(createRootPath() + File.separator + "img"
				+ File.separator);
		return path;
	}

	/**
	 * 获取图片裁剪缓存目录
	 *
	 * @return 创建失败,返回""
	 */
	public static String getImageCropCachePath() {
		String path = createDir(createRootPath() + File.separator + "imgCrop"
				+ File.separator);

		return path;
	}

	/**
	 * 删除文件或者文件夹
	 *
	 * @param file
	 */
	public static void deleteFileOrDirectory(File file) {
		try {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				// 删除空文件夹
				if (childFiles == null || childFiles.length == 0) {
					file.delete();
					return;
				}
				// 递归删除文件夹下的子文件
				for (int i = 0; i < childFiles.length; i++) {
					deleteFileOrDirectory(childFiles[i]);
				}
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将内容写入文件
	 *
	 * @param filePath
	 *            eg:/mnt/sdcard/demo.txt
	 * @param content
	 *            内容
	 */
	public static void writeFileSdcard(String filePath, String content,
									   boolean isAppend) {

		try {
			FileOutputStream fout = new FileOutputStream(filePath, isAppend);
			byte[] bytes = content.getBytes();

			fout.write(bytes);

			fout.close();

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

}

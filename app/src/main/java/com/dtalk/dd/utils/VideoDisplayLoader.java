package com.dtalk.dd.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Donal on 16/6/14.
 */
public class VideoDisplayLoader {
    private MemoryCache mMCache;//一级缓存,内存缓存

    private static VideoDisplayLoader ins = new VideoDisplayLoader();

    public static VideoDisplayLoader getIns() {
        return ins;
    }

    private VideoDisplayLoader() {
        mMCache = ImageLoader.getInstance().getMemoryCache();
    }

    public void display(String url, VideoDisplayListener videoDisplayListener) {

        new VideoLoadTask(url, videoDisplayListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//使用AsyncTask自带的线程池

    }


    private class VideoLoadTask extends AsyncTask<Void, Void, String> {
        private String url;
        private VideoDisplayListener videoDisplayListener;

        public VideoLoadTask(String url, VideoDisplayListener videoDisplayListener) {
            this.url = url;
            this.videoDisplayListener = videoDisplayListener;
        }

        @Override
        protected String doInBackground(Void... params) {
            String localFilePath;
            localFilePath = getLocalFilePath(this.url);
            if (new File(localFilePath).exists()) {
                return localFilePath;
            }
            try {
                File tmpFile = new File(localFilePath);
                FileOutputStream fos = new FileOutputStream(tmpFile);
                URL url = new URL(this.url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                int length = conn.getContentLength();
                byte buf[] = new byte[1024];
                int count = 0;
                do {
                    int numread = is.read(buf);
                    count += numread;
                    //当前进度值
//                    progress =(int)(((float)count / length) * 100);
                    if (numread <= 0) {
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);
                fos.close();
                is.close();
                return localFilePath;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String bitmap) {
            this.videoDisplayListener.onVideoLoadCompleted(this.url, bitmap);
        }
    }

    public String getLocalFilePath(String remoteUrl) {
        File dir = CommonUtil.getVideoSavePath();
        String savePath = dir.getAbsolutePath();
        String localPath;
        if (remoteUrl.contains("/")) {
            localPath = savePath + remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
        } else {
            localPath = savePath + "/" + remoteUrl;
        }
        return localPath;
    }

    public interface VideoDisplayListener {
        void onVideoLoadCompleted(String url, String path);
    }
}

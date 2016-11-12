package com.dtalk.dd.qiniu.utils;

import android.content.Context;
import android.util.Base64;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.MD5Util;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Donal on 16/11/4.
 */

public class QNUploadManager {

    private static QNUploadManager qnUploadManager;
    private UploadManager uploadManager;
    private String token;
    public QNUploadManager(Context context) {
        uploadManager = new UploadManager();
        token = initQNToken();
    }

    public static QNUploadManager getInstance(Context context) {
        synchronized (QNUploadManager.class) {
            if (qnUploadManager == null) {
                Logger.d("new up");
                qnUploadManager = new QNUploadManager(IMApplication.getInstance());
            }
            return qnUploadManager;
        }
    }

    private String initQNToken() {
        String auploadToken = "";
        PutPolicy putPolicy = new PutPolicy(Config.QINIU_BUCKET);
        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
        try {
            auploadToken = putPolicy.token(mac);
        } catch (Exception e) {
        }
        return auploadToken;
    }

    public void uploadCircleFiles(final List<String> path, final SVProgressHUD svProgressHUD, final OnQNUploadCallback callback) {
        final Map<String, String> uploadedFiles = new HashMap<>();
        for (String item : path) {
            String qiniuKey = "event/" + MD5Util.getMD5String(item) + ".png";
            Logger.d(token);
            uploadManager.put(item, qiniuKey, token, new UpCompletionHandler() {
                @Override
                public void complete(String key, ResponseInfo info, JSONObject response) {
                    Logger.d(info.toString());
                    uploadedFiles.put(key, Config.QINIU_PREFIX+key);
                    if (uploadedFiles.size() == path.size()) {
                        if (callback != null) {
                            callback.uploadCompleted(uploadedFiles);
                        }
                    }
                }
            }, new UploadOptions(null, null, false,
                    new UpProgressHandler(){
                        public void progress(String key, double percent){
                            Logger.i(key + ": " + percent);
                            if (svProgressHUD != null) {
                                svProgressHUD.getProgressBar().setProgress((int) (percent * 100));
                            }
                        }
                    }, null));
        }
    }

    public void uploadCircleVideo(final String path, final SVProgressHUD svProgressHUD, final OnQNUploadCallback callback) {
        final Map<String, String> uploadedFiles = new HashMap<>();
        String qiniuKey = "event/" + MD5Util.getMD5String(path) + ".mp4";
        Logger.d(token);
        uploadManager.put(path, qiniuKey, token, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                Logger.d(info.toString());
                uploadedFiles.put(key, Config.QINIU_PREFIX+key);
                if (callback != null) {
                    callback.uploadCompleted(uploadedFiles);
                }
            }
        }, new UploadOptions(null, null, false,
                new UpProgressHandler(){
                    public void progress(String key, double percent){
                        Logger.i(key + ": " + percent);
                        if (svProgressHUD != null) {
                            svProgressHUD.getProgressBar().setProgress((int) (percent * 100));
                        }
                    }
                }, null));
    }

    public interface OnQNUploadCallback {
        abstract void uploadCompleted(Map<String, String> uploadedFiles);
    }
}

package com.dtalk.dd.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.utils.ImageLoaderUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.yixia.camera.VCamera;
import com.yixia.camera.demo.service.AssertService;
import com.yixia.camera.util.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import cn.jpush.android.api.JPushInterface;
import im.fir.sdk.FIR;


public class IMApplication extends Application {
    private static IMApplication _context;

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("Application starts");
        MultiDex.install(this);
        startIMService();
        _context = this;
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
        FIR.init(this);
        Fresco.initialize(this, createFrescoConfig());

        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                VCamera.setVideoCachePath(dcim + "/DtalkJuns/");
            } else {
                VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/DtalkJuns/");
            }
        } else {
            VCamera.setVideoCachePath(dcim + "/DtalkJuns/");
        }
        // 开启log输出,ffmpeg输出到logcat
        VCamera.setDebugMode(true);
        // 初始化拍摄SDK，必须
        VCamera.initialize(this);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        // 解压assert里面的文件
        String isUpdateCid = SandboxUtils.getInstance().get(this, "theme_parse");
        if (StringUtils.empty(isUpdateCid)) {
            startService(new Intent(this, AssertService.class));
        }
        initOK();
    }

    private ImagePipelineConfig createFrescoConfig() {
        DiskCacheConfig mainDiskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(getExternalCacheDir())
                .setBaseDirectoryName("fresco cache")
                .setMaxCacheSize(100 * 1024 * 1024)
                .setMaxCacheSizeOnLowDiskSpace(10 * 1024 * 1024)
                .setMaxCacheSizeOnVeryLowDiskSpace(5 * 1024 * 1024)
                .setVersion(1)
                .build();
        return ImagePipelineConfig.newBuilder(this)
//                .setBitmapMemoryCacheParamsSupplier(bitmapCacheParamsSupplier)
//                .setCacheKeyFactory(cacheKeyFactory)
//                .setEncodedMemoryCacheParamsSupplier(encodedCacheParamsSupplier)
//                .setExecutorSupplier(executorSupplier)
//                .setImageCacheStatsTracker(imageCacheStatsTracker)
                .setMainDiskCacheConfig(mainDiskCacheConfig)
//                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
//                .setNetworkFetchProducer(networkFetchProducer)
//                .setPoolFactory(poolFactory)
//                .setProgressiveJpegConfig(progressiveJpegConfig)
//                .setRequestListeners(requestListeners)
//                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();
    }

    private void initOK() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", "Android-TT");
        OkGo.init(this);
        try {
            OkGo.getInstance()
                    .debug("OkGo")
                    .setConnectTimeout(OkGo.DEFAULT_MILLISECONDS)  //全局的连接超时时间
                    .setReadTimeOut(OkGo.DEFAULT_MILLISECONDS)     //全局的读取超时时间
                    .setWriteTimeOut(OkGo.DEFAULT_MILLISECONDS)    //全局的写入超时时间
                    .setCacheMode(CacheMode.NO_CACHE)
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                    .setCookieStore(new PersistentCookieStore())
                    .addCommonHeaders(headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startIMService() {
        Logger.i("start IMService");
        Intent intent = new Intent();
        intent.setClass(this, IMService.class);
        startService(intent);
    }

    public static boolean gifRunning = true;//gif是否运行

    public static IMApplication getInstance() {
        return _context;
    }

}

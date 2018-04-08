package com.dtalk.dd.app;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.support.multidex.MultiDex;

import com.dtalk.dd.BuildConfig;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.utils.ImageLoaderUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import cn.jpush.android.api.JPushInterface;
import okhttp3.OkHttpClient;


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
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        MultiDex.install(this);
        startIMService();
        _context = this;
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
        Fresco.initialize(this, createFrescoConfig());

        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        // 解压assert里面的文件
        String isUpdateCid = SandboxUtils.getInstance().get(this, "theme_parse");
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
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
            builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
            builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
            builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
            HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
            builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("tt");
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
                loggingInterceptor.setColorLevel(Level.WARNING);
                builder.addInterceptor(loggingInterceptor);
            }
            OkGo.getInstance()
                    .init(this)
                    .setOkHttpClient(builder.build())
                    .setCacheMode(CacheMode.NO_CACHE)
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                    .setRetryCount(0)
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

package com.dtalk.dd.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Donal on 16/9/28.
 */

public class SandboxUtils {
    public static final String TAG = "SandboxUtils";
    private final static String APP_CONFIG = "config";


    private static SandboxUtils sandboxUtils;

    public SandboxUtils() {

    }

    public static SandboxUtils getInstance() {
        synchronized (SandboxUtils.class) {
            if (sandboxUtils == null) {
                sandboxUtils = new SandboxUtils();
            }
            return sandboxUtils;
        }
    }

    public boolean containsProperty(Context context, String key) {
        Properties props = getProperties(context);
        return props.containsKey(key);
    }

    public void setProperties(Context context, Properties ps) {
        set(context, ps);
    }

    public Properties getProperties(Context context) {
        return get(context);
    }

    public void setProperty(Context context, String key, String value) {
        set(context, key, value);
    }

    public String getProperty(Context context, String key) {
        return get(context, key);
    }

    public void removeProperty(Context context, String... key) {
        remove(context, key);
    }

    public String get(Context context, String key) {
        Properties props = get(context);
        return (props != null) ? props.getProperty(key) : null;
    }

    public Properties get(Context context) {
        FileInputStream fis = null;
        Properties props = new Properties();
        try {
            //读取files目录下的config
            //fis = activity.openFileInput(APP_CONFIG);

            //读取app_config目录下的config
            File dirConf = context.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            fis = new FileInputStream(dirConf.getPath() + File.separator + APP_CONFIG);

            props.load(fis);
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return props;
    }

    private void setProps(Context context, Properties p) {
        FileOutputStream fos = null;
        try {
            //把config建在files目录下
            //fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);

            //把config建在(自定义)app_config的目录下
            File dirConf = context.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            File conf = new File(dirConf, APP_CONFIG);
            fos = new FileOutputStream(conf);

            p.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public void set(Context context, Properties ps) {
        Properties props = get(context);
        props.putAll(ps);
        setProps(context, props);
    }

    public void set(Context context, String key, String value) {
        Properties props = get(context);
        props.setProperty(key, value);
        setProps(context, props);
    }

    public void remove(Context context, String... key) {
        Properties props = get(context);
        for (String k : key)
            props.remove(k);
        setProps(context, props);
    }

    /**
     * 清除app缓存
     */
    public void clearAppCache(Context context) {
        //清除webview缓存
//		File file = CacheManager.
//		if (file != null && file.exists() && file.isDirectory()) {
//		    for (File item : file.listFiles()) {
//		    	item.delete();
//		    }
//		    file.delete();
//		}
        context.deleteDatabase("webview.db");
        context.deleteDatabase("webview.db-shm");
        context.deleteDatabase("webview.db-wal");
        context.deleteDatabase("webviewCache.db");
        context.deleteDatabase("webviewCache.db-shm");
        context.deleteDatabase("webviewCache.db-wal");
        //清除数据缓存
        clearCacheFolder(context.getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(context.getCacheDir(), System.currentTimeMillis());
        //清除编辑器保存的临时内容
        Properties props = getProperties(context);
        for (Object key : props.keySet()) {
            String _key = key.toString();
            if (_key.startsWith("temp"))
                removeProperty(context, _key);
        }
    }

    /**
     * 清除缓存目录
     *
     * @param dir     目录
     * @param curTime 当前系统时间
     * @return
     */
    @SuppressWarnings("unused")
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    /**
     * 保存对象
     *
     * @param context 上下文
     * @param ser     Serializable的object
     * @param key     key
     * @return boolean
     */
    public boolean saveObject(Context context, Serializable ser, String key) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(key, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取对象
     *
     * @param context 上下文
     * @param key     key
     * @return Serializable的object
     */
    public Serializable readObject(Context context, String key) {
        if (!isExistDataCache(context, key))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(key);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (OutOfMemoryError e) {
        } catch (Exception e) {
            e.printStackTrace();
            //反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(key);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 判断缓存是否存在
     *
     * @param context  上下文
     * @param cacheKey key
     * @return boolean
     */
    private boolean isExistDataCache(Context context, String cacheKey) {
        boolean exist = false;
        File data = context.getFileStreamPath(cacheKey);
        if (data.exists())
            exist = true;
        return exist;
    }

    public void removeObject(Context context, String file)
    {
        if (isExistDataCache(context, file)) {
            File data = context.getFileStreamPath(file);
            data.delete();
        }
    }

}

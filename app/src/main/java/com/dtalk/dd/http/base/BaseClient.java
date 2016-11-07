package com.dtalk.dd.http.base;

import android.util.Base64;

import com.dtalk.dd.Security;
import com.dtalk.dd.config.UrlConstant;

/**
 * Created by Donal on 16/4/19.
 */
public class BaseClient {

    public static String getAbsoluteUrl(String relativeUrl) {
        if (relativeUrl.contains("http")) {
            return relativeUrl;
        }
        else {
            return UrlConstant.BASE_API + relativeUrl;
        }
    }

    public interface ClientCallback
    {
        abstract void onPreConnection();

        abstract void onCloseConnection();

        /**
         * 返回api有效数据
         *
         * @param data
         */
        abstract void onSuccess(Object data);


        /**
         * 连接api失败
         *
         * @param message
         */
        abstract void onFailure(String message);

        /**
         * 返回解析json等异常
         *
         * @param e
         */
        abstract void onException(Exception e);
    }

    public static String encryptParam(String param) throws Exception{
        return new String(Security.getInstance().EncryptMsg(Base64.encodeToString(param.getBytes("utf-8"), Base64.DEFAULT)));
    }

    public static String decryptResult(String result) throws Exception{
        return new String(Base64.decode(Security.getInstance().DecryptMsg(result), Base64.DEFAULT));
    }
}

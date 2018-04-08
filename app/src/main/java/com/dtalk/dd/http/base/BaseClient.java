package com.dtalk.dd.http.base;

import android.util.Base64;

import com.dtalk.dd.Security;
import com.dtalk.dd.config.UrlConstant;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.base.Request;

/**
 * Created by Donal on 16/4/19.
 */
public class BaseClient {

    public static String getAbsoluteUrl(String relativeUrl) {
        if (relativeUrl.contains("http")) {
            return relativeUrl;
        } else {
            return UrlConstant.BASE_API + relativeUrl;
        }
    }


    public static String encryptParam(String param) throws Exception {
        return new String(Security.getInstance().EncryptMsg(Base64.encodeToString(param.getBytes("utf-8"), Base64.DEFAULT)));
    }

    public static String decryptResult(String result) throws Exception {
        return new String(Base64.decode(Security.getInstance().DecryptMsg(result), Base64.DEFAULT));
    }

    public static void postRequest(String url, HttpParams params, final ClientCallback callback) {
        OkGo.<String>post(url)
                .params(params)
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        if (callback != null) {
                            callback.onPreConnection();
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (callback != null) {
                            callback.onCloseConnection();
                        }
                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        if (callback != null) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(response.body());
                            } else {
                                callback.onFailure(response.message());
                            }
                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        if (callback != null) {
                            callback.onFailure(response.getException().toString());
                        }
                    }
                });
    }

    public static void onStart(ClientCallback callback) {
        if (callback != null) {
            callback.onPreConnection();
        }
    }

    public static void onFinish(ClientCallback callback) {
        if (callback != null) {
            callback.onCloseConnection();
        }
    }

    public static void onRequestSuccess(ClientCallback callback, Object data) {
        if (callback != null) {
            callback.onSuccess(data);
        }
    }

    public static void onRequestFailure(ClientCallback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }

    public static void onRequestException(ClientCallback callback, Exception e) {
        if (callback != null) {
            callback.onException(e);
        }
    }
}

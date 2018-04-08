package com.dtalk.dd.http.register;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.utils.MD5Util;
import com.lzy.okgo.model.HttpParams;

/**
 * Created by Donal on 16/4/19.
 */
public class RegisterClient extends BaseClient {
    private final static String TOKEN = "5EC77EFF9765188C81203E08DBDF74E9";

    public static void registerUser(String account, String password, String nickname, String avatar, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        callback.onPreConnection();
        params.put("username", account);
        params.put("password", password);
        params.put("nickname", nickname);
        params.put("avatar", avatar);
        postRequest(getAbsoluteUrl("/Api/Auth/register"), params, new ClientCallback() {
            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    BaseResponse res = JSON.parseObject((String) data, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    onRequestException(callback, e);
                }
            }

            @Override
            public void onFailure(String message) {
                onRequestFailure(callback, message);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    public static void confirmSMSCode(String phone, String code, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", phone);
        params.put("code", code);
        String sign = MD5Util.getMD5String(MD5Util.getMD5String("code=" + code + "&phone=" + phone) + "" + TOKEN);
        params.put("sign", sign);
        postRequest(getAbsoluteUrl("/Api/Sms/verify"), params, new ClientCallback() {
            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    BaseResponse res = JSON.parseObject((String) data, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    onRequestException(callback, e);
                }
            }

            @Override
            public void onFailure(String message) {
                onRequestFailure(callback, message);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    public static void sendSmsCode(final String phone, String type, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", phone);
        params.put("type", type);
        String sign = MD5Util.getMD5String(MD5Util.getMD5String("phone=" + phone + "&type=" + type) + "" + TOKEN);
        params.put("sign", sign);
        postRequest(getAbsoluteUrl("/Api/Sms/send"), params, new ClientCallback() {
            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    BaseResponse res = JSON.parseObject((String) data, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    onRequestException(callback, e);
                }
            }

            @Override
            public void onFailure(String message) {
                onRequestFailure(callback, message);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    public static void resetPassword(String account, String code, String password, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", account);
        params.put("code", code);
        params.put("newpassword", password);
        postRequest(getAbsoluteUrl("/Api/Auth/resetPassword"), params, new ClientCallback() {
            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    BaseResponse res = JSON.parseObject((String) data, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    onRequestException(callback, e);
                }
            }

            @Override
            public void onFailure(String message) {
                onRequestFailure(callback, message);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }
}

package com.dtalk.dd.http.register;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.utils.MD5Util;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.utils.Logger;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.apache.http.Header;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Donal on 16/4/19.
 */
public class RegisterClient extends BaseClient{
    private final static String TOKEN = "5EC77EFF9765188C81203E08DBDF74E9";
    public static void registerUser(String account, String password, String nickname, String avatar, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        callback.onPreConnection();
        params.put("username", account);
        params.put("password", password);
        params.put("nickname", nickname);
        params.put("avatar", avatar);
        OkGo.post(getAbsoluteUrl("/Api/Auth/register"))
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(BaseRequest request) {
                        super.onBefore(request);
                        callback.onPreConnection();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        callback.onCloseConnection();
                        try {
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
                            callback.onSuccess(res);
                        } catch (Exception e) {
                            callback.onException(e);
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        callback.onCloseConnection();
                        callback.onFailure(e.getLocalizedMessage());
                    }
                });
    }

    public static void confirmSMSCode(String phone, String code, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", phone);
        params.put("code", code);
        String sign = MD5Util.getMD5String(MD5Util.getMD5String("code=" + code + "&phone=" + phone)+""+TOKEN);
        params.put("sign", sign);
        OkGo.post(getAbsoluteUrl("/Api/Sms/verify"))
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(BaseRequest request) {
                        super.onBefore(request);
                        callback.onPreConnection();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        callback.onCloseConnection();
                        try {
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
                            callback.onSuccess(res);
                        } catch (Exception e) {
                            callback.onException(e);
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        callback.onCloseConnection();
                        callback.onFailure(e.getLocalizedMessage());
                    }
                });
    }

    public static void sendSmsCode(final String phone, String type, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", phone);
        params.put("type", type);
        String sign = MD5Util.getMD5String(MD5Util.getMD5String("phone="+phone+"&type="+type)+""+TOKEN);
        params.put("sign", sign);
        OkGo.post(getAbsoluteUrl("/Api/Sms/send"))
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(BaseRequest request) {
                        super.onBefore(request);
                        callback.onPreConnection();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        callback.onCloseConnection();
                        try {
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
                            callback.onSuccess(res);
                        } catch (Exception e) {
                            callback.onException(e);
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        callback.onCloseConnection();
                        callback.onFailure(e.getLocalizedMessage());
                    }
                });
    }

    public static void resetPassword(String account, String code, String password, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("phone", account);
        params.put("code", code);
        params.put("newpassword", password);
        OkGo.post(getAbsoluteUrl("/Api/Auth/resetPassword"))
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(BaseRequest request) {
                        super.onBefore(request);
                        callback.onPreConnection();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        callback.onCloseConnection();
                        try {
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
                            callback.onSuccess(res);
                        } catch (Exception e) {
                            callback.onException(e);
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        callback.onCloseConnection();
                        callback.onFailure(e.getLocalizedMessage());
                    }
                });
    }
}

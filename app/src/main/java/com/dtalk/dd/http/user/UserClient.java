package com.dtalk.dd.http.user;


import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.apache.http.Header;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Donal on 16/4/26.
 */
public class UserClient extends BaseClient {

    public static void updateUserPush(String client_id, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("client_id", (client_id));
        params.put("platform", (String.valueOf(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID_VALUE)));
        OkGo.post(getAbsoluteUrl("/Api/Member/updateUserPush"))
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onBefore(BaseRequest request) {
                        super.onBefore(request);
                        if (callback != null)
                            callback.onPreConnection();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        if (callback != null)
                            callback.onCloseConnection();
                        try {
                            BaseResponse res = JSON.parseObject((s), BaseResponse.class);
                            if (res.getStatus() == 1)
                                SandboxUtils.getInstance().set(IMApplication.getInstance(), IMLoginManager.instance().getLoginId() + "-regId", "1");
                            if (callback != null)
                                callback.onSuccess(res);
                        } catch (Exception e) {
                            if (callback != null)
                                callback.onException(e);
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        if (callback != null) {
                            callback.onCloseConnection();
                            callback.onFailure(e.getLocalizedMessage());
                        }
                    }
                });
    }

    public static void doLogin(String username, String password, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("username", username);
        params.put("password", password);
        OkGo.post(getAbsoluteUrl("/Api/Auth/login"))
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
                            UserInfo res = JSON.parseObject(s, UserInfo.class);
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

    public static void updateUserByJson(String json, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("json", json);
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        OkGo.post(getAbsoluteUrl("/Api/Member/updateProfile"))
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
                        callback.onSuccess(s);
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

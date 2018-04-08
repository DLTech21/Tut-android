package com.dtalk.dd.http.user;


import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.utils.SandboxUtils;
import com.lzy.okgo.model.HttpParams;

/**
 * Created by Donal on 16/4/26.
 */
public class UserClient extends BaseClient {

    public static void updateUserPush(String client_id, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("client_id", (client_id));
        params.put("platform", (String.valueOf(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID_VALUE)));
        postRequest(getAbsoluteUrl("/Api/Member/updateUserPush"), params, new ClientCallback() {
            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    BaseResponse res = JSON.parseObject((String) data, BaseResponse.class);
                    if (res.getStatus() == 1)
                        SandboxUtils.getInstance().set(IMApplication.getInstance(), IMLoginManager.instance().getLoginId() + "-regId", "1");
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

    public static void doLogin(String username, String password, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("username", username);
        params.put("password", password);
        postRequest(getAbsoluteUrl("/Api/Auth/login"), params, new ClientCallback() {
            @Override
            public void onPreConnection() {
                super.onPreConnection();
                onStart(callback);
            }

            @Override
            public void onCloseConnection() {
                super.onCloseConnection();
                onFinish(callback);
            }

            @Override
            public void onSuccess(Object data) {
                try {
                    UserInfo res = JSON.parseObject((String) data, UserInfo.class);
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

    public static void updateUserByJson(String json, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("json", json);
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        postRequest(getAbsoluteUrl("/Api/Member/updateProfile"), params, new ClientCallback() {
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
                onRequestSuccess(callback, data);
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

    public static void updateName(String id, String name, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("id", id);
        params.put("name", name);
        postRequest(getAbsoluteUrl("/Api/Member/changeGroupName"), params, new ClientCallback() {
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
                onRequestSuccess(callback, data);
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

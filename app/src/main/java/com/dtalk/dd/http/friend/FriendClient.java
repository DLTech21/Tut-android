package com.dtalk.dd.http.friend;


import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;

import okhttp3.Response;

/**
 * Created by Donal on 16/4/27.
 */
public class FriendClient extends BaseClient {


    public static void applyFriend(String tid, String msg, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("friendid", tid);
        params.put("avatar", IMLoginManager.instance().getLoginInfo().getAvatar());
        params.put("nickname", IMLoginManager.instance().getLoginInfo().getMainName());
        params.put("msg", msg);
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        postRequest(getAbsoluteUrl("/Api/Friend/apply"), params, new ClientCallback() {
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

    public static void confirmFriend(String tid, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("friendid", (tid));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        postRequest(getAbsoluteUrl("/Api/Friend/confirm"), params, new ClientCallback() {
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

    public static void getFriendList(final ClientCallback callback) {
        HttpParams params = new HttpParams();
        onStart(callback);
        params.put("uid", IMLoginManager.instance().getLoginId());
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        try {
            Response response = OkGo.post(getAbsoluteUrl("/Api/Friend/get")).params(params).execute();
            if (response.isSuccessful()) {
                try {
                    FriendListResp res = JSON.parseObject(response.body().string(), FriendListResp.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    onRequestException(callback, e);
                }
            } else {

            }
            onFinish(callback);
        } catch (Exception e) {
            onFinish(callback);
            onRequestException(callback, e);
        }
    }

    public static void getFriendInfo(String username, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", IMLoginManager.instance().getLoginId());
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        if (StringUtils.isMobileNO(username)) {
            params.put("friendname", username);
        } else {
            params.put("friendid", username);
        }
        postRequest(getAbsoluteUrl("/Api/Friend/search"), params, new ClientCallback() {
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
                    OtherUserInfoNoRemark res = JSON.parseObject((String) data, OtherUserInfoNoRemark.class);
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

package com.dtalk.dd.http.friend;


import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.Logger;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Donal on 16/4/27.
 */
public class FriendClient extends BaseClient {


    public static void applyFriend(String tid, String msg, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("friendid", tid);
        params.put("avatar", IMLoginManager.instance().getLoginInfo().getAvatar());
        params.put("nickname", IMLoginManager.instance().getLoginInfo().getMainName());
        params.put("msg", msg);
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        OkGo.post(getAbsoluteUrl("/Api/Friend/apply"))
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

    public static void confirmFriend(String tid, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", (String.valueOf(IMLoginManager.instance().getLoginId())));
        params.put("friendid", (tid));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        OkGo.post(getAbsoluteUrl("/Api/Friend/confirm"))
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

    public static void getFriendList(final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        callback.onPreConnection();
        params.put("uid", IMLoginManager.instance().getLoginId());
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        try {
            Response response = OkGo.post(getAbsoluteUrl("/Api/Friend/get")).params(params).execute();
            if (response.isSuccessful()) {
                try {
                    FriendListResp res = JSON.parseObject(response.body().string(), FriendListResp.class);
                    callback.onSuccess(res);
                } catch (Exception e) {
                    callback.onException(e);
                }
            } else {

            }
        } catch (Exception e) {
            callback.onException(e);
        }
    }

    public static void getFriendInfo(String username, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        callback.onPreConnection();
        params.put("uid", IMLoginManager.instance().getLoginId());
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        if (StringUtils.isMobileNO(username)) {
            params.put("friendname", username);
        } else {
            params.put("friendid", username);
        }
        OkGo.post(getAbsoluteUrl("/Api/Friend/search"))
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
                            Logger.e(s);
                            JSONObject jsonObject = new JSONObject(s);
                            if (jsonObject.has("remarks")) {
                                OtherUserInfo res = JSON.parseObject(s, OtherUserInfo.class);
                                callback.onSuccess(res);
                            } else {
                                OtherUserInfoNoRemark res = JSON.parseObject(s, OtherUserInfoNoRemark.class);
                                callback.onSuccess(res);
                            }
                        } catch (Exception e) {
                            Logger.e(e.getLocalizedMessage());
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

package com.dtalk.dd.http.moment;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.util.LogUtils;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Donal on 16/7/29.
 */
public class MomentClient extends BaseClient {

    public static void fetchMoment(final String username, String token, final String last, String limit, final BaseClient.ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("last", last);
        params.put("limit", limit);
        OkGo.post(getAbsoluteUrl("/Api/Moment/get"))
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
                            MomentList data = JSON.parseObject(s, MomentList.class);
                            if (data.status == 1) {
                                for (int i = 0; i < data.list.size(); i++) {
                                    Moment m = data.list.get(i);
                                    boolean isFavor = false;
                                    m.like_maps = new HashMap<String, UserInfo>();
                                    for (UserInfo user : m.like_users) {
                                        m.like_maps.put(user.getUid(), user);
                                    }
                                    if (m.like_maps.get(String.valueOf(IMLoginManager.instance().getLoginId())) != null) {
                                        isFavor = true;
                                    }
                                    m.isFavor = isFavor;
                                }
                            }
                            callback.onSuccess(data);
                        } catch (Exception e) {
                            e.printStackTrace();
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

    public static void postImageMoment(String username, String token, String txt, List<String> image, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("type", "txt");
        params.put("content", txt);
        params.put("image", new Gson().toJson(image));
        OkGo.post(getAbsoluteUrl("/Api/Moment/add"))
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

    public static void postVideoMoment(String username, String token, String videoUrl, String videoCover, String localPath, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("type", "video");
        params.put("content", videoUrl);
        params.put("cover", videoCover);
        params.put("image", localPath);
        OkGo.post(getAbsoluteUrl("/Api/Moment/add"))
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

    public static void deleteMoment(String username, String token, String id, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("id", id);
        OkGo.post(getAbsoluteUrl("/Api/Moment/del"))
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
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
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

    public static void commentMoment(String id, String replyname, String comment, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        params.put("reply_uid", replyname);
        params.put("comment", comment);
        OkGo.post(getAbsoluteUrl("/Api/Moment/addComment"))
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
                            Comment data = Comment.parse(s);
                            callback.onSuccess(data);
                        } catch (Exception e) {
                            e.printStackTrace();
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

    public static void likeMoment(String id, boolean like, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        params.put("like", like ? "1" : "0");
        OkGo.post(getAbsoluteUrl("/Api/Moment/like"))
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
                            UserInfo res = JSON.parseObject(s, UserInfo.class);
                            callback.onSuccess(res);
                        } catch (Exception e) {
                            e.printStackTrace();
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

    public static void delcommentMoment(String id, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        OkGo.post(getAbsoluteUrl("/Api/Moment/delComment"))
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
                            BaseResponse res = JSON.parseObject(s, BaseResponse.class);
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

    public static void fetchOnesMoment(final String momentUsername, final String last, String limit, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("moment_uid", momentUsername);
        params.put("last", last);
        params.put("limit", limit);
        OkGo.post(getAbsoluteUrl("/Api/Moment/getOne"))
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
                            MomentList data = JSON.parseObject(s, MomentList.class);
                            if (data.status == 1) {
                                for (int i = 0; i < data.list.size(); i++) {
                                    Moment m = data.list.get(i);
                                    boolean isFavor = false;
                                    m.like_maps = new HashMap<String, UserInfo>();
                                    for (UserInfo user : m.like_users) {
                                        m.like_maps.put(user.getUid(), user);
                                    }
                                    if (m.like_maps.get(String.valueOf(IMLoginManager.instance().getLoginId())) != null) {
                                        isFavor = true;
                                    }
                                    m.isFavor = isFavor;
                                }
                            }
                            if (last.equals("0")) {
                                SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), data, "moments-"+momentUsername);
                            }
                            callback.onSuccess(data);
                        } catch (Exception e) {
                            e.printStackTrace();
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
}

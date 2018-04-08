package com.dtalk.dd.http.moment;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.SandboxUtils;
import com.google.gson.Gson;
import com.lzy.okgo.model.HttpParams;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Donal on 16/7/29.
 */
public class MomentClient extends BaseClient {

    public static void fetchMoment(final String username, String token, final String last, String limit, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("last", last);
        params.put("limit", limit);
        postRequest(getAbsoluteUrl("/Api/Moment/get"), params, new ClientCallback() {
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
            public void onSuccess(Object s) {
                try {
                    MomentList data = JSON.parseObject((String) s, MomentList.class);
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
                    onRequestSuccess(callback, data);
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static void postImageMoment(String username, String token, String txt, List<String> image, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("type", "txt");
        params.put("content", txt);
        params.put("image", new Gson().toJson(image));
        postRequest(getAbsoluteUrl("/Api/Moment/add"), params, new ClientCallback() {
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
            public void onSuccess(Object s) {
                try {
                    BaseResponse res = JSON.parseObject((String) s, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static void postVideoMoment(String username, String token, String videoUrl, String videoCover, String localPath, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("type", "video");
        params.put("content", videoUrl);
        params.put("cover", videoCover);
        params.put("image", localPath);
        postRequest(getAbsoluteUrl("/Api/Moment/add"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    BaseResponse res = JSON.parseObject((String) s, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }

    public static void deleteMoment(String username, String token, String id, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", username);
        params.put("token", token);
        params.put("id", id);
        postRequest(getAbsoluteUrl("/Api/Moment/del"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    BaseResponse res = JSON.parseObject((String) s, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }

    public static void commentMoment(String id, String replyname, String comment, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        params.put("reply_uid", replyname);
        params.put("comment", comment);
        postRequest(getAbsoluteUrl("/Api/Moment/addComment"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    Comment data = Comment.parse((String) s);
                    onRequestSuccess(callback, data);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }

    public static void likeMoment(String id, boolean like, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        params.put("like", like ? "1" : "0");
        postRequest(getAbsoluteUrl("/Api/Moment/like"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    UserInfo res = JSON.parseObject((String) s, UserInfo.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }

    public static void delcommentMoment(String id, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("id", id);
        postRequest(getAbsoluteUrl("/Api/Moment/delComment"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    BaseResponse res = JSON.parseObject((String) s, BaseResponse.class);
                    onRequestSuccess(callback, res);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }

    public static void fetchOnesMoment(final String momentUsername, final String last, String limit, final ClientCallback callback) {
        HttpParams params = new HttpParams();
        params.put("uid", String.valueOf(IMLoginManager.instance().getLoginId()));
        params.put("token", SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"));
        params.put("moment_uid", momentUsername);
        params.put("last", last);
        params.put("limit", limit);
        postRequest(getAbsoluteUrl("/Api/Moment/getOne"), params, new ClientCallback() {
            @Override
            public void onSuccess(Object s) {
                try {
                    MomentList data = JSON.parseObject((String) s, MomentList.class);
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
                        SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), data, "moments-" + momentUsername);
                    }
                    onRequestSuccess(callback, data);
                } catch (Exception e) {
                    e.printStackTrace();
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
        });
    }
}

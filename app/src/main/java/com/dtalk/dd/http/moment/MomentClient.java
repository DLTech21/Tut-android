package com.dtalk.dd.http.moment;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.utils.Logger;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Donal on 16/7/29.
 */
public class MomentClient extends BaseClient{

    public static void fetchMoment(String username, String token, final String last, String limit, final BaseClient.ClientCallback callback) {
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
                        try
                        {
                            MomentList data = JSON.parseObject(s, MomentList.class);
                            callback.onSuccess(data);
                        } catch (Exception e)
                        {
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

}

package com.dtalk.dd.http.moment;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.http.base.BaseClient;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.BaseRequest;

import org.apache.http.Header;

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

}

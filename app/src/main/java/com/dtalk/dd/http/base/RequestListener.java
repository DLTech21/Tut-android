package com.dtalk.dd.http.base;

/**
 * Created by donal on 2018/4/8.
 */

public interface RequestListener {
    void onSuccess(Object data);

    void onFailure(String message);

    void onException(Exception e);
}

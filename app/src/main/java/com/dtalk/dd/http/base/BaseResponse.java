package com.dtalk.dd.http.base;

/**
 * Created by Donal on 16/4/19.
 */
public class BaseResponse {

    private int status;
    private String msg;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

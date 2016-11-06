package com.dtalk.dd.http.user;

/**
 * Created by Donal on 16/4/26.
 */
public class UserResp {
    private int status;
    private String msg;
    private UserInfo data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }
}

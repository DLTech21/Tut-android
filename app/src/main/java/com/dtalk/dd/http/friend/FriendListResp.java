package com.dtalk.dd.http.friend;

import com.dtalk.dd.http.user.UserInfo;

import java.util.List;

/**
 * Created by Donal on 16/4/28.
 */
public class FriendListResp {
    private int status;
    private String msg;
    private List<UserInfo> list;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<UserInfo> getList() {
        return list;
    }

    public void setList(List<UserInfo> list) {
        this.list = list;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

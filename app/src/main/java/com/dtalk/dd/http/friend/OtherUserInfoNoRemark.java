package com.dtalk.dd.http.friend;

import java.io.Serializable;

/**
 * Created by Donal on 16/7/19.
 */
public class OtherUserInfoNoRemark implements Serializable{
    private int status;
    private String msg;
    private String nickname;
    private String avatar;
    private String uid;
    private String sex;
    private String signature;
    private String friend;
    private String username;
    private String area;
    private String moment_cover;

    public String getMoment_cover() {
        return moment_cover;
    }

    public void setMoment_cover(String moment_cover) {
        this.moment_cover = moment_cover;
    }

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}

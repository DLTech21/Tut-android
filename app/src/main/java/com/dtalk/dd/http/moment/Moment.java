package com.dtalk.dd.http.moment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dtalk.dd.http.user.UserInfo;

public class Moment implements Serializable{
    public String moment_id;
    public String username;
    public String uid;
    public String content;
    public List<UserInfo> like_users;//只需要用userbean的nickname username avatar
    public String created;
    public String nickname;
    public String avatar;
//    public Media media;
    public List<Comment> comment;
    public String type;
    
    public String title;
    public String cover;
    public String url;
    public List<String> image = new ArrayList<String>();
}

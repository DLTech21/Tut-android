package com.dtalk.dd.http.moment;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Comment implements Serializable{

    public String comment_id;
    public String moment_id;
    public String uid;
    public String username;
    public String avatar;
    public String reply_username;
    public String content;
    public String created;
    public String nickname;
    public String reply_nickname;
    public String reply_uid;
    public int status;
    public String msg;
    
   
    @Override
    public String toString() {
        return "Comment [comment_id=" + comment_id + ", moment_id=" + moment_id
                + ", username=" + username + ", replyname=" + reply_username
                + ", content=" + content + ", created=" + created
                + ", nickname=" + nickname + ", reply_nickname="
                + reply_nickname + ", uid=" + uid + ", reply_uid=" + reply_uid
                + ", status=" + status + ", msg=" + msg + "]";
    }


    public static Comment parse(String json)
    {
        Comment data = new Comment();
        Gson gson = new Gson();
        data = gson.fromJson(json, new TypeToken<Comment>()
        {
        }.getType());
        return data;
    }
}

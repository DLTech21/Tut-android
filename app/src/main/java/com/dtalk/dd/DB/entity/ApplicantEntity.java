package com.dtalk.dd.DB.entity;

/**
 * Created by Donal on 16/4/27.
 */
public class ApplicantEntity {
    private Long id;

    /** Not-null value. */
    private int uid;
    private String avatar;
    private String nickname;
    private String msg;
    private int type;
    private int response;

    private String content;
    private int msgId;
    private int fromUserId;
    private int msgType;
    private int created;
    private int toSessionId;
    private int toGroupId;
    public ApplicantEntity() {
    }

    public ApplicantEntity(Long id) {
        this.id = id;
    }

    public ApplicantEntity(Long id, int uid, String avatar, String nickname, String msg, int type, int response) {
        this.id = id;
        this.uid = uid;
        this.avatar = avatar;
        this.nickname = nickname;
        this.msg = msg;
        this.type = type;
        this.response = response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getToSessionId() {
        return toSessionId;
    }

    public void setToSessionId(int toSessionId) {
        this.toSessionId = toSessionId;
    }

    public int getToGroupId() {
        return toGroupId;
    }

    public void setToGroupId(int toGroupId) {
        this.toGroupId = toGroupId;
    }
}

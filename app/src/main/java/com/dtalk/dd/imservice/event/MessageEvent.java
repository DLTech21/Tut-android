package com.dtalk.dd.imservice.event;

import com.dtalk.dd.DB.entity.MessageEntity;

import java.util.ArrayList;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 */
public class MessageEvent {

    private ArrayList<MessageEntity> msgList;
    private Event event;

    public MessageEvent(){
    }

    public MessageEvent(Event event){
        //默认值 初始化使用
        this.event = event;
    }

    public MessageEvent(Event event,MessageEntity entity){
        //默认值 初始化使用
        this.event = event;
        msgList = new ArrayList<>(1);
        msgList.add(entity);
    }

    public enum Event{
      NONE,
      HISTORY_MSG_OBTAIN,

      SENDING_MESSAGE,

      ACK_SEND_MESSAGE_OK,
       ACK_SEND_MESSAGE_TIME_OUT,
      ACK_SEND_MESSAGE_FAILURE,

      HANDLER_IMAGE_UPLOAD_FAILD,
        IMAGE_UPLOAD_FAILD,
        HANDLER_IMAGE_UPLOAD_SUCCESS,
        IMAGE_UPLOAD_SUCCESS,

        HANDLER_FILE_UPLOAD_FAILD,
        FILE_UPLOAD_FAILD,
        HANDLER_FILE_UPLOAD_SUCCESS,
        FILE_UPLOAD_SUCCESS,

        HANDLER_VIDEO_UPLOAD_FAILD,
        VIDEO_UPLOAD_FAILD,
        HANDLER_VIDEO_UPLOAD_SUCCESS,
        VIDEO_UPLOAD_SUCCESS
     }

    public MessageEntity getMessageEntity() {
        if(msgList == null || msgList.size() <=0){
            return null;
        }
        return msgList.get(0);
    }

    public void setMessageEntity(MessageEntity messageEntity) {
        if(msgList == null){
            msgList = new ArrayList<>();
        }
        msgList.clear();
        msgList.add(messageEntity);
    }

    public ArrayList<MessageEntity> getMsgList() {
        return msgList;
    }

    public void setMsgList(ArrayList<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}

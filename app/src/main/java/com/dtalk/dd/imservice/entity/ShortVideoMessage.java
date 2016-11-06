package com.dtalk.dd.imservice.entity;

import android.util.Base64;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.PeerEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.imservice.support.SequenceNumberMaker;
import com.dtalk.dd.utils.FileUtil;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by Donal on 16/6/13.
 */
public class ShortVideoMessage  extends MessageEntity implements Serializable {
    /**本地保存的path*/
    private String video_path = "";
    private String video_cover = "";
    /**图片的网络地址*/
    private String video_path_url = "";
    private String video_cover_url = "";
    private int loadStatus;

    public ShortVideoMessage(){
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    /**消息拆分的时候需要*/
    private ShortVideoMessage(MessageEntity entity){
        /**父类的id*/
        id =  entity.getId();
        msgId  = entity.getMsgId();
        fromId = entity.getFromId();
        toId   = entity.getToId();
        sessionKey = entity.getSessionKey();
        content=entity.getContent();
        msgType=entity.getMsgType();
        displayType=entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    /**接受到网络包，解析成本地的数据*/
    public static ShortVideoMessage parseFromNet(MessageEntity entity) throws JSONException {
        String strContent = entity.getContent();
        // 判断开头与结尾
        ShortVideoMessage shortVideoMessage = new ShortVideoMessage(entity);
        shortVideoMessage.setDisplayType(DBConstant.SHOW_VIDEO_TYPE);
        ShortVideoEntity shortVideoEntity = new Gson().fromJson(strContent, ShortVideoEntity.class);
        shortVideoMessage.setVideo_cover_url(shortVideoEntity.getVideo_cover_url());
        shortVideoMessage.setVideo_path_url(shortVideoEntity.getVideo_path_url());
        shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        shortVideoMessage.setStatus(MessageConstant.MSG_SUCCESS);
        return shortVideoMessage;
    }

    public static ShortVideoMessage parseFromDB(MessageEntity entity)  {
        if(entity.getDisplayType() != DBConstant.SHOW_VIDEO_TYPE){
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
        }
        ShortVideoMessage shortVideoMessage = new ShortVideoMessage(entity);
        String originContent = entity.getContent();

        ShortVideoEntity shortVideoEntity = new Gson().fromJson(originContent, ShortVideoEntity.class);
        shortVideoMessage.setVideo_cover_url(shortVideoEntity.getVideo_cover_url());
        shortVideoMessage.setVideo_path_url(shortVideoEntity.getVideo_path_url());

        int loadStatus = shortVideoEntity.getLoadStatus();
        if(loadStatus == MessageConstant.IMAGE_LOADING){
            loadStatus = MessageConstant.IMAGE_UNLOAD;
        }
        shortVideoMessage.setLoadStatus(loadStatus);

        return shortVideoMessage;
    }

    public static ShortVideoMessage buildForSend(String path, String coverPath, UserEntity fromUser,PeerEntity peerEntity){
        ShortVideoMessage shortVideoMessage = new ShortVideoMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        shortVideoMessage.setFromId(fromUser.getPeerId());
        shortVideoMessage.setToId(peerEntity.getPeerId());
        shortVideoMessage.setUpdated(nowTime);
        shortVideoMessage.setCreated(nowTime);
        shortVideoMessage.setDisplayType(DBConstant.SHOW_VIDEO_TYPE);
        shortVideoMessage.setVideo_path(path);
        shortVideoMessage.setVideo_cover(coverPath);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_VIDEO
                : DBConstant.MSG_TYPE_SINGLE_VIDEO;
        shortVideoMessage.setMsgType(msgType);

        shortVideoMessage.setStatus(MessageConstant.MSG_SENDING);
        shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        shortVideoMessage.buildSessionKey(true);

        ShortVideoEntity shortVideoEntity = new ShortVideoEntity();
        shortVideoEntity.setLoadStatus(shortVideoMessage.getLoadStatus());
        shortVideoEntity.setVideo_path(shortVideoMessage.getVideo_path());
        shortVideoEntity.setVideo_cover(shortVideoMessage.getVideo_cover());
        shortVideoMessage.setContent(new Gson().toJson(shortVideoEntity));

        return shortVideoMessage;
    }

    /**
     * Not-null value.
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {

        try {
            String base64String = new String(Base64.encode(content.getBytes("utf-8"), Base64.DEFAULT));
            String encrySendContent =new String(com.dtalk.dd.Security.getInstance().EncryptMsg(base64String));
            return encrySendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public String getVideo_cover() {
        return video_cover;
    }

    public void setVideo_cover(String video_cover) {
        this.video_cover = video_cover;
    }

    public String getVideo_path_url() {
        return video_path_url;
    }

    public void setVideo_path_url(String video_path_url) {
        this.video_path_url = video_path_url;
    }

    public String getVideo_cover_url() {
        return video_cover_url;
    }

    public void setVideo_cover_url(String video_cover_url) {
        this.video_cover_url = video_cover_url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}

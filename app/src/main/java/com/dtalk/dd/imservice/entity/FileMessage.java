package com.dtalk.dd.imservice.entity;

import android.util.Base64;

import com.google.gson.Gson;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.PeerEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.imservice.support.SequenceNumberMaker;
import com.dtalk.dd.utils.FileUtil;

import org.json.JSONException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Created by Donal on 16/4/22.
 */
public class FileMessage extends MessageEntity implements Serializable {

    /**
     * 本地保存的path
     */
    private String path = "";
    private String fileName = "";
    private String ext = "";
    /**
     * 图片的网络地址
     */
    private String url = "";
    private int loadStatus;

    public FileMessage() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    /**
     * 消息拆分的时候需要
     */
    private FileMessage(MessageEntity entity) {
        /**父类的id*/
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    /**
     * 接受到网络包，解析成本地的数据
     */
    public static FileMessage parseFromNet(MessageEntity entity) throws JSONException {
        String strContent = entity.getContent();
        // 判断开头与结尾
        FileMessage fileMessage = new FileMessage(entity);
        fileMessage.setDisplayType(DBConstant.SHOW_FIEL_TYPE);
        FileEntity fileEntity = new Gson().fromJson(strContent, FileEntity.class);
        fileMessage.setUrl(fileEntity.getUrl());
        fileMessage.setFileName(fileEntity.getFileName());
        fileMessage.setExt(fileEntity.getExt());
        fileMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        fileMessage.setStatus(MessageConstant.MSG_SUCCESS);
        fileMessage.setDisplayType(MsgAnalyzeEngine.fileMessageGif(fileMessage.getUrl()));
        return fileMessage;
    }


    public static FileMessage parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_FIEL_TYPE && entity.getDisplayType() != DBConstant.SHOW_GIF_FILE_TYPE) {
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE or SHOW_GIF_FILE_TYPE");
        }
        FileMessage fileMessage = new FileMessage(entity);
        String originContent = entity.getContent();

        FileEntity fileEntity = new Gson().fromJson(originContent, FileEntity.class);
        fileMessage.setUrl(fileEntity.getUrl());
        fileMessage.setFileName(fileEntity.getFileName());
        fileMessage.setExt(fileEntity.getExt());

        int loadStatus = fileEntity.getLoadStatus();
        if (loadStatus == MessageConstant.IMAGE_LOADING) {
            loadStatus = MessageConstant.IMAGE_UNLOAD;
        }
        fileMessage.setLoadStatus(loadStatus);
        fileMessage.setDisplayType(MsgAnalyzeEngine.fileMessageGif(fileMessage.getUrl()));
        return fileMessage;
    }

    public static FileMessage buildForSend(String path, UserEntity fromUser, PeerEntity peerEntity) {
        FileMessage fileMessage = new FileMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        fileMessage.setFromId(fromUser.getPeerId());
        fileMessage.setToId(peerEntity.getPeerId());
        fileMessage.setUpdated(nowTime);
        fileMessage.setCreated(nowTime);
        fileMessage.setDisplayType(DBConstant.SHOW_FIEL_TYPE);
        fileMessage.setPath(path);
        fileMessage.setFileName(FileUtil.getFilename(path));
        fileMessage.setExt(FileUtil.getExtensionName(path));
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_FILE
                : DBConstant.MSG_TYPE_SINGLE_FILE;
        fileMessage.setMsgType(msgType);

        fileMessage.setStatus(MessageConstant.MSG_SENDING);
        fileMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        fileMessage.buildSessionKey(true);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileMessage.getFileName());
        fileEntity.setExt(fileMessage.getExt());
        fileEntity.setLoadStatus(fileMessage.getLoadStatus());
        fileEntity.setPath(fileMessage.getPath());
        fileMessage.setContent(new Gson().toJson(fileEntity));

        return fileMessage;
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
            String encrySendContent = new String(com.dtalk.dd.Security.getInstance().EncryptMsg(base64String));
            return encrySendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}

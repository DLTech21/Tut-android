package com.dtalk.dd.imservice.entity;

import android.text.TextUtils;
import android.util.Base64;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.Security;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.protobuf.IMBaseDefine;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 *
 * historical reasons,没有充分利用msgType字段
 * 多端的富文本的考虑
 */
public class MsgAnalyzeEngine {
    public static String analyzeMessageDisplay(String content){
        String finalRes = content;
        String originContent = content;
        while (!originContent.isEmpty()) {
            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
            if (nStart < 0) {// 没有头
                break;
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
                if (nEnd < 0) {// 没有尾
                    String strSplitString = originContent;
                    break;
                } else {// 匹配到
                    String pre = originContent.substring(0, nStart);

                    originContent = subContentString.substring(nEnd
                            + MessageConstant.IMAGE_MSG_END.length());

                    if(!TextUtils.isEmpty(pre) || !TextUtils.isEmpty(originContent)){
                        finalRes = DBConstant.DISPLAY_FOR_MIX;
                    }else{
                        finalRes = DBConstant.DISPLAY_FOR_IMAGE;
                    }
                }
            }
        }
        return finalRes;
    }


    // 抽离放在同一的地方
    public static MessageEntity analyzeMessage(IMBaseDefine.MsgInfo msgInfo) {
       MessageEntity messageEntity = new MessageEntity();

       messageEntity.setCreated(msgInfo.getCreateTime());
       messageEntity.setUpdated(msgInfo.getCreateTime());
       messageEntity.setFromId(msgInfo.getFromSessionId());
       messageEntity.setMsgId(msgInfo.getMsgId());
       messageEntity.setMsgType(ProtoBuf2JavaBean.getJavaMsgType(msgInfo.getMsgType()));
       messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
       messageEntity.setContent(msgInfo.getMsgData().toStringUtf8());
        /**
         * 解密文本信息
         */
       String desMessage = new String(Base64.decode(Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()), Base64.DEFAULT));
       messageEntity.setContent(desMessage);

        switch (msgInfo.getMsgType()){
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                return TextMessage.parseFromNet(messageEntity);

            case MSG_TYPE_GROUP_IMAGE:
            case MSG_TYPE_SINGLE_IMAGE:
                try {
                    ImageMessage imageMessage =  ImageMessage.parseFromNet(messageEntity);
                    return imageMessage;
                } catch (JSONException e) {
                    // e.printStackTrace();
                    return null;
                }

            case MSG_TYPE_SINGLE_LOCATION:
            case MSG_TYPE_GROUP_LOCATION:
                return LocationMessage.parseFromNet(messageEntity);

            case MSG_TYPE_SINGLE_URL:
            case MSG_TYPE_GROUP_URL:

            case MSG_TYPE_SINGLE_FILE:
            case MSG_TYPE_GROUP_FILE:
                try {
                    return FileMessage.parseFromNet(messageEntity);
                } catch (JSONException e) {
                    // e.printStackTrace();
                    return null;
                }

            case MSG_TYPE_SINGLE_VIDEO:
            case MSG_TYPE_GROUP_VIDEO:
                try {
                    return ShortVideoMessage.parseFromNet(messageEntity);
                } catch (JSONException e) {
                    // e.printStackTrace();
                    return null;
                }

            case MSG_TYPE_GROUP_EMOTION:
            case MSG_TYPE_SINGLE_EMOTION:
                return EmotionMessage.parseFromNet(messageEntity);

            default:
                return TextMessage.parseFromNet(messageEntity);
        }
    }


    /**
     * todo 优化字符串分析
     * @param msg
     * @return
     */
    private static List<MessageEntity> textDecode(MessageEntity msg){
        List<MessageEntity> msgList = new ArrayList<>();

        String originContent = msg.getContent();
        while (!TextUtils.isEmpty(originContent)) {
            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
            if (nStart < 0) {// 没有头
                String strSplitString = originContent;

                MessageEntity entity = addMessage(msg, strSplitString);
                if(entity!=null){
                    msgList.add(entity);
                }

                originContent = "";
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
                if (nEnd < 0) {// 没有尾
                    String strSplitString = originContent;


                    MessageEntity entity = addMessage(msg,strSplitString);
                    if(entity!=null){
                        msgList.add(entity);
                    }

                    originContent = "";
                } else {// 匹配到
                    String pre = originContent.substring(0, nStart);
                    MessageEntity entity1 = addMessage(msg,pre);
                    if(entity1!=null){
                        msgList.add(entity1);
                    }

                    String matchString = subContentString.substring(0, nEnd
                            + MessageConstant.IMAGE_MSG_END.length());

                    MessageEntity entity2 = addMessage(msg,matchString);
                    if(entity2!=null){
                        msgList.add(entity2);
                    }

                    originContent = subContentString.substring(nEnd
                            + MessageConstant.IMAGE_MSG_END.length());
                }
            }
        }

        return msgList;
    }


    public static MessageEntity addMessage(MessageEntity msg,String strContent) {
        if (TextUtils.isEmpty(strContent.trim())){
            return null;
        }
        msg.setContent(strContent);

        if (strContent.startsWith(MessageConstant.IMAGE_MSG_START)
                && strContent.endsWith(MessageConstant.IMAGE_MSG_END)) {
            try {
                ImageMessage imageMessage =  ImageMessage.parseFromNet(msg);
                return imageMessage;
            } catch (JSONException e) {
                // e.printStackTrace();
                return null;
            }
        } else {
           return TextMessage.parseFromNet(msg);
        }
    }

    public static int imageMessageGif(String url) {
        if (url.contains(".gif")) {
            return DBConstant.SHOW_GIF_OTHER_TYPE;
        }
        return DBConstant.SHOW_IMAGE_TYPE;
    }

}

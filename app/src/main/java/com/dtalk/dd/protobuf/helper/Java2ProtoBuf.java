package com.dtalk.dd.protobuf.helper;

import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.protobuf.IMBaseDefine;

/**
 * @author : yingmu on 15-1-6.
 * @email : yingmu@mogujie.com.
 */
public class Java2ProtoBuf {
    /**----enum 转化接口--*/
    public static IMBaseDefine.MsgType getProtoMsgType(int msgType){
        switch (msgType){
            case DBConstant.MSG_TYPE_GROUP_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_TEXT;
            case DBConstant.MSG_TYPE_GROUP_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUDIO;
            case DBConstant.MSG_TYPE_GROUP_LOCATION:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_LOCATION;
            case DBConstant.MSG_TYPE_GROUP_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_IMAGE;
            case DBConstant.MSG_TYPE_GROUP_URL:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_URL;
            case DBConstant.MSG_TYPE_GROUP_FILE:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_FILE;
            case DBConstant.MSG_TYPE_GROUP_VIDEO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_VIDEO;
            case DBConstant.MSG_TYPE_GROUP_EMOTION:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_EMOTION;

            case DBConstant.MSG_TYPE_SINGLE_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUDIO;
            case DBConstant.MSG_TYPE_SINGLE_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_TEXT;
            case DBConstant.MSG_TYPE_SINGLE_IMAGE:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_IMAGE;
            case DBConstant.MSG_TYPE_SINGLE_VIDEO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_VIDEO;
            case DBConstant.MSG_TYPE_SINGLE_LOCATION:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_LOCATION;
            case DBConstant.MSG_TYPE_SINGLE_URL:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_URL;
            case DBConstant.MSG_TYPE_SINGLE_FILE:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_FILE;
            case DBConstant.MSG_TYPE_SINGLE_EMOTION:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_EMOTION;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }


    public static IMBaseDefine.SessionType getProtoSessionType(int sessionType){
        switch (sessionType){
            case DBConstant.SESSION_TYPE_SINGLE:
                return IMBaseDefine.SessionType.SESSION_TYPE_SINGLE;
            case DBConstant.SESSION_TYPE_GROUP:
                return IMBaseDefine.SessionType.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }
}

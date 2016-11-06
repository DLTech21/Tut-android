
package com.dtalk.dd.imservice.manager;


import android.text.TextUtils;

import com.dtalk.dd.DB.entity.PeerEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.GroupEntity;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.SessionEntity;
import com.dtalk.dd.DB.sp.ConfigurationSp;
import com.dtalk.dd.imservice.entity.RecentInfo;
import com.dtalk.dd.imservice.entity.UnreadEntity;
import com.dtalk.dd.imservice.event.SessionEvent;
import com.dtalk.dd.protobuf.helper.EntityChangeEngine;
import com.dtalk.dd.protobuf.helper.Java2ProtoBuf;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.protobuf.IMBuddy;
import com.dtalk.dd.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * app显示首页
 * 最近联系人列表
 */
public class IMSessionManager extends IMManager {
    private static  IMSessionManager inst = new IMSessionManager();
    public static IMSessionManager instance() {
       return inst;
    }

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMLoginManager imLoginManager = IMLoginManager.instance();
    private DBInterface dbInterface = DBInterface.instance();
    private IMGroupManager groupManager = IMGroupManager.instance();

    // key = sessionKey -->  sessionType_peerId
    private  Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();
    //SessionManager 状态字段
    private boolean sessionListReady = false;

    @Override
    public void doOnStart() {}

    @Override
    public void reset() {
        sessionListReady = false;
        sessionMap.clear();
    }

    /**
     * 实现自身的事件驱动
     * @param event
     */
    public void triggerEvent(SessionEvent event) {
        switch (event){
            case RECENT_SESSION_LIST_SUCCESS:
                sessionListReady = true;
            break;
        }
        EventBus.getDefault().post(event);
    }

    public void onNormalLoginOk() {
        Logger.d("recent#onLogin Successful");
        onLocalLoginOk();
        onLocalNetOk();
    }

    public void onLocalLoginOk(){
        Logger.i("session#loadFromDb");
        List<SessionEntity>  sessionInfoList = dbInterface.loadAllSession();
        for(SessionEntity sessionInfo:sessionInfoList){
            sessionMap.put(sessionInfo.getSessionKey(), sessionInfo);
        }

        triggerEvent(SessionEvent.RECENT_SESSION_LIST_SUCCESS);
    }

    public void onLocalNetOk(){
        int latestUpdateTime = dbInterface.getSessionLastTime();
        reqGetRecentContacts(latestUpdateTime);
    }

    /**----------------------------分割线--------------------------------*/

    /**
     * 请求最近回话
     */
    private void reqGetRecentContacts(int latestUpdateTime) {
        Logger.i("session#reqGetRecentContacts");
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMRecentContactSessionReq recentContactSessionReq = IMBuddy.IMRecentContactSessionReq
                .newBuilder()
                .setLatestUpdateTime(latestUpdateTime)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_REQUEST_VALUE;
        imSocketManager.sendRequest(recentContactSessionReq, sid, cid);
    }

    /**
     * 最近回话返回
     * 与本地的进行merge
     * @param recentContactSessionRsp
     */
    public void onRepRecentContacts(IMBuddy.IMRecentContactSessionRsp recentContactSessionRsp) {
        Logger.i("session#onRepRecentContacts");
        int userId = recentContactSessionRsp.getUserId();
        List<IMBaseDefine.ContactSessionInfo> contactSessionInfoList = recentContactSessionRsp.getContactSessionListList();
        /**更新最近联系人列表*/

        ArrayList<SessionEntity> needDb = new ArrayList<>();
        for(IMBaseDefine.ContactSessionInfo sessionInfo:contactSessionInfoList){
            // 返回的没有主键Id
            SessionEntity sessionEntity = ProtoBuf2JavaBean.getSessionEntity(sessionInfo);
            //并没有按照时间来排序
            sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
            needDb.add(sessionEntity);
        }
        Logger.d("session#onRepRecentContacts is ready,now broadcast");

        //将最新的session信息保存在DB中
        dbInterface.batchInsertOrUpdateSession(needDb);
        if(needDb.size()>0){
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    /**
     * 请求删除会话
     */
    public  void reqRemoveSession(RecentInfo recentInfo) {
        Logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();
        String sessionKey = recentInfo.getSessionKey();
        /**直接本地先删除,清楚未读消息*/
        if(sessionMap.containsKey(sessionKey)){
            sessionMap.remove(sessionKey);
            IMUnreadMsgManager.instance().readUnreadSession(sessionKey);
            dbInterface.deleteSession(sessionKey);
            ConfigurationSp.instance(ctx,loginId).setSessionTop(sessionKey,false);
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }

        IMBuddy.IMRemoveSessionReq removeSessionReq = IMBuddy.IMRemoveSessionReq
                .newBuilder()
                .setUserId(loginId)
                .setSessionId(recentInfo.getPeerId())
                .setSessionType(Java2ProtoBuf.getProtoSessionType(recentInfo.getSessionType()))
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_REQ_VALUE;
        imSocketManager.sendRequest(removeSessionReq, sid, cid);
    }

    /**
     * 删除会话返回
     */
    public void onRepRemoveSession(IMBuddy.IMRemoveSessionRsp removeSessionRsp) {
        Logger.i("session#onRepRemoveSession");
        int resultCode = removeSessionRsp.getResultCode();
        if(0 != resultCode){
            Logger.e("session#removeSession failed");
            return;
        }
    }

    /**新建群组时候的更新 */
    public void updateSession(GroupEntity entity){
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setLatestMsgType(DBConstant.MSG_TYPE_GROUP_TEXT);
        sessionEntity.setUpdated(entity.getUpdated());
        sessionEntity.setCreated(entity.getCreated());
        sessionEntity.setLatestMsgData("[你创建的新群喔]");
        sessionEntity.setTalkId(entity.getCreatorId());
        sessionEntity.setLatestMsgId(0);
        sessionEntity.setPeerId(entity.getPeerId());
        sessionEntity.setPeerType(DBConstant.SESSION_TYPE_GROUP);
        sessionEntity.buildSessionKey();

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);
        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
    }


    /**
     * 1.自己发送消息
     * 2.收到消息
     * @param msg
     */
    public void updateSession(MessageEntity msg) {
        if (msg == null) {
            Logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }
        int loginId =imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);
        // 因为多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            Logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();
            // 判断群组的信息是否存在
            if(sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP){
               GroupEntity groupEntity = groupManager.findGroup(peerId);
               if(groupEntity == null){
                   groupManager.reqGroupDetailInfo(peerId);
               }
            }
        }else{
            Logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());
            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            //todo check if msgid is null/0
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        /**DB 先更新*/
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
    }


    public List<SessionEntity> getRecentSessionList() {
        List<SessionEntity> recentInfoList = new ArrayList<>(sessionMap.values());
        return recentInfoList;
    }

    private static void sort(List<RecentInfo> data) {
        Collections.sort(data, new Comparator<RecentInfo>() {
            public int compare(RecentInfo o1, RecentInfo o2) {
                Integer a =  o1.getUpdateTime();
                Integer b = o2.getUpdateTime();

                boolean isTopA = o1.isTop();
                boolean isTopB = o2.isTop();

                if(isTopA == isTopB){
                    // 升序
                    //return a.compareTo(b);
                    // 降序
                    return  b.compareTo(a);
                }else{
                    if(isTopA){
                        return -1;
                    }else{
                        return 1;
                    }
                }

            }
        });
    }

    // 获取最近联系人列表，RecentInfo 是sessionEntity unreadEntity user/group 等等实体的封装
    // todo every time it has to sort, kind of inefficient, change it
    public List<RecentInfo> getRecentListInfo(){
        /**整理topList*/
        List<RecentInfo> recentSessionList = new ArrayList<>();
        int loginId = IMLoginManager.instance().getLoginId();

        List<SessionEntity> sessionList = getRecentSessionList();
        Map<Integer,UserEntity> userMap = IMContactManager.instance().getUserMap();
        Map<Integer,UserEntity> friendMap = IMFriendManager.instance().getUserMap();
        Map<String,UnreadEntity> unreadMsgMap = IMUnreadMsgManager.instance().getUnreadMsgMap();
        Map<Integer,GroupEntity> groupEntityMap = IMGroupManager.instance().getGroupMap();
        HashSet<String> topList = ConfigurationSp.instance(ctx,loginId).getSessionTopList();


        for(SessionEntity recentSession:sessionList){
            int sessionType = recentSession.getPeerType();
            int peerId = recentSession.getPeerId();
            String sessionKey = recentSession.getSessionKey();

            UnreadEntity unreadEntity = unreadMsgMap.get(sessionKey);
            if(sessionType == DBConstant.SESSION_TYPE_GROUP){
                GroupEntity groupEntity = groupEntityMap.get(peerId);
                RecentInfo recentInfo = new RecentInfo(recentSession,groupEntity,unreadEntity);
                if(topList !=null && topList.contains(sessionKey)){
                    recentInfo.setTop(true);
                }

                //谁说的这条信息，只有群组需要，例如 【XXX:您好】
                int lastFromId = recentSession.getTalkId();
                UserEntity talkUser = userMap.get(lastFromId);
               // 用户已经不存在了
                if(talkUser !=null){
                    String  oriContent =  recentInfo.getLatestMsgData();
                    String  finalContent = talkUser.getMainName() + ": "+oriContent;
                    recentInfo.setLatestMsgData(finalContent);
                }
                recentSessionList.add(recentInfo);
            }else if(sessionType == DBConstant.SESSION_TYPE_SINGLE){
                UserEntity userEntity = userMap.get(peerId);
                if (userEntity==null) {
                    userEntity = friendMap.get(peerId);
                }
                RecentInfo recentInfo = new RecentInfo(recentSession,userEntity,unreadEntity);
                if(topList !=null && topList.contains(sessionKey)){
                    recentInfo.setTop(true);
                }
                recentSessionList.add(recentInfo);
            }
        }
        sort(recentSessionList);
        return recentSessionList;
    }


    public SessionEntity findSession(String sessionKey){
        if(sessionMap.size()<=0 || TextUtils.isEmpty(sessionKey)){return null;}
        if(sessionMap.containsKey(sessionKey)){
            return sessionMap.get(sessionKey);
        }
        return null;
    }

    public PeerEntity findPeerEntity(String sessionKey){
        if(TextUtils.isEmpty(sessionKey)){
            return null;
        }
        // 拆分
        PeerEntity peerEntity;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        switch (peerType){
            case DBConstant.SESSION_TYPE_SINGLE:{
                peerEntity  = IMContactManager.instance().findContact(peerId);
                if (peerEntity == null) {
                    peerEntity  = IMFriendManager.instance().findContact(peerId);
                }
            }break;
            case DBConstant.SESSION_TYPE_GROUP:{
                peerEntity = IMGroupManager.instance().findGroup(peerId);
            }break;
            default:
                throw new IllegalArgumentException("findPeerEntity#peerType is illegal,cause by " +peerType);
        }
        return peerEntity;
    }

    /**------------------------实体的get set-----------------------------*/
    public boolean isSessionListReady() {
        return sessionListReady;
    }

    public void setSessionListReady(boolean sessionListReady) {
        this.sessionListReady = sessionListReady;
    }
}

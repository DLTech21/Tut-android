package com.dtalk.dd.imservice.manager;

import android.content.Intent;
import android.text.TextUtils;

import com.dtalk.dd.imservice.entity.LocationMessage;
import com.dtalk.dd.imservice.entity.ShortVideoEntity;
import com.dtalk.dd.imservice.entity.ShortVideoMessage;
import com.dtalk.dd.imservice.service.LoadShortVideoService;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.dtalk.dd.DB.entity.PeerEntity;
import com.dtalk.dd.DB.entity.SessionEntity;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.callback.Packetlistener;
import com.dtalk.dd.imservice.entity.AudioMessage;
import com.dtalk.dd.imservice.entity.EmotionMessage;
import com.dtalk.dd.imservice.entity.FileEntity;
import com.dtalk.dd.imservice.entity.FileMessage;
import com.dtalk.dd.imservice.entity.ImageMessage;
import com.dtalk.dd.imservice.entity.TextMessage;
import com.dtalk.dd.imservice.event.MessageEvent;
import com.dtalk.dd.imservice.event.PriorityEvent;
import com.dtalk.dd.imservice.event.RefreshHistoryMsgEvent;
import com.dtalk.dd.imservice.service.LoadFileService;
import com.dtalk.dd.imservice.service.LoadImageService;
import com.dtalk.dd.protobuf.helper.EntityChangeEngine;
import com.dtalk.dd.protobuf.helper.Java2ProtoBuf;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.protobuf.IMMessage;
import com.dtalk.dd.imservice.support.SequenceNumberMaker;
import com.dtalk.dd.utils.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 消息的处理
 */
public class IMMessageManager extends IMManager{
	private static IMMessageManager inst = new IMMessageManager();
	public static IMMessageManager instance() {
			return inst;
	}

    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private IMSessionManager sessionManager = IMSessionManager.instance();
    private DBInterface dbInterface = DBInterface.instance();

    // 消息发送超时时间爱你设定
    // todo eric, after testing ok, make it a longer value
    private final long TIMEOUT_MILLISECONDS = 6 * 1000;
    private final long IMAGE_TIMEOUT_MILLISECONDS = 4 * 60 * 1000;


    private long getTimeoutTolerance(MessageEntity msg) {
        switch (msg.getDisplayType()){
            case DBConstant.SHOW_IMAGE_TYPE:
                return IMAGE_TIMEOUT_MILLISECONDS;
            default:break;
        }
        return TIMEOUT_MILLISECONDS;
    }

    /**
     * 接受到消息，并且向服务端发送确认
     * @param msg
     */
    public void ackReceiveMsg(MessageEntity msg){
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(msg.getSessionType());
        IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.newBuilder()
                .setMsgId(msg.getMsgId())
                .setSessionId(msg.getToId())
                .setUserId(msg.getFromId())
                .setSessionType(sessionType)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_ACK_VALUE;
        imSocketManager.sendRequest(imMsgDataAck,sid,cid);
    }

    @Override
    public void doOnStart() {
    }

    public void onLoginSuccess(){
        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().register(inst);
        }
    }

    @Override
    public void reset() {
        EventBus.getDefault().unregister(inst);
    }

    /**
     * 自身的事件驱动
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }


    /**图片的处理放在这里，因为在发送图片的过程中，很可能messageActivity已经关闭掉*/
    public void onEvent(MessageEvent event){
        MessageEvent.Event  type = event.getEvent();
        switch (type){
            case IMAGE_UPLOAD_FAILD:{
                Logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage)event.getMessageEntity();
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                imageMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(imageMessage);

                /**通知Activity层 失败*/
                event.setEvent(MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD);
                event.setMessageEntity(imageMessage);
                triggerEvent(event);
            }break;

            case IMAGE_UPLOAD_SUCCESS:{
                onImageLoadSuccess(event);
            }break;

            case FILE_UPLOAD_FAILD: {
                Logger.d("flie#onUploadFlieFaild");
                FileMessage fileMessage = (FileMessage)event.getMessageEntity();
                fileMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                fileMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(fileMessage);

                /**通知Activity层 失败*/
                event.setEvent(MessageEvent.Event.HANDLER_FILE_UPLOAD_FAILD);
                event.setMessageEntity(fileMessage);
                triggerEvent(event);
            }break;

            case FILE_UPLOAD_SUCCESS:{
                onFileLoadSuccess(event);
            }break;

            case VIDEO_UPLOAD_FAILD: {
                Logger.d("flie#onUploadShortVideoMessageFaild");
                ShortVideoMessage shortVideoMessage = (ShortVideoMessage)event.getMessageEntity();
                shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                shortVideoMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(shortVideoMessage);

                /**通知Activity层 失败*/
                event.setEvent(MessageEvent.Event.HANDLER_VIDEO_UPLOAD_FAILD);
                event.setMessageEntity(shortVideoMessage);
                triggerEvent(event);
            }break;

            case VIDEO_UPLOAD_SUCCESS:{
                onVideoLoadSuccess(event);
            }break;
        }
    }

    /**
     * 事件的处理会在一个后台线程中执行，对应的函数名是onEventBackgroundThread，
     * 虽然名字是BackgroundThread，事件处理是在后台线程，
     * 但事件处理时间还是不应该太长
     * 因为如果发送事件的线程是后台线程，会直接执行事件，
     * 如果当前线程是UI线程，事件会被加到一个队列中，由一个线程依次处理这些事件，
     * 如果某个事件处理时间太长，会阻塞后面的事件的派发或处理
     * */
    public void onEventBackgroundThread(RefreshHistoryMsgEvent historyMsgEvent){
        doRefreshLocalMsg(historyMsgEvent);
    }


    /**----------------------底层的接口-------------------------------------*/
    /**
     * 发送消息，最终的状态情况
     * MessageManager下面的拆分
     * 应该是自己发的信息，所以msgId为0
     * 这个地方用DB id作为主键
     */
    public void sendMessage(MessageEntity msgEntity) {
        // 发送情况下 msg_id 都是0
        // 服务端是从1开始计数的
        if(!SequenceNumberMaker.getInstance().isFailure(msgEntity.getMsgId())){
            throw new RuntimeException("#sendMessage# msgId is wrong,cause by 0!");
        }

        IMBaseDefine.MsgType msgType = Java2ProtoBuf.getProtoMsgType(msgEntity.getMsgType());
        byte[] sendContent = msgEntity.getSendContent();


        IMMessage.IMMsgData msgData = IMMessage.IMMsgData.newBuilder()
                .setFromUserId(msgEntity.getFromId())
                .setToSessionId(msgEntity.getToId())
                .setMsgId(0)
                .setCreateTime(msgEntity.getCreated())
                .setMsgType(msgType)
                .setMsgData(ByteString.copyFrom(sendContent))  // 这个点要特别注意 todo ByteString.copyFrom
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE;


        final MessageEntity messageEntity  = msgEntity;
        imSocketManager.sendRequest(msgData,sid,cid,new Packetlistener(getTimeoutTolerance(messageEntity)) {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.parseFrom((CodedInputStream)response);
                    Logger.i("chat#onAckSendedMsg");
                    if(imMsgDataAck.getMsgId() <=0){
                        throw  new RuntimeException("Msg ack error,cause by msgId <=0");
                    }
                    messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
                    messageEntity.setMsgId(imMsgDataAck.getMsgId());
                    /**主键ID已经存在，直接替换*/
                    dbInterface.insertOrUpdateMessage(messageEntity);
                    /**更新sessionEntity lastMsgId问题*/
                    sessionManager.updateSession(messageEntity);
                    triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_OK,messageEntity));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFaild() {
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE,messageEntity));
            }
            @Override
            public void onTimeout() {
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_TIME_OUT,messageEntity));
            }
        });
    }

    /**
     * 收到服务端原始信息
     * 1. 解析消息的类型
     * 2. 根据不同的类型,转化成不同的消息
     * 3. 先保存在DB[insertOrreplace]中，session的更新，Unread的更新
     * 4上层通知
     * @param imMsgData
     */
    public void onRecvMessage(IMMessage.IMMsgData imMsgData) {
        Logger.i("chat#onRecvMessage");
        if (imMsgData == null) {
            Logger.e("chat#decodeMessageInfo failed,cause by is null");
            return;
        }

        MessageEntity recvMessage = ProtoBuf2JavaBean.getMessageEntity(imMsgData);
        int loginId = IMLoginManager.instance().getLoginId();
        boolean isSend = recvMessage.isSend(loginId);
        recvMessage.buildSessionKey(isSend);
        recvMessage.setStatus(MessageConstant.MSG_SUCCESS);
        /**对于混合消息，未读消息计数还是1,session已经更新*/

        dbInterface.insertOrUpdateMessage(recvMessage);
        sessionManager.updateSession(recvMessage);

        /**
         *  发送已读确认由上层的activity处理 特殊处理
         *  1. 未读计数、 通知、session页面
         *  2. 当前会话
         * */
        PriorityEvent  notifyEvent = new PriorityEvent();
        notifyEvent.event = PriorityEvent.Event.MSG_RECEIVED_MESSAGE;
        notifyEvent.object = recvMessage;
        triggerEvent(notifyEvent);
    }


    /**-------------------其实可以继续分层切分---------消息发送相关-------------------------------*/
    /**
     * 1. 先保存DB
     * 2. push到adapter中
     * 3. 等待ack,更新页面
     * */
	public void sendText(TextMessage textMessage) {
        Logger.i("chat#text#textMessage");
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        long pkId =  DBInterface.instance().insertOrUpdateMessage(textMessage);
        sessionManager.updateSession(textMessage);
		sendMessage(textMessage);
	}

    public void sendText(EmotionMessage textMessage) {
        Logger.i("chat#text#textMessage");
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        long pkId =  DBInterface.instance().insertOrUpdateMessage(textMessage);
        sessionManager.updateSession(textMessage);
        sendMessage(textMessage);
    }

	public void sendVoice(AudioMessage audioMessage) {
		Logger.i("chat#audio#sendVoice");
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
        long pkId =  DBInterface.instance().insertOrUpdateMessage(audioMessage);
        sessionManager.updateSession(audioMessage);
		sendMessage(audioMessage);
	}

    public void sendFile(FileMessage fileMessage) {
        fileMessage.setStatus(MessageConstant.MSG_SENDING);
        DBInterface.instance().insertOrUpdateMessage(fileMessage);
        int loadStatus = fileMessage.getLoadStatus();
        switch (loadStatus){
            case MessageConstant.IMAGE_LOADED_FAILURE:
            case MessageConstant.IMAGE_UNLOAD:
            case MessageConstant.IMAGE_LOADING:
                fileMessage.setLoadStatus(MessageConstant.IMAGE_LOADING);
                Intent loadImageIntent = new Intent(ctx, LoadFileService.class);
                loadImageIntent.putExtra(SysConstant.UPLOAD_FILE_INTENT_PARAMS, fileMessage);
                ctx.startService(loadImageIntent);
                break;
            case MessageConstant.IMAGE_LOADED_SUCCESS:
                sendMessage(fileMessage);
                break;
            default:
                throw new RuntimeException("sendImages#status不可能出现的状态");
        }
        sessionManager.updateSession(fileMessage);
    }

    public void sendVideo(ShortVideoMessage shortVideoMessage) {
        shortVideoMessage.setStatus(MessageConstant.MSG_SENDING);
        DBInterface.instance().insertOrUpdateMessage(shortVideoMessage);
        int loadStatus = shortVideoMessage.getLoadStatus();
        switch (loadStatus){
            case MessageConstant.IMAGE_LOADED_FAILURE:
            case MessageConstant.IMAGE_UNLOAD:
            case MessageConstant.IMAGE_LOADING:
                shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_LOADING);
                Intent loadImageIntent = new Intent(ctx, LoadShortVideoService.class);
                loadImageIntent.putExtra(SysConstant.UPLOAD_VIDEO_INTENT_PARAMS, shortVideoMessage);
                ctx.startService(loadImageIntent);
                break;
            case MessageConstant.IMAGE_LOADED_SUCCESS:
                sendMessage(shortVideoMessage);
                break;
            default:
                throw new RuntimeException("sendImages#status不可能出现的状态");
        }
        sessionManager.updateSession(shortVideoMessage);
    }

    public void sendSingleImage(ImageMessage msg){
        Logger.d("ImMessageManager#sendImage ");
        ArrayList<ImageMessage> msgList = new ArrayList<>();
        msgList.add(msg);
        sendImages(msgList);
    }

    /**
     * 发送图片消息
     * @param msgList
     */
    public void sendImages(List<ImageMessage> msgList) {
        if(null == msgList || msgList.size() <=0){
            return ;
        }

        int len = msgList.size();
        ArrayList<MessageEntity> needDbList = new ArrayList<>();
        for (ImageMessage msg : msgList) {
            needDbList.add(msg);
        }
        DBInterface.instance().batchInsertOrUpdateMessage(needDbList);

		for (ImageMessage msg : msgList) {
			// image message would wrapped as a text message after uploading
            int loadStatus = msg.getLoadStatus();

            switch (loadStatus){
                case MessageConstant.IMAGE_LOADED_FAILURE:
                case MessageConstant.IMAGE_UNLOAD:
                case MessageConstant.IMAGE_LOADING:
                    msg.setLoadStatus(MessageConstant.IMAGE_LOADING);
                    Intent loadImageIntent = new Intent(ctx, LoadImageService.class);
                    loadImageIntent.putExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS,msg);
                    ctx.startService(loadImageIntent);
                    break;
                case MessageConstant.IMAGE_LOADED_SUCCESS:
                        sendMessage(msg);
                    break;
                default:
                    throw new RuntimeException("sendImages#status不可能出现的状态");
            }
		}
        /**将最后一条更新到Session上面*/
        sessionManager.updateSession(msgList.get(len - 1));
	}

    /**
     * 重新发送 message数据包
     * 1.检测DB状态
     * 2.删除DB状态 [不用删除]
     * 3.调用对应的发送
     * 判断消息的类型、判断是否是重发的状态
     * */
	public void resendMessage(MessageEntity msgInfo) {
        if (msgInfo == null) {
            Logger.d("chat#resendMessage msgInfo is null or already send success!");
            return;
        }
        /**check 历史原因处理*/
        if(!SequenceNumberMaker.getInstance().isFailure(msgInfo.getMsgId())){
            // 之前的状态处理有问题
            msgInfo.setStatus(MessageConstant.MSG_SUCCESS);
            dbInterface.insertOrUpdateMessage(msgInfo);
            triggerEvent(new MessageEvent(MessageEvent.Event.ACK_SEND_MESSAGE_OK,msgInfo));
            return;
        }

        /**重新设定message 的时间,已经从DB中删除*/
        int nowTime = (int) (System.currentTimeMillis()/1000);
        msgInfo.setUpdated(nowTime);
        msgInfo.setCreated(nowTime);

        /**判断信息的类型*/
        int msgType = msgInfo.getDisplayType();
        switch (msgType){
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                  sendText((TextMessage)msgInfo);
                  break;
            case DBConstant.SHOW_IMAGE_TYPE:
                    sendSingleImage((ImageMessage) msgInfo);
                break;
            case DBConstant.SHOW_AUDIO_TYPE:
                   sendVoice((AudioMessage) msgInfo); break;
            case DBConstant.SHOW_LOCATION_TYPE:
                sendMessage(msgInfo);
                break;
            case DBConstant.SHOW_FIEL_TYPE:
                sendFile((FileMessage) msgInfo);
                break;
            case DBConstant.SHOW_GIF_TYPE:
                sendText((EmotionMessage)msgInfo);
                break;
            case DBConstant.SHOW_VIDEO_TYPE:
                sendVideo((ShortVideoMessage) msgInfo);
            break;
            default:
                throw new IllegalArgumentException("#resendMessage#enum type is wrong!!,cause by displayType"+msgType);
        }
	}



    // 拉取历史消息 {from MessageActivity}
    public List<MessageEntity> loadHistoryMsg(int pullTimes,String sessionKey,PeerEntity peerEntity){
        int lastMsgId = 99999999;
        int lastCreateTime = 99999999;
        int count = SysConstant.MSG_CNT_PER_PAGE;
        SessionEntity sessionEntity = IMSessionManager.instance().findSession(sessionKey);
        if (sessionEntity != null) {
            // 以前已经聊过天，删除之后，sessionEntity不存在
            Logger.i("#loadHistoryMsg# sessionEntity is null");
            lastMsgId = sessionEntity.getLatestMsgId();
            // 这个地方设定有问题，先使用最大的时间,session的update设定存在问题
            lastCreateTime = sessionEntity.getUpdated();
        }

        if(lastMsgId <1 || TextUtils.isEmpty(sessionKey)){
            return Collections.emptyList();
        }
        if(count > lastMsgId){
            count = lastMsgId;
        }
        List<MessageEntity> msgList = doLoadHistoryMsg(
                pullTimes, peerEntity.getPeerId(),
                peerEntity.getType(),
                sessionKey, lastMsgId, lastCreateTime, count);

        return msgList;
    }

    // 根据次数有点粗暴
    public List<MessageEntity> loadHistoryMsg(MessageEntity entity,int pullTimes){
        Logger.d("IMMessageActivity#LoadHistoryMsg");
        // 在滑动的过程中请求，msgId请求下一条的
        int reqLastMsgId = entity.getMsgId() - 1;
        int loginId = IMLoginManager.instance().getLoginId();
        int reqLastCreateTime = entity.getCreated();
        String chatKey = entity.getSessionKey();
        int cnt = SysConstant.MSG_CNT_PER_PAGE;
        List<MessageEntity> msgList = doLoadHistoryMsg(pullTimes,
                entity.getPeerId(entity.isSend(loginId)),
                entity.getSessionType(),
                chatKey, reqLastMsgId, reqLastCreateTime, cnt);
        return msgList;
    }

    /**
     * 从DB中请求信息
     * 1. 从最近会话点击进入，拉取消息
     * 2. 在消息页面下拉刷新
     * @param pullTimes
     * @param peerId
     * @param peerType
     * @param sessionKey
     * @param lastMsgId
     * @param lastCreateTime
     * @param count
     * @return
     */
    private List<MessageEntity> doLoadHistoryMsg(int pullTimes,final int peerId,final int peerType, final String sessionKey,int lastMsgId,int lastCreateTime,int count){
        if(lastMsgId <1 || TextUtils.isEmpty(sessionKey)){
            return Collections.emptyList();
        }
        if(count > lastMsgId){
            count = lastMsgId;
        }
        // 降序结果输出desc
        List<MessageEntity> listMsg = dbInterface.getHistoryMsg(sessionKey, lastMsgId, lastCreateTime, count);
        // asyn task refresh
        int resSize = listMsg.size();
        if(resSize==0 || pullTimes == 1 || pullTimes %3==0){
            RefreshHistoryMsgEvent historyMsgEvent = new RefreshHistoryMsgEvent();
            historyMsgEvent.pullTimes = pullTimes;
            historyMsgEvent.count = count;
            historyMsgEvent.lastMsgId = lastMsgId;
            historyMsgEvent.listMsg = listMsg;
            historyMsgEvent.peerId = peerId;
            historyMsgEvent.peerType = peerType;
            historyMsgEvent.sessionKey = sessionKey;
            triggerEvent(historyMsgEvent);
        }
        return listMsg;
    }

    /**
     * asyn task
     * 因为是多端同步，本地信息并不一定完成，拉取时提前异步检测
     * */
    private void doRefreshLocalMsg(RefreshHistoryMsgEvent hisEvent){
        /**check DB数据的一致性*/
        int lastSuccessMsgId = hisEvent.lastMsgId;
        List<MessageEntity> listMsg = hisEvent.listMsg;

        int resSize = listMsg.size();
        if(hisEvent.pullTimes > 1) {
            for (int index = resSize - 1; index >= 0; index--) {
                MessageEntity entity = listMsg.get(index);
                if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                    lastSuccessMsgId = entity.getMsgId();
                    break;
                }
            }
        }else{
            /**是第一次拉取*/
            if(SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId))
            /**正序第一个*/
                for(MessageEntity entity:listMsg){
                    if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                        lastSuccessMsgId = entity.getMsgId();
                        break;
                    }
                }
        }

        final int refreshCnt = hisEvent.count * 3;
        int peerId = hisEvent.peerId;
        int peerType = hisEvent.peerType;
        String sessionKey = hisEvent.sessionKey;
        boolean localFailure =  SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId);
        if(localFailure){
            Logger.e("LoadHistoryMsg# all msg is failure!");
            if(hisEvent.pullTimes ==1){
                reqHistoryMsgNet(peerId,peerType,lastSuccessMsgId,refreshCnt);
            }
        }else {
            /**正常*/
            refreshDBMsg(peerId, peerType, sessionKey, lastSuccessMsgId, refreshCnt);
        }
    }

    /**
     * 历史消息直接从DB中获取。
     * 所以要保证DB数据没有问题
     */
    public void refreshDBMsg(int peerId,int peedType,String chatKey,int lastMsgId,int refreshCnt){
        if(lastMsgId <1){return;}
        int beginMsgId = lastMsgId - refreshCnt;
        if(beginMsgId<1){beginMsgId=1;}

        // 返回的结果是升序
        List<Integer> msgIdList =  dbInterface.refreshHistoryMsgId(chatKey, beginMsgId, lastMsgId);
        if(msgIdList.size() == (lastMsgId-beginMsgId+1)){
            Logger.d("refreshDBMsg#do need refresh Message!,cause sizeOfList is right");
            return;
        }
        // 查找缺失的msgid
        List<Integer> needReqList = new ArrayList<>();
        for(int startIndex=beginMsgId,endIndex=lastMsgId;startIndex<=endIndex;startIndex++){
            if(!msgIdList.contains(startIndex)){
                needReqList.add(startIndex);
            }
        }
        // 请求缺失的消息
        if(needReqList.size()>0){
            reqMsgById(peerId, peedType, needReqList);
        }
    }


    private void reqMsgById(int peerId,int sessionType,List<Integer> msgIds){
        int userId = IMLoginManager.instance().getLoginId();
        IMBaseDefine.SessionType  sType = Java2ProtoBuf.getProtoSessionType(sessionType);
        IMMessage.IMGetMsgByIdReq  imGetMsgByIdReq = IMMessage.IMGetMsgByIdReq.newBuilder()
                .setSessionId(peerId)
                .setUserId(userId)
                .setSessionType(sType)
                .addAllMsgIdList(msgIds)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_REQ_VALUE;
        imSocketManager.sendRequest(imGetMsgByIdReq,sid,cid);
    }

    public void onReqMsgById(IMMessage.IMGetMsgByIdRsp rsp){
        int userId = rsp.getUserId();
        int peerId = rsp.getSessionId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(rsp.getSessionType());
        String sessionKey = EntityChangeEngine.getSessionKey(peerId,sessionType);

        List<IMBaseDefine.MsgInfo>  msgList = rsp.getMsgListList();
        if(msgList.size() <=0){
            Logger.i("onReqMsgById# have no msgList");
            return;
        }
        List<MessageEntity> dbEntity = new ArrayList<>();
        for(IMBaseDefine.MsgInfo msg:msgList){
            MessageEntity entity = ProtoBuf2JavaBean.getMessageEntity(msg);
            if(entity == null){
                continue;
            }

            entity.setSessionKey(sessionKey);
            switch (sessionType){
                case DBConstant.SESSION_TYPE_GROUP:{
                    entity.setToId(peerId);
                }break;
                case DBConstant.SESSION_TYPE_SINGLE:{
                    if(entity.getFromId() == userId){
                        entity.setToId(peerId);
                    }else{
                        entity.setToId(userId);
                    }
                }break;
            }

            dbEntity.add(entity);
        }
        dbInterface.batchInsertOrUpdateMessage(dbEntity);
        /**事件驱动通知*/
        MessageEvent event = new MessageEvent();
        event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAIN);
        triggerEvent(event);
    }


    /**
     * network 请求历史消息
     */
    public  void reqHistoryMsgNet(int peerId,int peerType, int lastMsgId, int cnt){
        int loginId = IMLoginManager.instance().getLoginId();

        IMMessage.IMGetMsgListReq req = IMMessage.IMGetMsgListReq.newBuilder()
                .setUserId(loginId)
                .setSessionType(Java2ProtoBuf.getProtoSessionType(peerType))
                .setSessionId(peerId)
                .setMsgIdBegin(lastMsgId)
                .setMsgCnt(cnt)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(req, sid, cid);
    }

    /**
     * 收到消息的具体信息
     * 保存在DB中
     * 通知上层，请求消息成功
     *
     *对于群而言，如果消息数目返回的数值小于请求的cnt,则表示群的消息能拉取的到头了，更早的消息没有权限拉取。
     *如果msg_cnt 和 msg_id_begin计算得到的最早消息id与实际返回的最早消息id不一致，说明服务器消息有缺失，需要
     *客户端做一个缺失标记，避免下次再次拉取。
     * */
     public void onReqHistoryMsg(IMMessage.IMGetMsgListRsp rsp){
           // 判断loginId 判断sessionId
           int userId = rsp.getUserId();
           int sessionType = ProtoBuf2JavaBean.getJavaSessionType(rsp.getSessionType());
           int peerId = rsp.getSessionId();
           String sessionKey = EntityChangeEngine.getSessionKey(peerId,sessionType);
           int msgBegin = rsp.getMsgIdBegin();

           List<IMBaseDefine.MsgInfo> msgList = rsp.getMsgListList();

           ArrayList<MessageEntity> result = new ArrayList<>();
           for(IMBaseDefine.MsgInfo msgInfo:msgList){
               MessageEntity messageEntity = ProtoBuf2JavaBean.getMessageEntity(msgInfo);
               if(messageEntity == null){
                   continue;
               }
               messageEntity.setSessionKey(sessionKey);
               switch (sessionType){
                   case DBConstant.SESSION_TYPE_GROUP:{
                       messageEntity.setToId(peerId);
                   }break;
                   case DBConstant.SESSION_TYPE_SINGLE:{
                       if(messageEntity.getFromId() == userId){
                           messageEntity.setToId(peerId);
                       }else{
                           messageEntity.setToId(userId);
                       }
                   }break;
               }
               result.add(messageEntity);
            }
         /**事件的通知 check */
         if(result.size()>0) {
             dbInterface.batchInsertOrUpdateMessage(result);
             MessageEvent event = new MessageEvent();
             event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAIN);
             triggerEvent(event);
         }
     }

    /**下载图片的整体迁移出来*/
    private void onImageLoadSuccess(MessageEvent imageEvent){

        ImageMessage imageMessage = (ImageMessage)imageEvent.getMessageEntity();
        Logger.d("pic#onImageUploadFinish");
        String imageUrl = imageMessage.getUrl();
        String realImageURL = "";
        try {
            realImageURL = URLDecoder.decode(imageUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.toString());
        }

        imageMessage.setUrl(realImageURL);
        imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
        imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
        dbInterface.insertOrUpdateMessage(imageMessage);

        /**通知Activity层 成功 ， 事件通知*/
        imageEvent.setEvent(MessageEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS);
        imageEvent.setMessageEntity(imageMessage);
        triggerEvent(imageEvent);

        imageMessage.setContent(MessageConstant.IMAGE_MSG_START
                + realImageURL + MessageConstant.IMAGE_MSG_END);
        sendMessage(imageMessage);
    }

    private void onFileLoadSuccess(MessageEvent imageEvent){

        FileMessage fileMessage = (FileMessage)imageEvent.getMessageEntity();
        Logger.d("file#onFileUploadFinish");
        String imageUrl = fileMessage.getUrl();
        String realImageURL = "";
        try {
            realImageURL = URLDecoder.decode(imageUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.toString());
        }

        fileMessage.setUrl(realImageURL);
        fileMessage.setStatus(MessageConstant.MSG_SUCCESS);
        fileMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
        dbInterface.insertOrUpdateMessage(fileMessage);

        /**通知Activity层 成功 ， 事件通知*/
        imageEvent.setEvent(MessageEvent.Event.HANDLER_FILE_UPLOAD_SUCCESS);
        imageEvent.setMessageEntity(fileMessage);
        triggerEvent(imageEvent);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileMessage.getFileName());
        fileEntity.setExt(fileMessage.getExt());
        fileEntity.setLoadStatus(fileMessage.getLoadStatus());
        fileEntity.setUrl(fileMessage.getUrl());
        fileEntity.setPath(fileMessage.getPath());
        fileMessage.setContent(new Gson().toJson(fileEntity));
        sendMessage(fileMessage);
    }

    private void onVideoLoadSuccess(MessageEvent imageEvent){

        ShortVideoMessage shortVideoMessage = (ShortVideoMessage)imageEvent.getMessageEntity();
        Logger.d("file#onShortVideoMessageUploadFinish");
        String imageUrl = shortVideoMessage.getVideo_cover_url();
        String videoUrl = shortVideoMessage.getVideo_path_url();
        String realImageURL = "";
        try {
            realImageURL = URLDecoder.decode(imageUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.toString());
        }
        String realVideoURL = "";
        try {
            realVideoURL = URLDecoder.decode(videoUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.e(e.toString());
        }

        shortVideoMessage.setVideo_cover_url(realImageURL);
        shortVideoMessage.setVideo_path_url(realVideoURL);
        shortVideoMessage.setStatus(MessageConstant.MSG_SUCCESS);
        shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
        dbInterface.insertOrUpdateMessage(shortVideoMessage);

        /**通知Activity层 成功 ， 事件通知*/
        imageEvent.setEvent(MessageEvent.Event.HANDLER_VIDEO_UPLOAD_SUCCESS);
        imageEvent.setMessageEntity(shortVideoMessage);
        triggerEvent(imageEvent);

        ShortVideoEntity shortVideoEntity = new ShortVideoEntity();
        shortVideoEntity.setLoadStatus(shortVideoMessage.getLoadStatus());
        shortVideoEntity.setVideo_cover_url(shortVideoMessage.getVideo_cover_url());
        shortVideoEntity.setVideo_path_url(shortVideoMessage.getVideo_path_url());
        shortVideoMessage.setContent(new Gson().toJson(shortVideoEntity));
        sendMessage(shortVideoMessage);
    }

//    /**获取session内的最后一条回话*/
//    private void reqSessionLastMsgId(int sessionId,int sessionType,Packetlistener packetlistener){
//        int userId = IMLoginManager.instance().getLoginId();
//        IMMessage.IMGetLatestMsgIdReq latestMsgIdReq = IMMessage.IMGetLatestMsgIdReq.newBuilder()
//                .setUserId(userId)
//                .setSessionId(sessionId)
//                .setSessionType(Java2ProtoBuf.getProtoSessionType(sessionType))
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_LATEST_MSG_ID_REQ_VALUE;
//        imSocketManager.sendRequest(latestMsgIdReq,sid,cid,packetlistener);
//    }
//
//    public void onReqSessionLastMsgId(IMMessage.IMGetLatestMsgIdRsp latestMsgIdRsp){
//        int lastMsgId = latestMsgIdRsp.getLatestMsgId();
//    }
}

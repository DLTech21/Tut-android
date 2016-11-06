package com.dtalk.dd.imservice.manager;

import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.http.friend.FriendListResp;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.event.FriendInfoEvent;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.protobuf.IMBuddy;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.pinyin.PinYin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/4/28.
 */
public class IMFriendManager extends IMManager {

    // 单例
    private static IMFriendManager inst = new IMFriendManager();
    public static IMFriendManager instance() {
        return inst;
    }
    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();

    // 自身状态字段
    private boolean  userDataReady = false;
    private Map<Integer,UserEntity> userMap = new ConcurrentHashMap<>();


    @Override
    public void doOnStart() {
    }

    /**
     * 登陆成功触发
     * auto自动登陆
     * */
    public void onNormalLoginOk(){
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 加载本地DB的状态
     * 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public void onLocalLoginOk(){
        Logger.d("contact#loadAllUserInfo");

        List<UserEntity> userlist = dbInterface.loadAllFriendUsers();
        Logger.d("contact#loadAllUserInfo dbsuccess");

        for(UserEntity userInfo:userlist){
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userMap.put(userInfo.getPeerId(),userInfo);
        }

        triggerEvent(FriendInfoEvent.FRIEND_INFO_OK);
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk(){
//        reqGetAllUsers(0);
        reqGetAllFriends();
    }


    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
    }


    /**
     * @param event
     */
    public void triggerEvent(FriendInfoEvent event) {
        //先更新自身的状态
        switch (event){
            case FRIEND_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    /**-----------------------事件驱动---end---------*/

    private void reqGetAllFriends() {
        FriendClient.getFriendList(new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {

            }

            @Override
            public void onCloseConnection() {

            }

            @Override
            public void onSuccess(Object data) {
                FriendListResp friendListResp = (FriendListResp) data;
                int count = friendListResp.getList().size();
                Logger.e("contact#user cnt:%d"+ count);
                if (count <= 0) {
                    return;
                }
                ArrayList<UserEntity> needDb = new ArrayList<>();
                for (UserInfo userInfo : friendListResp.getList()) {
                    UserEntity userEntity = new UserEntity();
                    int timeNow = (int) (System.currentTimeMillis() / 1000);
                    userEntity.setStatus(Integer.valueOf(userInfo.getStatus()));
                    userEntity.setAvatar(userInfo.getAvatar());
                    userEntity.setCreated(timeNow);
                    userEntity.setDepartmentId(1);
                    userEntity.setEmail("");
                    userEntity.setGender(Integer.valueOf(userInfo.getSex()));
                    userEntity.setMainName(userInfo.getNickname());
                    userEntity.setPhone(userInfo.getUsername());
                    userEntity.setPinyinName(userInfo.getNickname());
                    userEntity.setRealName(userInfo.getRemarkname());
                    userEntity.setUpdated(timeNow);
                    userEntity.setPeerId(Integer.valueOf(userInfo.getUid()));
                    PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                    userMap.put(userEntity.getPeerId(), userEntity);
                    needDb.add(userEntity);
                }
                dbInterface.deleteAllFriendUser();
                dbInterface.batchInsertOrUpdateFriendUser(needDb);
                triggerEvent(FriendInfoEvent.FRIEND_INFO_OK);
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    private void reqGetAllUsers(int lastUpdateTime) {
        Logger.i("contact#reqGetAllUsers");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMAllFriendReq imAllUserReq  = IMBuddy.IMAllFriendReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_FRIEND_REQUEST_VALUE;
        imSocketManager.sendRequest(imAllUserReq, sid, cid);
    }

    /**
     * yingmu change id from string to int
     * @param imAllFriendRsp
     *
     * 1.请求所有用户的信息,总的版本号version
     * 2.匹配总的版本号，返回可能存在变更的
     * 3.选取存在变更的，请求用户详细信息
     * 4.更新DB，保存globalVersion 以及用户的信息
     */
    public void onRepAllUsers(IMBuddy.IMAllFriendRsp imAllFriendRsp) {
        Logger.i("contact#onRepAllUsers");
        int userId = imAllFriendRsp.getUserId();
        int lastTime = imAllFriendRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count =  imAllFriendRsp.getUserListCount();
        Logger.e("contact#user cnt:%d"+ count);
        if(count <=0){
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if(userId != loginId){
            Logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return ;
        }

        List<IMBaseDefine.UserInfo> changeList =  imAllFriendRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:changeList){
            UserEntity entity =  ProtoBuf2JavaBean.getUserEntity(userInfo);
            userMap.put(entity.getPeerId(),entity);
            needDb.add(entity);
        }
        dbInterface.deleteAllFriendUser();
        dbInterface.batchInsertOrUpdateFriendUser(needDb);
        triggerEvent(FriendInfoEvent.FRIEND_INFO_OK);
    }

    public UserEntity findContact(int buddyId){
        if(buddyId > 0 && userMap.containsKey(buddyId)){
            return userMap.get(buddyId);
        }
        return null;
    }

    public  List<UserEntity> getContactSortedList() {
        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Collections.sort(contactList, new Comparator<UserEntity>(){
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if(entity1.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity1.getMainName(),entity1.getPinyinElement());
                    }
                    if(entity2.getPinyinElement().pinyin==null)
                    {
                        PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }

    // 确实要将对比的抽离出来 Collections
    public  List<UserEntity> getSearchContactList(String key){
        List<UserEntity> searchList = new ArrayList<>();
        for(Map.Entry<Integer,UserEntity> entry:userMap.entrySet()){
            UserEntity user = entry.getValue();
            if (IMUIHelper.handleContactSearch(key, user)) {
                searchList.add(user);
            }
        }
        return searchList;
    }

    /**-----------------------实体 get set 定义-----------------------------------*/

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }


    public boolean isUserDataReady() {
        return userDataReady;
    }

}

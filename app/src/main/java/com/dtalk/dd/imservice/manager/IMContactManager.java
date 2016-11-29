package com.dtalk.dd.imservice.manager;

import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.DepartmentEntity;
import com.dtalk.dd.DB.entity.GroupEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.http.friend.OtherUserInfo;
import com.dtalk.dd.http.friend.OtherUserInfoNoRemark;
import com.dtalk.dd.imservice.event.UserInfoEvent;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.protobuf.IMBaseDefine;
import com.dtalk.dd.protobuf.IMBuddy;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.dd.utils.pinyin.PinYin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * 负责用户信息的请求
 * 为回话页面以及联系人页面提供服务
 *
 * 联系人信息管理
 * 普通用户的version  有总版本
 * 群组没有总version的概念， 每个群有version
 * 具体请参见 服务端具体的pd协议
 */
public class IMContactManager extends IMManager {

    // 单例
    private static IMContactManager inst = new IMContactManager();
    public static IMContactManager instance() {
            return inst;
    }
    private IMSocketManager imSocketManager = IMSocketManager.instance();
    private DBInterface dbInterface = DBInterface.instance();

    // 自身状态字段
    private boolean  userDataReady = false;
    private Map<Integer,UserEntity> userMap = new ConcurrentHashMap<>();
    private Map<Integer,DepartmentEntity> departmentMap = new ConcurrentHashMap<>();


    @Override
    public void doOnStart() {
    }

    /**
     * 登陆成功触发
     * auto自动登陆
     * */
    public void onNormalLoginOk(){
        onLocalLoginOk();
//        onLocalNetOk();
    }

    /**
     * 加载本地DB的状态
     * 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public void onLocalLoginOk(){
//        Logger.d("contact#loadAllUserInfo");
//
//        List<DepartmentEntity> deptlist = dbInterface.loadAllDept();
//        Logger.d("contact#loadAllDept dbsuccess");
//
        List<UserEntity> userlist = dbInterface.loadAllUsers();
//        Logger.d("contact#loadAllUserInfo dbsuccess");
//
        for(UserEntity userInfo:userlist){
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userMap.put(userInfo.getPeerId(),userInfo);
        }
//
//        for(DepartmentEntity deptInfo:deptlist){
//            PinYin.getPinYin(deptInfo.getDepartName(), deptInfo.getPinyinElement());
//            departmentMap.put(deptInfo.getDepartId(),deptInfo);
//        }
        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk(){
        // 部门信息
//        int deptUpdateTime = dbInterface.getDeptLastTime();
//        reqGetDepartment(deptUpdateTime);

        // 用户信息
//        int updateTime = dbInterface.getUserInfoLastTime();
//        Logger.e("contact#loadAllUserInfo req-updateTime:%d", updateTime);
//        reqGetAllUsers(0);
    }


    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
    }


    /**
     * @param event
     */
    public void triggerEvent(UserInfoEvent event) {
        //先更新自身的状态
        switch (event){
            case USER_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    /**-----------------------事件驱动---end---------*/

//    private void reqGetAllUsers(int lastUpdateTime) {
//		Logger.i("contact#reqGetAllUsers");
//		int userId = IMLoginManager.instance().getLoginId();
//
//        IMBuddy.IMAllUserReq imAllUserReq  = IMBuddy.IMAllUserReq.newBuilder()
//                .setUserId(userId)
//                .setLatestUpdateTime(lastUpdateTime).build();
//        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
//        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_REQUEST_VALUE;
//        imSocketManager.sendRequest(imAllUserReq, sid, cid);
//	}

    /**
     * yingmu change id from string to int
     * @param imAllUserRsp
     *
     * 1.请求所有用户的信息,总的版本号version
     * 2.匹配总的版本号，返回可能存在变更的
     * 3.选取存在变更的，请求用户详细信息
     * 4.更新DB，保存globalVersion 以及用户的信息
     */
	public void onRepAllUsers(IMBuddy.IMAllUserRsp imAllUserRsp) {
        Logger.e("contact#onRepAllUsers" + Integer.MAX_VALUE);
		Logger.i("contact#onRepAllUsers");
        int userId = imAllUserRsp.getUserId();
        int lastTime = imAllUserRsp.getLatestUpdateTime();
        // lastTime 需要保存嘛? 不保存了

        int count =  imAllUserRsp.getUserListCount();
        if(count <=0){
            return;
        }

		int loginId = IMLoginManager.instance().getLoginId();
        if(userId != loginId){
            Logger.e("[fatal error] userId not equels loginId ,cause by onRepAllUsers");
            return ;
        }

        List<IMBaseDefine.UserInfo> changeList =  imAllUserRsp.getUserListList();
        ArrayList<UserEntity> needDb = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:changeList){
            UserEntity entity =  ProtoBuf2JavaBean.getUserEntity(userInfo);
            userMap.put(entity.getPeerId(),entity);
            needDb.add(entity);
        }
        dbInterface.deleteAllUser();
        dbInterface.batchInsertOrUpdateUser(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
	}

    public UserEntity findContact(int buddyId){
        if(buddyId > 0 && userMap.containsKey(buddyId)){
            return userMap.get(buddyId);
        }
        return null;
    }

    public void reqGetDetailUser(String currentUserId) {
        FriendClient.getFriendInfo(currentUserId + "", new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {
            }

            @Override
            public void onCloseConnection() {
            }

            @Override
            public void onSuccess(Object data) {
                if(data instanceof OtherUserInfo) {
                    OtherUserInfo otherUserInfo = (OtherUserInfo) data;
                    if (otherUserInfo.getStatus() == 1) {
                        UserEntity userEntity = new UserEntity();
                        int timeNow = (int) (System.currentTimeMillis() / 1000);
                        userEntity.setStatus(Integer.valueOf(otherUserInfo.getStatus()));
                        userEntity.setAvatar(otherUserInfo.getAvatar());
                        userEntity.setCreated(timeNow);
                        userEntity.setDepartmentId(1);
                        userEntity.setEmail("");
                        userEntity.setGender(Integer.valueOf(otherUserInfo.getSex()));
                        userEntity.setMainName(otherUserInfo.getNickname());
                        userEntity.setPhone(otherUserInfo.getUsername());
                        userEntity.setPinyinName(otherUserInfo.getNickname());
                        userEntity.setUpdated(timeNow);
                        userEntity.setPeerId(Integer.valueOf(otherUserInfo.getUid()));
                        userEntity.setIsFriend(Integer.valueOf(otherUserInfo.getFriend()));
                        userEntity.setArea(otherUserInfo.getArea());
                        userEntity.setMomentcover(otherUserInfo.getMoment_cover());
                        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                        if (StringUtils.notEmpty(otherUserInfo.getRemarks())) {
                            userEntity.setRealName(otherUserInfo.getRemarks().getRemarkname());
                        }
                        boolean needEvent = false;
                        ArrayList<UserEntity> dbNeed = new ArrayList<>();
                        int userId = userEntity.getPeerId();
                        if (userMap.containsKey(userId) && userMap.get(userId).equals(userEntity)) {
                            //没有必要通知更新
                        } else {
                            needEvent = true;
                            userMap.put(userEntity.getPeerId(), userEntity);
                            dbNeed.add(userEntity);
//                        if (otherUserInfo.getUid() == loginId) {
//                            IMLoginManager.instance().setLoginInfo(userEntity);
//                        }
                        }
                        
                        // 负责userMap
                        dbInterface.batchInsertOrUpdateUser(dbNeed);

                        // 判断有没有必要进行推送
                        if (needEvent) {
                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
                        }
                    }
                }
                else if(data instanceof OtherUserInfoNoRemark){
                    OtherUserInfoNoRemark otherUserInfoNoRemark = (OtherUserInfoNoRemark) data;
                    if (otherUserInfoNoRemark.getStatus() == 1) {
                        UserEntity userEntity = new UserEntity();
                        int timeNow = (int) (System.currentTimeMillis() / 1000);
                        userEntity.setStatus(Integer.valueOf(otherUserInfoNoRemark.getStatus()));
                        userEntity.setAvatar(otherUserInfoNoRemark.getAvatar());
                        userEntity.setCreated(timeNow);
                        userEntity.setDepartmentId(1);
                        userEntity.setEmail("");
                        userEntity.setGender(Integer.valueOf(otherUserInfoNoRemark.getSex()));
                        userEntity.setMainName(otherUserInfoNoRemark.getNickname());
                        userEntity.setPhone(otherUserInfoNoRemark.getUsername());
                        userEntity.setPinyinName(otherUserInfoNoRemark.getNickname());
                        userEntity.setUpdated(timeNow);
                        userEntity.setPeerId(Integer.valueOf(otherUserInfoNoRemark.getUid()));
                        userEntity.setIsFriend(Integer.valueOf(otherUserInfoNoRemark.getFriend()));
                        userEntity.setArea(otherUserInfoNoRemark.getArea());
                        userEntity.setMomentcover(otherUserInfoNoRemark.getMoment_cover());
                        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
                        userEntity.setRealName(otherUserInfoNoRemark.getNickname());
                        if (userEntity.getIsFriend() == 1) {
                            dbInterface.insertOrUpdateFriendUser(userEntity);
                        }
                        boolean needEvent = false;
                        ArrayList<UserEntity> dbNeed = new ArrayList<>();
                        int userId = userEntity.getPeerId();
                        if (userMap.containsKey(userId) && userMap.get(userId).equals(userEntity)) {
                            //没有必要通知更新
                        } else {
                            needEvent = true;
                            userMap.put(userEntity.getPeerId(), userEntity);
                            dbNeed.add(userEntity);
                        }
                        // 负责userMap
                        dbInterface.batchInsertOrUpdateUser(dbNeed);

                        // 判断有没有必要进行推送
                        if (needEvent) {
                            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    /**
     * 请求用户详细信息
     * @param userIds
     */
    public void reqGetDetaillUsers(ArrayList<Integer> userIds){
        Logger.i("contact#contact#reqGetDetaillUsers");
        if(null == userIds || userIds.size() <=0){
            Logger.i("contact#contact#reqGetDetaillUsers return,cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.instance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq  =  IMBuddy.IMUsersInfoReq.newBuilder()
                .setUserId(loginId)
                .addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }

    /**
     * 获取用户详细的信息
     * @param imUsersInfoRsp
     */
    public void  onRepDetailUsers(IMBuddy.IMUsersInfoRsp imUsersInfoRsp){
        int loginId = imUsersInfoRsp.getUserId();
        boolean needEvent = false;
        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp.getUserInfoListList();

        ArrayList<UserEntity>  dbNeed = new ArrayList<>();
        for(IMBaseDefine.UserInfo userInfo:userInfoList) {
            UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            int userId = userEntity.getPeerId();
            if (userMap.containsKey(userId) && userMap.get(userId).equals(userEntity)) {
                //没有必要通知更新
            } else {
                needEvent = true;
                userMap.put(userEntity.getPeerId(), userEntity);
                dbNeed.add(userEntity);
                if (userInfo.getUserId() == loginId) {
                    IMLoginManager.instance().setLoginInfo(userEntity);
                }
            }
        }
        // 负责userMap
        dbInterface.batchInsertOrUpdateUser(dbNeed);

        // 判断有没有必要进行推送
        if(needEvent){
            triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
        }
    }


    public DepartmentEntity findDepartment(int deptId){
         DepartmentEntity entity = departmentMap.get(deptId);
         return entity;
    }


    public  List<DepartmentEntity>  getDepartmentSortedList(){
        // todo eric efficiency
        List<DepartmentEntity> departmentList = new ArrayList<>(departmentMap.values());
        Collections.sort(departmentList, new Comparator<DepartmentEntity>() {
            @Override
            public int compare(DepartmentEntity entity1, DepartmentEntity entity2) {

                if (entity1.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity1.getDepartName(), entity1.getPinyinElement());
                }
                if (entity2.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity2.getDepartName(), entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);

            }
        });
        return departmentList;
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


    // 通讯录中的部门显示 需要根据优先级
    public List<UserEntity> getDepartmentTabSortedList() {
        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        Collections.sort(contactList, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                DepartmentEntity dept1 = departmentMap.get(entity1.getDepartmentId());
                DepartmentEntity dept2 = departmentMap.get(entity2.getDepartmentId());

                if (entity1.getDepartmentId() == entity2.getDepartmentId()) {
                    // start compare
                    if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                        return -1;
                    } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                        // todo eric guess: latter is > 0
                        return 1;
                    } else {
                        if(entity1.getPinyinElement().pinyin==null)
                        {
                            PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                        }
                        if(entity2.getPinyinElement().pinyin==null)
                        {
                            PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                        }
                        return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                    }
                    // end compare
                } else {
                    if(dept1!=null && dept2!=null && dept1.getDepartName()!=null && dept2.getDepartName()!=null)
                    {
                        return dept1.getDepartName().compareToIgnoreCase(dept2.getDepartName());
                    }
                    else
                        return 1;

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

    public List<DepartmentEntity> getSearchDepartList(String key) {
        List<DepartmentEntity> searchList = new ArrayList<>();
        for(Map.Entry<Integer,DepartmentEntity> entry:departmentMap.entrySet()){
            DepartmentEntity dept = entry.getValue();
            if (IMUIHelper.handleDepartmentSearch(key, dept)) {
                searchList.add(dept);
            }
        }
        return searchList;
    }

    /**------------------------部门相关的协议 start------------------------------*/

    // 更新的方式与userInfo一直，根据时间点
    public void reqGetDepartment(int lastUpdateTime){
        Logger.i("contact#reqGetDepartment");
        int userId = IMLoginManager.instance().getLoginId();

        IMBuddy.IMDepartmentReq imDepartmentReq  = IMBuddy.IMDepartmentReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(imDepartmentReq,sid,cid);
    }

    public void onRepDepartment(IMBuddy.IMDepartmentRsp imDepartmentRsp){
        Logger.i("contact#onRepDepartment");
        int userId = imDepartmentRsp.getUserId();
        int lastTime = imDepartmentRsp.getLatestUpdateTime();

        int count =  imDepartmentRsp.getDeptListCount();
        // 如果用户找不到depart 那么部门显示未知
        if(count <=0){
            return;
        }

        int loginId = IMLoginManager.instance().getLoginId();
        if(userId != loginId){
            Logger.e("[fatal error] userId not equels loginId ,cause by onRepDepartment");
            return ;
        }
        List<IMBaseDefine.DepartInfo> changeList =  imDepartmentRsp.getDeptListList();
        ArrayList<DepartmentEntity> needDb = new ArrayList<>();

        for(IMBaseDefine.DepartInfo  departInfo:changeList){
            DepartmentEntity entity =  ProtoBuf2JavaBean.getDepartEntity(departInfo);
            departmentMap.put(entity.getDepartId(),entity);
            needDb.add(entity);
        }
        // 部门信息更新
        dbInterface.batchInsertOrUpdateDepart(needDb);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    /**------------------------部门相关的协议 end------------------------------*/

    /**-----------------------实体 get set 定义-----------------------------------*/

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public Map<Integer, DepartmentEntity> getDepartmentMap() {
        return departmentMap;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }

}

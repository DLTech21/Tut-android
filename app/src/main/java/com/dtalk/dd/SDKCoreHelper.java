package com.dtalk.dd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDeskManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECNotifyOptions;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;

import java.util.List;

/**
 * Created by Jorstin on 2015/3/17.
 */
public class SDKCoreHelper implements ECDevice.InitListener , ECDevice.OnECDeviceConnectListener,ECDevice.OnLogoutListener, OnChatReceiveListener {

    public static final String TAG = "SDKCoreHelper";
    public static final String ACTION_LOGOUT = "com.yuntongxun.ECDemo_logout";
    public static final String ACTION_SDK_CONNECT = "com.yuntongxun.Intent_Action_SDK_CONNECT";
    public static final String ACTION_KICK_OFF = "com.yuntongxun.Intent_ACTION_KICK_OFF";
    private static SDKCoreHelper sInstance;
    private Context mContext;
    private ECDevice.ECConnectState mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
    private ECInitParams mInitParams;
    private ECInitParams.LoginMode mMode = ECInitParams.LoginMode.FORCE_LOGIN;
    /**初始化错误*/
    public static final int ERROR_CODE_INIT = -3;
    
    public static final int WHAT_SHOW_PROGRESS = 0x101A;
	public static final int WHAT_CLOSE_PROGRESS = 0x101B;
    private boolean mKickOff = false;
    private ECNotifyOptions mOptions;
    public static SoftUpdate mSoftUpdate;
    
    private Handler handler;
    private SDKCoreHelper() {
    	initNotifyOptions();
    }

    public static SDKCoreHelper getInstance() {
        if (sInstance == null) {
            sInstance = new SDKCoreHelper();
        }
        return sInstance;
    }
    
    public synchronized void setHandler(final Handler handler) {
		this.handler = handler;
	}

    public static boolean isKickOff() {
        return getInstance().mKickOff;
    }

    public static void init(Context ctx) {
        init(ctx, ECInitParams.LoginMode.AUTO);
    }

    public static void init(Context ctx , ECInitParams.LoginMode mode) {
        getInstance().mKickOff = false;
        ctx = IMApplication.getInstance().getApplicationContext();
        getInstance().mMode = mode;
        getInstance().mContext = ctx;
        // 判断SDK是否已经初始化，没有初始化则先初始化SDK
        if(!ECDevice.isInitialized()) {
            getInstance().mConnect = ECDevice.ECConnectState.CONNECTING;
            // ECSDK.setNotifyOptions(getInstance().mOptions);
            ECDevice.initial(ctx, getInstance());

            postConnectNotify();
            return ;
        }
        // 已经初始化成功，直接进行注册
        getInstance().onInitialized();
    }

    public static void setSoftUpdate(String version , String desc , boolean mode) {
        mSoftUpdate = new SoftUpdate(version ,desc ,mode);
    }
    
    private void initNotifyOptions() {
        if(mOptions == null) {
            mOptions = new ECNotifyOptions();
        }
        // 设置新消息是否提醒
        mOptions.setNewMsgNotify(true);
        // 设置状态栏通知图标
        mOptions.setIcon(R.drawable.ic_launcher);
        // 设置是否启用勿扰模式（不会声音/震动提醒）
        mOptions.setSilenceEnable(false);
        // 设置勿扰模式时间段（开始小时/开始分钟-结束小时/结束分钟）
        // 小时采用24小时制
        // 如果设置勿扰模式不启用，则设置勿扰时间段无效
        // 当前设置晚上11点到第二天早上8点之间不提醒
        mOptions.setSilenceTime(23, 0, 8, 0);
        // 设置是否震动提醒(如果处于免打扰模式则设置无效，没有震动)
        mOptions.enableShake(true);
        // 设置是否声音提醒(如果处于免打扰模式则设置无效，没有声音)
        mOptions.enableSound(true);
    }

    @Override
    public void onInitialized() {

        // 设置消息提醒
        ECDevice.setNotifyOptions(mOptions);
        // 设置接收VoIP来电事件通知Intent
        // 呼入界面activity、开发者需修改该类
//        Intent intent = new Intent(getInstance().mContext, VoIPCallActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity( getInstance().mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        ECDevice.setPendingIntent(pendingIntent);

        // 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
        // 通知应用SDK注册状态
        // 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
        ECDevice.setOnChatReceiveListener(this);
        ECDevice.setOnDeviceConnectListener(this);


        if (mInitParams == null){
            mInitParams = ECInitParams.createParams();
        }
        mInitParams.reset();
        // 如：VoIP账号/手机号码/..
        mInitParams.setUserid(IMLoginManager.instance().getLoginId()+"");
        // appkey
        mInitParams.setAppKey("aaf98f8947d7c8680147f110fd4c20f9");
        // mInitParams.setAppKey(/*clientUser.getAppKey()*/"ff8080813d823ee6013d856001000029");
        // appToken
        mInitParams.setToken("9d9abe305f7f4882ad010972f03367d1");
        // mInitParams.setToken(/*clientUser.getAppToken()*/"d459711cd14b443487c03b8cc072966e");
        // ECInitParams.LoginMode.FORCE_LOGIN
        mInitParams.setMode(getInstance().mMode);


        // 设置登陆验证模式（是否验证密码/如VoIP方式登陆）
        mInitParams.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);

        if(!mInitParams.validate()) {
            Intent failIntent = new Intent(ACTION_SDK_CONNECT);
            failIntent.putExtra("error", -1);
            mContext.sendBroadcast(failIntent);
            return ;
        }

        ECDevice.login(mInitParams);
        
    }

    @Override
    public void onConnect() {
        // Deprecated
    }

    @Override
    public void onDisconnect(ECError error) {
        // SDK与云通讯平台断开连接
        // Deprecated
    }

    @Override
    public void onConnectState(ECDevice.ECConnectState state, ECError error) {
    }

    /**
     * 当前SDK注册状态
     * @return
     */
    public static ECDevice.ECConnectState getConnectState() {
        return getInstance().mConnect;
    }

    @Override
    public void onLogout() {
        getInstance().mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
        if(mInitParams != null && mInitParams.getInitParams() != null) {
            mInitParams.getInitParams().clear();
        }
        mInitParams = null;
        mContext.sendBroadcast(new Intent(ACTION_LOGOUT));
    }

    @Override
    public void onError(Exception exception) {
        Intent intent = new Intent(ACTION_SDK_CONNECT);
        intent.putExtra("error", ERROR_CODE_INIT);
        mContext.sendBroadcast(intent);
        ECDevice.unInitial();
    }

    /**
     * 状态通知
     */
    private static void postConnectNotify() {
//        if(getInstance().mContext instanceof LauncherActivity) {
//            ((LauncherActivity) getInstance().mContext).onNetWorkNotify(getConnectState());
//        }
    }

    public static void logout(boolean isNotice) {
    	ECDevice.NotifyMode notifyMode = (isNotice) ? ECDevice.NotifyMode.IN_NOTIFY : ECDevice.NotifyMode.NOT_NOTIFY;
        ECDevice.logout(notifyMode, getInstance());
        
        
        release();
    }

    public static void release() {
        getInstance().mKickOff = false;
    }

    /**
     * IM聊天功能接口
     * @return
     */
    public static ECChatManager getECChatManager() {
        ECChatManager ecChatManager = ECDevice.getECChatManager();
        return ecChatManager;
    }

    /**
     * 群组聊天接口
     * @return
     */
    public static ECGroupManager getECGroupManager() {
        return ECDevice.getECGroupManager();
    }

    public static ECDeskManager getECDeskManager() {
        return ECDevice.getECDeskManager();
    }

    /**
     * VoIP呼叫接口
     * @return
     */
    public static ECVoIPCallManager getVoIPCallManager() {
        return ECDevice.getECVoIPCallManager();
    }

    public static ECVoIPSetupManager getVoIPSetManager() {
        return ECDevice.getECVoIPSetupManager();
    }

    @Override
    public void OnReceivedMessage(ECMessage ecMessage) {

    }

    @Override
    public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

    }

    @Override
    public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage ecGroupNoticeMessage) {

    }

    @Override
    public void onOfflineMessageCount(int i) {

    }

    @Override
    public int onGetOfflineMessage() {
        return 0;
    }

    @Override
    public void onReceiveOfflineMessage(List<ECMessage> list) {

    }

    @Override
    public void onReceiveOfflineMessageCompletion() {

    }

    @Override
    public void onServicePersonVersion(int i) {

    }

    @Override
    public void onReceiveDeskMessage(ECMessage ecMessage) {

    }

    @Override
    public void onSoftVersion(String s, int i) {

    }


    public static class SoftUpdate  {
        public String version;
        public String desc;
        public boolean force;

        public SoftUpdate(String version ,String desc, boolean force) {
            this.version = version;
            this.force = force;
            this.desc = desc;
        }
    }
    
    /**
     * 
     * @return返回底层so库 是否支持voip及会议功能 
     * true 表示支持 false表示不支持
     * 请在sdk初始化完成之后调用
     */
    public boolean isSupportMedia(){
    	
    	return ECDevice.isSupportMedia();
    }
    
    public static boolean hasFullSize(String inStr) {
		if (inStr.getBytes().length != inStr.length()) {
			return true;
		}
		return false;
	}
    
    public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {

		Bundle b = new Bundle();
		b.putParcelable("VideoConferenceMsg", msg);

//		sendTarget(VideoconferenceBaseActivity.KEY_VIDEO_RECEIVE_MESSAGE, b);

	}
    long t = 0;
    
    public void sendTarget(int what, Object obj) {
		t = System.currentTimeMillis();
		while (handler == null && (System.currentTimeMillis() - t < 3500)) {

			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
			}
		}

		if (handler == null) {
			return;
		}

		Message msg = Message.obtain(handler);
		msg.what = what;
		msg.obj = obj;
		msg.sendToTarget();
	}
    
    /**
     * 判断服务是否自动重启
     * @return 是否自动重启
     */
    public static boolean isUIShowing() {
        return ECDevice.isInitialized();
    }
    
}

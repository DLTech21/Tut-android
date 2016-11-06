package com.dtalk.dd.voip;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageButton;

import com.dtalk.dd.R;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public abstract class ECVoIPBaseActivity extends TTBaseActivity
        implements VoIPCallHelper.OnCallEventNotifyListener , ECCallControlUILayout.OnCallControlDelegate  {

    private static final String TAG = "ECSDK_Demo.ECVoIPBaseActivity";
    private KeyguardManager mKeyguardManager = null;
    private KeyguardManager.KeyguardLock mKeyguardLock = null;
    private PowerManager.WakeLock mWakeLock;
    /**昵称*/
    public static final String EXTRA_CALL_NAME = "com.mogujie.tt.VoIP_CALL_NAME";
    /**通话号码*/
    public static final String EXTRA_CALL_NUMBER = "com.mogujie.tt.VoIP_CALL_NUMBER";
    /**呼入方或者呼出方*/
    public static final String EXTRA_OUTGOING_CALL = "com.mogujie.tt.VoIP_OUTGOING_CALL";
    /**VoIP呼叫*/
    public static final String ACTION_VOICE_CALL = "com.mogujie.tt.intent.ACTION_VOICE_CALL";
    /**Video呼叫*/
    public static final String ACTION_VIDEO_CALL = "com.mogujie.tt.intent.ACTION_VIDEO_CALL";
    public static final String ACTION_CALLBACK_CALL = "com.mogujie.tt.intent.ACTION_VIDEO_CALLBACK";

    /**通话昵称*/
    protected String mCallName;
    /**通话号码*/
    protected String mCallNumber;
    protected String mPhoneNumber;
    /**是否来电*/
    protected boolean mIncomingCall = false;
    /**呼叫唯一标识号*/
    protected String mCallId;
    /**VoIP呼叫类型（音视频）*/
    protected ECVoIPCallManager.CallType mCallType;
    /**透传号码参数*/
    private static final String KEY_TEL = "tel";
    /**透传名称参数*/
    private static final String KEY_NAME = "nickname";
    protected ECCallHeadUILayout mCallHeaderView;
    protected ECCallControlUILayout mCallControlUIView;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(init()) {
            return ;
        }

        if(mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }

        initProwerManager();
    }
    
    private Intent sIntent;
    
    

    private boolean init() {
    	if(getIntent()==null){
    		return true;
    	}
    	sIntent=getIntent();
        mIncomingCall = !(getIntent().getBooleanExtra(EXTRA_OUTGOING_CALL, false));
        mCallType = (ECVoIPCallManager.CallType) getIntent().getSerializableExtra(ECDevice.CALLTYPE);

        if(mIncomingCall) {
            // 透传信息
            String[] infos = getIntent().getExtras().getStringArray(ECDevice.REMOTE);
            if (infos != null && infos.length > 0) {
                for (String str : infos) {
                    if (str.startsWith(KEY_TEL)) {
                        mPhoneNumber = getLastwords(str, "=");
                    } else if (str.startsWith(KEY_NAME)) {
                        mCallName = getLastwords(str, "=");
                    }
                }
            }
        }

        if(!VoIPCallHelper.mHandlerVideoCall && mCallType == ECVoIPCallManager.CallType.VIDEO) {
            VoIPCallHelper.mHandlerVideoCall = true;
//            Intent mVideoIntent = new Intent(this , VideoActivity.class);
//            mVideoIntent.putExtras(getIntent().getExtras());
//            mVideoIntent.putExtra(VoIPCallActivity.EXTRA_OUTGOING_CALL , false);
//            startActivity(mVideoIntent);
//            super.finish();
            return true;
        }
        return false;

    }


    public static String getLastwords(String srcText, String p) {
        try {
            String[] array = TextUtils.split(srcText, p);
            int index = (array.length - 1 < 0) ? 0 : array.length - 1;
            return array[index];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 收到的VoIP通话事件通知是否与当前通话界面相符
     * @return 是否正在进行的VoIP通话
     */
    protected boolean isEqualsCall(String callId) {
        return (!TextUtils.isEmpty(callId) && callId.equals(mCallId));
    }

    /**
     * 是否需要做界面更新
     * @param callId
     * @return
     */
    protected boolean needNotify(String callId) {
        return !(isFinishing() || !isEqualsCall(callId));
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterIncallMode();
        VoIPCallHelper.setOnCallEventNotifyListener(this);
//        ECNotificationManager.cancelCCPNotification(ECNotificationManager.CCP_NOTIFICATOIN_ID_CALLING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
    }

    /**
     * 唤醒屏幕资源
     */
    protected void enterIncallMode() {
        if (!(mWakeLock.isHeld())) {
            // wake up screen
            // BUG java.lang.RuntimeException: WakeLock under-locked
            mWakeLock.setReferenceCounted(false);
            mWakeLock.acquire();
        }
        mKeyguardLock = this.mKeyguardManager.newKeyguardLock("");
        mKeyguardLock.disableKeyguard();
    }

    /**
     * 初始化资源
     */
    protected void initProwerManager() {
        mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP , "CALL_ACTIVITY#" + super.getClass().getName());
        mKeyguardManager = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE));
    }

    /**
     * 释放资源
     */
    protected void releaseWakeLock() {
        try {
            if (this.mWakeLock.isHeld()) {
                if (this.mKeyguardLock != null) {
                    this.mKeyguardLock.reenableKeyguard();
                    this.mKeyguardLock = null;
                }
                this.mWakeLock.release();
            }
            return;
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(VoIPCallHelper.isHoldingCall()) {
//            ECNotificationManager.showCallingNotification(mCallType);
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

    @Override
    public void onViewAccept(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {///
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.acceptCall(mCallId);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.INCALL);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_accepting);
        
    }

    @Override
    public void onViewRelease(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.releaseCall(mCallId);
    }

    @Override
    public void onViewReject(ECCallControlUILayout controlPanelView, ImageButton view) {
        if(controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
        VoIPCallHelper.rejectCall(mCallId);
    }

    @Override
    public void onVideoRatioChanged(VideoRatio videoRatio) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ECHandlerHelper.removeCallbacksRunnOnUI(OnCallFinish);
        setIntent(intent);
//        setIntent(sIntent);
        if(init()) {
            return ;
        }

        if(mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }
    }

    @Override
    public void finish() {
            ECHandlerHelper.postDelayedRunnOnUI(OnCallFinish, 3000);
    }
    public void hfFinish() {
    	ECHandlerHelper.postDelayedRunnOnUI(OnCallFinish, 0);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// do nothing.
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


    /**
     * 延时关闭界面
     */
    final Runnable OnCallFinish = new Runnable() {
        public void run() {
            ECVoIPBaseActivity.super.finish();
        }
    };
    
}

package com.dtalk.dd.voip;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.dtalk.dd.R;
import com.dtalk.dd.utils.ViewUtils;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECVoIPCallManager.CallType;
import com.yuntongxun.ecsdk.SdkErrorCode;

/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class VoIPCallActivity extends ECVoIPBaseActivity {

    private static final String TAG = "ECSDK_Demo.VoIPCallActivity";
	private boolean isCallBack;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.ec_call_interface);
        initCall();
        isCreated=true;
        
    }

    private void initCall() {
        if(mIncomingCall) {
            // 来电
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
        } else {
            // 呼出
            mCallName = getIntent().getStringExtra(EXTRA_CALL_NAME);
            mCallNumber = getIntent().getStringExtra(EXTRA_CALL_NUMBER);

            isCallBack = getIntent().getBooleanExtra(ACTION_CALLBACK_CALL, false);
        }

        initView();
        if (!mIncomingCall) {
            // 处理呼叫逻辑
            if (TextUtils.isEmpty(mCallNumber)) {
                ViewUtils.showMessage(R.string.ec_call_number_error);
                finish();
                return;
            }

            if (isCallBack) {
                VoIPCallHelper.makeCallBack(CallType.VOICE, mCallNumber);
            } else {
                mCallId = VoIPCallHelper.makeCall(mCallType, mCallNumber);
                if (TextUtils.isEmpty(mCallId)) {
                    ViewUtils .showMessage(R.string.ec_app_err_disconnect_server_tip);
                    finish();
                    return;
                }
            }
            mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_connecting_server);
        } else {
            mCallHeaderView.setCallTextMsg(" ");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	if(!isCreated){
         super.onNewIntent(intent);
         initCall();
    	}
    }
    private boolean isCreated=false;

    private void initView() {
        mCallHeaderView = (ECCallHeadUILayout) findViewById(R.id.call_header_ll);
        mCallControlUIView = (ECCallControlUILayout) findViewById(R.id.call_control_ll);
        mCallControlUIView.setOnCallControlDelegate(this);
        mCallHeaderView.setCallName(mCallName);
        mCallHeaderView.setCallNumber(TextUtils.isEmpty(mPhoneNumber) ? mCallNumber : mPhoneNumber);
        mCallHeaderView.setCalling(false);

        ECCallControlUILayout.CallLayout callLayout = mIncomingCall ? ECCallControlUILayout.CallLayout.INCOMING
                : ECCallControlUILayout.CallLayout.OUTGOING;
        mCallControlUIView.setCallDirect(callLayout);

//        mCallHeaderView.setSendDTMFDelegate(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreated=false;
    }

    /**
     * 连接到服务器
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallProceeding(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_call_connect);
    }

    /**
     * 连接到对端用户，播放铃音
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAlerting(String callId) {
        if(!needNotify(callId) || mCallHeaderView == null) {
            return ;
        }
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_wait);
        mCallControlUIView.setCallDirect(ECCallControlUILayout.CallLayout.ALERTING);
    }

    /**
     * 对端应答，通话计时开始
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallAnswered(final String callId) {
        if(!needNotify(callId)|| mCallHeaderView == null) {
            return ;
        }
        mCallHeaderView.setCalling(true);
        
        
        
    }

    @Override
    public void onMakeCallFailed(String callId , int reason) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        mCallHeaderView.setCalling(false);
        mCallHeaderView.setCallTextMsg(CallFailReason.getCallFailReason(reason));
        if(reason != SdkErrorCode.REMOTE_CALL_BUSY && reason != SdkErrorCode.REMOTE_CALL_DECLINED) {
            VoIPCallHelper.releaseCall(mCallId);
            finish();
        }
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }

    /**
     * 通话结束，通话计时结束
     * @param callId 通话的唯一标识
     */
    @Override
    public void onCallReleased(String callId) {
        if(mCallHeaderView == null || !needNotify(callId)) {
            return ;
        }
        mCallHeaderView.setCalling(false);
        mCallHeaderView.setCallTextMsg(R.string.ec_voip_calling_finish);
        mCallControlUIView.setControlEnable(false);
        finish();
    }

	@Override
	public void onMakeCallback(ECError ecError, String caller, String called) {
		if(!TextUtils.isEmpty(mCallId)) {
			return ;
		}
		if(ecError.errorCode != SdkErrorCode.REQUEST_SUCCESS) {
			mCallHeaderView .setCallTextMsg("回拨呼叫失败[" + ecError.errorCode + "]");
		} else {
			mCallHeaderView .setCallTextMsg(R.string.ec_voip_call_back_success);
		}
		mCallHeaderView.setCalling(false);
        mCallControlUIView.setControlEnable(false);
		finish();
	}

	@Override
	public void setDialerpadUI() {
		mCallHeaderView.controllerDiaNumUI();
	}

}
